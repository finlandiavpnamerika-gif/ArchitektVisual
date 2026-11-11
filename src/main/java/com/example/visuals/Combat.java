package com.example.visuals;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

/**
 * Боевая логика клиента: подтверждение попаданий (по hurtTime цели), комбо, статистика
 * (точность, киллы, смерти, серия), лог урона, определение критов и убийств.
 */
public final class Combat {
    private Combat() {}

    public static final String[] SOUNDS = {"Дзинь", "Колокол", "Пинг", "Щелчок", "Попадание"};
    private static final String[] SOUND_IDS = {
            "entity.experience_orb.pickup",
            "block.note_block.bell",
            "block.note_block.pling",
            "ui.button.click",
            "entity.arrow.hit_player"
    };

    // ---- комбо ----
    public static int combo = 0;
    public static int best = 0;
    public static long lastHitMs = 0;

    // ---- статистика ----
    public static int attempts = 0, hits = 0, kills = 0, deaths = 0, streak = 0, bestStreak = 0;

    // ---- лог урона ----
    public static final class LogEntry {
        public final String name;
        public final float amount;
        public final boolean dealt;
        public final long at;
        LogEntry(String name, float amount, boolean dealt, long at) {
            this.name = name; this.amount = amount; this.dealt = dealt; this.at = at;
        }
    }
    public static final List<LogEntry> LOG = new ArrayList<>();

    // ---- внутреннее состояние ----
    private static int pendingId = -1;
    private static long pendingAt = 0;
    private static int pendingHurt = 0;
    private static boolean pendingCrit = false;

    private static int trackId = -1;
    private static float trackHp = -1;
    private static double tx, ty, tz, trackH = 1.8;
    private static boolean trackPlayer = false;
    private static String trackName = "";
    private static long trackAttackAt = 0;
    private static boolean trackHitSeen = false;

    private static int prevPlayerHurt = 0;
    private static float selfHp = -1;
    private static boolean wasDeadScreen = false;

    public static void resetStats() {
        attempts = hits = kills = deaths = streak = bestStreak = 0;
        best = 0;
        LOG.clear();
    }

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient()) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null && player == mc.player && entity instanceof LivingEntity le && le.isAlive()) {
                    long now = System.currentTimeMillis();
                    attempts++;
                    pendingId = le.getId();
                    pendingAt = now;
                    pendingHurt = le.hurtTime;
                    pendingCrit = isCrit(mc.player);

                    if (trackId != le.getId()) {
                        trackId = le.getId();
                        trackHp = le.getHealth() + le.getAbsorptionAmount();
                        trackHitSeen = false;
                    }
                    trackPlayer = le instanceof PlayerEntity;
                    trackName = le.getName().getString();
                    trackAttackAt = now;
                    trackH = le.getHeight();
                    tx = le.getX(); ty = le.getY(); tz = le.getZ();
                }
            }
            return ActionResult.PASS;
        });
    }

    /** Условие критического удара как в ваниле. */
    private static boolean isCrit(PlayerEntity p) {
        return p.getAttackCooldownProgress(0.5f) > 0.9f
                && p.fallDistance > 0 && !p.isOnGround() && !p.isClimbing() && !p.isTouchingWater()
                && !p.hasStatusEffect(StatusEffects.BLINDNESS) && !p.hasVehicle() && !p.isSprinting();
    }

    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) {
            combo = 0;
            pendingId = -1;
            trackId = -1;
            selfHp = -1;
            return;
        }
        long now = System.currentTimeMillis();

        // смерть игрока (по экрану смерти)
        boolean dead = mc.currentScreen instanceof DeathScreen;
        if (dead && !wasDeadScreen) {
            deaths++;
            streak = 0;
            combo = 0;
        }
        wasDeadScreen = dead;

        // получил урон -> комбо сбрасывается; лог полученного урона
        int ph = mc.player.hurtTime;
        if (ph > prevPlayerHurt) combo = 0;
        prevPlayerHurt = ph;
        float hpNow = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (selfHp >= 0 && hpNow < selfHp - 0.01f && mc.player.isAlive()) addLog("Вы", selfHp - hpNow, false, now);
        selfHp = hpNow;

        // таймаут комбо
        if (combo > 0 && now - lastHitMs > (long) (Config.comboTimeout * 1000)) combo = 0;

        // подтверждение попадания (убийственный удар не даёт hurtTime, поэтому считаем и смерть цели)
        if (pendingId != -1) {
            Entity e = mc.world.getEntityById(pendingId);
            if (e instanceof LivingEntity le && (le.hurtTime > pendingHurt || le.getHealth() <= 0)) {
                pendingId = -1;
                onHit(mc, le, now);
            } else if (now - pendingAt > 600) {
                pendingId = -1;
            }
        }

        trackTarget(mc, now);
    }

    private static void trackTarget(MinecraftClient mc, long now) {
        if (trackId == -1) return;
        Entity e = mc.world.getEntityById(trackId);
        if (e instanceof LivingEntity le && !le.isDead() && le.getHealth() > 0) {
            float hp = le.getHealth() + le.getAbsorptionAmount();
            tx = le.getX(); ty = le.getY(); tz = le.getZ();
            trackH = le.getHeight();
            if (trackHp >= 0 && hp < trackHp - 0.01f && now - trackAttackAt < 1500) {
                addLog(trackName, trackHp - hp, true, now);
            }
            trackHp = hp;
            if (now - trackAttackAt > 8000) trackId = -1;
        } else {
            // цель умерла (здоровье 0) либо исчезла с малым остатком HP рядом после нашего удара
            boolean dead = e instanceof LivingEntity le2 && (le2.isDead() || le2.getHealth() <= 0);
            boolean near = mc.player.squaredDistanceTo(tx, ty, tz) < 16 * 16;
            boolean vanished = e == null && near && trackHp >= 0 && trackHp <= 6f && now - trackAttackAt < 1500;
            if (now - trackAttackAt < 4000 && (dead || vanished)) onKill(mc, now);
            trackId = -1;
        }
    }

    private static void onHit(MinecraftClient mc, LivingEntity le, long now) {
        hits++;
        combo++;
        if (combo > best) best = combo;
        lastHitMs = now;
        if (trackId == le.getId()) trackHitSeen = true;
        if (Config.hitSound) playHitSound(mc);
        if (pendingCrit && Config.critFx) CombatFx.crit(mc, le);
        pendingCrit = false;
    }

    private static void onKill(MinecraftClient mc, long now) {
        // эффект показываем для любой цели, а статистика может считаться только по игрокам
        if (Config.killFx) GhostFx.spawn(mc, tx, ty, tz, trackH);
        if (Config.statsPlayersOnly && !trackPlayer) return;
        kills++;
        streak++;
        if (streak > bestStreak) bestStreak = streak;
    }

    private static void addLog(String name, float amount, boolean dealt, long now) {
        LOG.add(new LogEntry(name, amount, dealt, now));
        while (LOG.size() > 30) LOG.remove(0);
    }

    public static void playHitSound(MinecraftClient mc) {
        playSound(mc, SOUND_IDS[Math.max(0, Math.min(Config.hitSoundType, SOUND_IDS.length - 1))],
                (float) Config.hitSoundPitch, (float) Config.hitSoundVol);
    }

    public static void playSound(MinecraftClient mc, String id, float pitch, float vol) {
        try {
            SoundEvent ev = SoundEvent.of(Identifier.of(id));
            mc.getSoundManager().play(PositionedSoundInstance.master(ev, pitch, vol));
        } catch (Exception ignored) {}
    }
}
