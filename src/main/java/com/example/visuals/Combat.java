package com.example.visuals;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

/**
 * Подтверждение попаданий: удар считается, только если у цели реально пошёл hurtTime.
 * На этом основаны Hit Sound и Combo Counter.
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

    /** Текущее комбо и время последнего попадания / начала анимации "всплеска". */
    public static int combo = 0;
    public static int best = 0;
    public static long lastHitMs = 0;

    private static int pendingId = -1;
    private static long pendingAt = 0;
    private static int pendingHurt = 0;
    private static int prevPlayerHurt = 0;

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient()) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null && player == mc.player && entity instanceof LivingEntity le && le.isAlive()) {
                    pendingId = le.getId();
                    pendingAt = System.currentTimeMillis();
                    pendingHurt = le.hurtTime;
                }
            }
            return ActionResult.PASS;
        });
    }

    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) {
            combo = 0;
            pendingId = -1;
            return;
        }
        long now = System.currentTimeMillis();

        // получил урон -> комбо сбрасывается
        int ph = mc.player.hurtTime;
        if (ph > prevPlayerHurt) combo = 0;
        prevPlayerHurt = ph;

        // таймаут комбо
        if (combo > 0 && now - lastHitMs > (long) (Config.comboTimeout * 1000)) combo = 0;

        if (pendingId != -1) {
            Entity e = mc.world.getEntityById(pendingId);
            if (e instanceof LivingEntity le && le.hurtTime > pendingHurt) {
                pendingId = -1;
                onHit(mc, now);
            } else if (now - pendingAt > 600) {
                pendingId = -1;
            }
        }
    }

    private static void onHit(MinecraftClient mc, long now) {
        combo++;
        if (combo > best) best = combo;
        lastHitMs = now;
        if (Config.hitSound) playHitSound(mc);
    }

    public static void playHitSound(MinecraftClient mc) {
        int i = Math.max(0, Math.min(Config.hitSoundType, SOUND_IDS.length - 1));
        try {
            SoundEvent ev = SoundEvent.of(Identifier.of(SOUND_IDS[i]));
            mc.getSoundManager().play(PositionedSoundInstance.ui(ev, (float) Config.hitSoundPitch, (float) Config.hitSoundVol));
        } catch (Exception ignored) {}
    }
}
