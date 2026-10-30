package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Predictions: линия траектории (стрелы, трезубец, жемчуг, снежки, зелья) + время до приземления.
// Линия рисуется на HUD через собственную проекцию 3D -> экран.
public final class Predictions {
    private Predictions() {}

    private static final int LINE = 0xFF859CFF;
    private static final int HIT = 0xFFFF5555;

    // Возвращает {скорость, гравитация} или null, если предмет не метательный
    private static double[] params(ClientPlayerEntity p, ItemStack st) {
        if (st.isEmpty()) return null;
        if (st.isOf(Items.BOW)) {
            if (!p.isUsingItem() || !p.getActiveItem().isOf(Items.BOW)) return null;
            float f = p.getItemUseTime() / 20.0f;
            f = (f * f + f * 2.0f) / 3.0f;
            if (f < 0.1f) return null;
            if (f > 1.0f) f = 1.0f;
            return new double[]{f * 3.0, 0.05};
        }
        if (st.isOf(Items.CROSSBOW)) return CrossbowItem.isCharged(st) ? new double[]{3.15, 0.05} : null;
        if (st.isOf(Items.TRIDENT)) return new double[]{2.5, 0.05};
        if (st.isOf(Items.SNOWBALL) || st.isOf(Items.EGG) || st.isOf(Items.ENDER_PEARL)) return new double[]{1.5, 0.03};
        if (st.isOf(Items.SPLASH_POTION) || st.isOf(Items.LINGERING_POTION)) return new double[]{0.5, 0.05};
        if (st.isOf(Items.EXPERIENCE_BOTTLE)) return new double[]{0.7, 0.07};
        return null;
    }

    // Рисует линию из квадратиков 2x2
    private static void line(DrawContext g, int x0, int y0, int x1, int y1, int color) {
        int dx = x1 - x0, dy = y1 - y0;
        int n = Math.max(Math.abs(dx), Math.abs(dy));
        if (n > 1500) return;
        if (n == 0) { g.fill(x0, y0, x0 + 2, y0 + 2, color); return; }
        for (int i = 0; i <= n; i++) {
            int x = x0 + dx * i / n;
            int y = y0 + dy * i / n;
            g.fill(x, y, x + 2, y + 2, color);
        }
    }

    public static void render(DrawContext g, MinecraftClient mc, float tickDelta) {
        if (!Config.predictions) return;
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null || mc.currentScreen != null) return;

        double[] pr = params(p, p.getMainHandStack());
        if (pr == null) pr = params(p, p.getOffHandStack());
        if (pr == null) return;
        double gravity = pr[1];

        // --- симуляция полёта ---
        Vec3d look = p.getRotationVec(tickDelta);
        Vec3d eye = p.getCameraPosVec(tickDelta);
        Vec3d pos = eye.subtract(0, 0.1, 0);
        Vec3d vel = look.multiply(pr[0]);
        Vec3d pv = p.getVelocity();
        vel = vel.add(pv.x, p.isOnGround() ? 0 : pv.y, pv.z);

        List<Vec3d> pts = new ArrayList<>();
        pts.add(pos);
        boolean landed = false;
        int ticks = 0;
        for (int i = 0; i < 200; i++) {
            Vec3d next = pos.add(vel);
            BlockHitResult hit = mc.world.raycast(new RaycastContext(pos, next,
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, p));
            ticks = i + 1;
            if (hit.getType() != HitResult.Type.MISS) {
                pts.add(hit.getPos());
                landed = true;
                break;
            }
            pts.add(next);
            if (next.y < mc.world.getBottomY()) break;
            pos = next;
            vel = vel.multiply(0.99).add(0, -gravity, 0);
        }

        // --- камера для проекции ---
        Perspective persp = mc.options.getPerspective();
        Vec3d fwd = look;
        Vec3d cam = eye;
        if (persp == Perspective.THIRD_PERSON_BACK) cam = eye.subtract(look.multiply(4.0));
        else if (persp == Perspective.THIRD_PERSON_FRONT) { cam = eye.add(look.multiply(4.0)); fwd = look.negate(); }

        Vec3d right = fwd.crossProduct(new Vec3d(0, 1, 0));
        if (right.lengthSquared() < 1.0E-6) return;
        right = right.normalize();
        Vec3d up = right.crossProduct(fwd);

        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();
        double tanHalf = Math.tan(Math.toRadians(mc.options.getFov().getValue()) / 2.0);
        double aspect = (double) w / h;

        int px = 0, py = 0;
        boolean havePrev = false;
        int lastX = 0, lastY = 0;
        boolean lastVisible = false;
        for (Vec3d pt : pts) {
            Vec3d d = pt.subtract(cam);
            double z = d.dotProduct(fwd);
            if (z < 0.1) { havePrev = false; lastVisible = false; continue; }
            double x = d.dotProduct(right);
            double y = d.dotProduct(up);
            int sx = (int) Math.round(w / 2.0 + (x / z) / (tanHalf * aspect) * (w / 2.0));
            int sy = (int) Math.round(h / 2.0 - (y / z) / tanHalf * (h / 2.0));
            if (havePrev) line(g, px, py, sx, sy, LINE);
            px = sx; py = sy; havePrev = true;
            lastX = sx; lastY = sy; lastVisible = true;
        }

        // --- метка приземления и время ---
        double secs = ticks / 20.0;
        double dist = pts.get(pts.size() - 1).distanceTo(eye);
        String label = landed
                ? String.format(Locale.ROOT, "%.1f c | %.0f м", secs, dist)
                : String.format(Locale.ROOT, "в воздухе %.1f c", secs);

        int tx, ty;
        if (landed && lastVisible && lastX > 0 && lastX < w && lastY > 0 && lastY < h) {
            g.fill(lastX - 3, lastY - 3, lastX + 4, lastY + 4, HIT);
            tx = lastX + 8;
            ty = lastY - 4;
        } else {
            tx = w / 2 - mc.textRenderer.getWidth(label) / 2;
            ty = h / 2 + 24;
        }
        g.drawTextWithShadow(mc.textRenderer, label, tx, ty, -1);
    }
}
