package com.example.visuals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * DamageFever: над целью всплывает число полученного урона и улетает вверх.
 * Урон определяется по падению здоровья цели (health + absorption), поэтому работает и на серверах.
 * Опция "Только мои удары": показывать урон только от твоих ударов.
 */
public final class DamageFever {
    private DamageFever() {}

    private static final long MINE_WINDOW_MS = 1200;
    private static final int MAX = 60;
    private static final Random RND = new Random();

    private static final class Pop {
        double x, y, z;
        float dmg;
        long born;
        float drift;
    }

    private static final ArrayList<Pop> POPS = new ArrayList<>();
    private static final Map<Integer, Float> HP = new HashMap<>();
    private static int lastHitId = -1;
    private static long lastHitAt = 0;
    private static long lastClean = 0;

    /** Вызвать один раз из VisualsClient.onInitializeClient(). */
    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient()) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null && player == mc.player) {
                    lastHitId = entity.getId();
                    lastHitAt = System.currentTimeMillis();
                }
            }
            return ActionResult.PASS;
        });
    }

    /** Каждый тик клиента. */
    public static void tick(MinecraftClient mc) {
        if (!Config.damageFever || mc.world == null || mc.player == null) {
            HP.clear();
            POPS.clear();
            return;
        }
        long now = System.currentTimeMillis();
        boolean clean = now - lastClean > 2000;
        java.util.HashSet<Integer> seen = clean ? new java.util.HashSet<>() : null;

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le) || le == mc.player || !le.isAlive()) continue;
            if (mc.player.squaredDistanceTo(le) > 64 * 64) continue;
            int id = le.getId();
            if (seen != null) seen.add(id);
            float hp = le.getHealth() + le.getAbsorptionAmount();
            Float prev = HP.put(id, hp);
            if (prev == null) continue;
            float dmg = prev - hp;
            if (dmg < 0.05f) continue;
            if (Config.dfMine && !(id == lastHitId && now - lastHitAt < MINE_WINDOW_MS)) continue;
            spawn(le, dmg, now);
        }
        if (clean) { HP.keySet().retainAll(seen); lastClean = now; }
    }

    private static void spawn(LivingEntity e, float dmg, long now) {
        if (POPS.size() >= MAX) POPS.remove(0);
        Pop p = new Pop();
        p.x = e.getX() + (RND.nextFloat() - 0.5) * 0.5;
        p.y = e.getY() + e.getHeight() * 0.85;
        p.z = e.getZ() + (RND.nextFloat() - 0.5) * 0.5;
        p.dmg = dmg;
        p.born = now;
        p.drift = (RND.nextFloat() - 0.5f) * 0.6f;
        POPS.add(p);
    }

    /** Из HudRenderCallback. */
    public static void hud(DrawContext g) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!Config.damageFever || mc.world == null || mc.player == null || POPS.isEmpty()) return;
        if (mc.options.hudHidden) return;

        Camera cam = mc.gameRenderer.getCamera();
        Vec3d cp = cam.getCameraPos();
        Quaternionf inv = new Quaternionf(cam.getRotation()).invert();
        int sw = g.getScaledWindowWidth(), sh = g.getScaledWindowHeight();
        double fov = Math.toRadians(mc.options.getFov().getValue());
        float tanHalf = (float) Math.tan(fov / 2.0);
        float aspect = (float) mc.getWindow().getFramebufferWidth() / (float) mc.getWindow().getFramebufferHeight();
        TextRenderer tr = mc.textRenderer;
        long now = System.currentTimeMillis();
        float life = (float) Config.dfLife * 1000f;
        int base = MathHelper.hsvToRgb((float) Config.dfHue, 0.85f, 1f) & 0xFFFFFF;

        Matrix3x2fStack m = g.getMatrices();
        Iterator<Pop> it = POPS.iterator();
        while (it.hasNext()) {
            Pop p = it.next();
            float age = now - p.born;
            if (age >= life) { it.remove(); continue; }
            float t = age / life;
            float rise = 1f - (1f - t) * (1f - t);                       // быстро вверх, потом медленнее
            Vector3f v = new Vector3f((float) (p.x - cp.x) + p.drift * t,
                                      (float) (p.y - cp.y) + 1.1f * rise,
                                      (float) (p.z - cp.z));
            inv.transform(v);
            float depth = -v.z;
            if (depth < 0.1f) continue;
            float ndcX = v.x / (depth * tanHalf * aspect);
            float ndcY = v.y / (depth * tanHalf);
            if (Math.abs(ndcX) > 1.2f || Math.abs(ndcY) > 1.2f) continue;
            float sx = (ndcX * 0.5f + 0.5f) * sw;
            float sy = (0.5f - ndcY * 0.5f) * sh;

            float pop = age < 150f ? 1.6f - 0.6f * (age / 150f) : 1f;     // "выпрыгивает"
            float heavy = 1f + Math.min(p.dmg, 12f) * 0.035f;             // сильные удары крупнее
            float s = (float) Config.dfSize * pop * heavy * MathHelper.clamp(5f / depth, 0.6f, 2.2f);

            String text = Config.dfDecimals ? String.format(java.util.Locale.ROOT, "%.1f", p.dmg)
                                            : String.valueOf(Math.max(1, Math.round(p.dmg)));
            float fade = t > 0.6f ? 1f - (t - 0.6f) / 0.4f : 1f;
            int a = Math.max(5, Math.min(255, Math.round(255 * fade)));
            int color = (a << 24) | base;

            m.pushMatrix();
            m.translate(sx, sy);
            m.scale(s, s);
            g.drawTextWithShadow(tr, text, -tr.getWidth(text) / 2, -4, color);
            m.popMatrix();
        }
    }
}
