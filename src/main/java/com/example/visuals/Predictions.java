package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Predictions: траектория + обводка блока приземления + время.
// Работает для: предмета в твоей руке, уже летящих снарядов (твоих и чужих),
// и игроков, которые натягивают лук.
public final class Predictions {
    private Predictions() {}

    // Цвета: свои (фиолетовый -> голубой) и чужие (оранжевый -> розовый)
    private static final int MINE_A = 0xFF9B7BFF, MINE_B = 0xFF4DE1FF;
    private static final int OTHER_A = 0xFFFFB347, OTHER_B = 0xFFFF5C8A;

    private static int pulseAcc = 0;

    // ---------- вспомогательное ----------
    private static int withAlpha(int c, int a) { return (a << 24) | (c & 0xFFFFFF); }

    private static int mix(int a, int b, float t) {
        int aa = (int) (((a >>> 24) & 255) + (((b >>> 24) & 255) - ((a >>> 24) & 255)) * t);
        int rr = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int gr = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bb = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return (aa << 24) | (rr << 16) | (gr << 8) | bb;
    }

    private static final class Cam {
        Vec3d pos, fwd, right, up;
        double tanHalf, aspect;
        int w, h;

        int[] project(Vec3d p) {
            Vec3d d = p.subtract(pos);
            double z = d.dotProduct(fwd);
            if (z < 0.1) return null;
            double x = d.dotProduct(right);
            double y = d.dotProduct(up);
            return new int[]{
                    (int) Math.round(w / 2.0 + (x / z) / (tanHalf * aspect) * (w / 2.0)),
                    (int) Math.round(h / 2.0 - (y / z) / tanHalf * (h / 2.0))};
        }
    }

    private static final class Path {
        List<Vec3d> pts = new ArrayList<>();
        boolean landed;
        int ticks;
        BlockHitResult hit;
    }

    // ---------- параметры выстрела ----------
    // Возвращает {скорость, гравитация} или null
    private static double[] params(LivingEntity p, ItemStack st) {
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

    private static Path simulate(MinecraftClient mc, Entity src, Vec3d pos, Vec3d vel, double gravity) {
        Path path = new Path();
        path.pts.add(pos);
        for (int i = 0; i < 200; i++) {
            Vec3d next = pos.add(vel);
            BlockHitResult hit = mc.world.raycast(new RaycastContext(pos, next,
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, src));
            path.ticks = i + 1;
            if (hit.getType() != HitResult.Type.MISS) {
                path.pts.add(hit.getPos());
                path.landed = true;
                path.hit = hit;
                break;
            }
            path.pts.add(next);
            if (next.y < mc.world.getBottomY()) break;
            pos = next;
            vel = vel.multiply(0.99).add(0, -gravity, 0);
        }
        return path;
    }

    // ---------- рисование ----------
    private static void seg(DrawContext g, int[] a, int[] b, int ca, int cb) {
        int dx = b[0] - a[0], dy = b[1] - a[1];
        int n = Math.max(Math.abs(dx), Math.abs(dy));
        if (n == 0 || n > 1500) return;
        int offset = (int) ((System.currentTimeMillis() / 30) % 16);
        for (int i = 0; i <= n; i++) {
            float t = i / (float) n;
            int x = a[0] + dx * i / n;
            int y = a[1] + dy * i / n;
            int c = mix(ca, cb, t);
            if (i % 2 == 0) g.fill(x - 1, y - 1, x + 3, y + 3, withAlpha(c, 0x28)); // свечение
            g.fill(x, y, x + 2, y + 2, c);
            pulseAcc++;
            if ((pulseAcc + offset) % 16 < 2) g.fill(x - 1, y - 1, x + 3, y + 3, 0xFFFFFFFF); // бегущая точка
        }
    }

    private static void drawPath(DrawContext g, Cam cam, Path path, int c0, int c1) {
        int n = path.pts.size();
        if (n < 2) return;
        pulseAcc = 0;
        int[] prev = null;
        for (int i = 0; i < n; i++) {
            int[] cur = cam.project(path.pts.get(i));
            if (cur != null && prev != null) {
                seg(g, prev, cur, mix(c0, c1, (i - 1) / (float) (n - 1)), mix(c0, c1, i / (float) (n - 1)));
            }
            prev = cur;
        }
    }

    private static void thin(DrawContext g, int[] a, int[] b, int color, int size) {
        int dx = b[0] - a[0], dy = b[1] - a[1];
        int n = Math.max(Math.abs(dx), Math.abs(dy));
        if (n == 0 || n > 1500) return;
        for (int i = 0; i <= n; i++) {
            int x = a[0] + dx * i / n;
            int y = a[1] + dy * i / n;
            g.fill(x, y, x + size, y + size, color);
        }
    }

    // Заливка выпуклого четырёхугольника горизонтальными полосами
    private static void fillQuad(DrawContext g, int[][] q, int color) {
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (int[] p : q) { minY = Math.min(minY, p[1]); maxY = Math.max(maxY, p[1]); }
        if (maxY - minY > 1500) return;
        for (int y = minY; y <= maxY; y++) {
            double lo = Double.MAX_VALUE, hi = -Double.MAX_VALUE;
            for (int e = 0; e < 4; e++) {
                int[] a = q[e], b = q[(e + 1) % 4];
                if (a[1] == b[1]) {
                    if (y == a[1]) {
                        lo = Math.min(lo, Math.min(a[0], b[0]));
                        hi = Math.max(hi, Math.max(a[0], b[0]));
                    }
                    continue;
                }
                if ((y < a[1] && y < b[1]) || (y > a[1] && y > b[1])) continue;
                double x = a[0] + (b[0] - a[0]) * (double) (y - a[1]) / (b[1] - a[1]);
                lo = Math.min(lo, x);
                hi = Math.max(hi, x);
            }
            if (hi >= lo) g.fill((int) lo, y, (int) hi + 1, y + 1, color);
        }
    }

    // Обводка блока приземления + подсветка стороны, в которую попадёт снаряд
    private static void drawBlock(DrawContext g, Cam cam, BlockHitResult hit, int color) {
        BlockPos bp = hit.getBlockPos();
        int[][] pr = new int[8][];
        for (int i = 0; i < 8; i++) {
            Vec3d c = new Vec3d(bp.getX() + (i & 1), bp.getY() + ((i >> 1) & 1), bp.getZ() + ((i >> 2) & 1));
            pr[i] = cam.project(c);
        }
        // рёбра куба
        for (int i = 0; i < 8; i++) {
            for (int b = 1; b <= 4; b <<= 1) {
                if ((i & b) != 0) continue;
                int[] a = pr[i], c = pr[i | b];
                if (a != null && c != null) thin(g, a, c, withAlpha(color, 0xAA), 1);
            }
        }
        // подсвеченная грань
        Direction s = hit.getSide();
        int[] idx;
        if (s == Direction.UP) idx = new int[]{2, 3, 7, 6};
        else if (s == Direction.DOWN) idx = new int[]{0, 1, 5, 4};
        else if (s == Direction.EAST) idx = new int[]{1, 3, 7, 5};
        else if (s == Direction.WEST) idx = new int[]{0, 2, 6, 4};
        else if (s == Direction.SOUTH) idx = new int[]{4, 5, 7, 6};
        else idx = new int[]{0, 1, 3, 2};
        int[][] q = new int[4][];
        for (int i = 0; i < 4; i++) {
            q[i] = pr[idx[i]];
            if (q[i] == null) return;
        }
        fillQuad(g, q, withAlpha(color, 0x45));
        for (int i = 0; i < 4; i++) thin(g, q[i], q[(i + 1) % 4], color, 2);
    }

    private static void label(DrawContext g, MinecraftClient mc, Cam cam, Vec3d at, String text, int accent) {
        int[] sp = cam.project(at);
        if (sp == null) return;
        int tw = mc.textRenderer.getWidth(text);
        int pw = tw + 12, ph = 14;
        int x = Math.max(2, Math.min(cam.w - pw - 2, sp[0] - pw / 2));
        int y = Math.max(2, Math.min(cam.h - ph - 2, sp[1] - 26));
        int bg = 0xCC101018;
        g.fill(x + 1, y, x + pw - 1, y + ph, bg);
        g.fill(x, y + 1, x + 1, y + ph - 1, bg);
        g.fill(x + pw - 1, y + 1, x + pw, y + ph - 1, bg);
        g.fill(x + 1, y, x + pw - 1, y + 1, accent);           // верхняя акцентная линия
        g.fill(x + 1, y + ph - 1, x + pw - 1, y + ph, withAlpha(accent, 0x60));
        g.drawTextWithShadow(mc.textRenderer, text, x + 6, y + 3, 0xFFFFFFFF);
    }

    private static void show(DrawContext g, MinecraftClient mc, Cam cam, Path path, boolean mine, Vec3d eye) {
        int c0 = mine ? MINE_A : OTHER_A;
        int c1 = mine ? MINE_B : OTHER_B;
        drawPath(g, cam, path, c0, c1);
        Vec3d end = path.pts.get(path.pts.size() - 1);
        double secs = path.ticks / 20.0;
        if (path.landed) {
            drawBlock(g, cam, path.hit, c1);
            String t = String.format(Locale.ROOT, "%.1f c  |  %.0f м", secs, end.distanceTo(eye));
            label(g, mc, cam, end, t, c1);
        } else {
            label(g, mc, cam, end, String.format(Locale.ROOT, "в воздухе %.1f c", secs), c1);
        }
    }

    // ---------- главная точка входа (из Features.hud) ----------
    public static void render(DrawContext g, MinecraftClient mc, float td) {
        if (!Config.predictions) return;
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null || mc.currentScreen != null) return;

        // камера для проекции
        Vec3d look = p.getRotationVec(td);
        Vec3d eye = p.getCameraPosVec(td);
        Perspective persp = mc.options.getPerspective();
        Cam cam = new Cam();
        cam.fwd = look;
        cam.pos = eye;
        if (persp == Perspective.THIRD_PERSON_BACK) cam.pos = eye.subtract(look.multiply(4.0));
        else if (persp == Perspective.THIRD_PERSON_FRONT) { cam.pos = eye.add(look.multiply(4.0)); cam.fwd = look.negate(); }
        Vec3d right = cam.fwd.crossProduct(new Vec3d(0, 1, 0));
        if (right.lengthSquared() < 1.0E-6) return;
        cam.right = right.normalize();
        cam.up = cam.right.crossProduct(cam.fwd);
        cam.w = mc.getWindow().getScaledWidth();
        cam.h = mc.getWindow().getScaledHeight();
        cam.tanHalf = Math.tan(Math.toRadians(mc.options.getFov().getValue()) / 2.0);
        cam.aspect = (double) cam.w / cam.h;

        // 1) предмет в руке (до броска)
        double[] pr = params(p, p.getMainHandStack());
        if (pr == null) pr = params(p, p.getOffHandStack());
        if (pr != null) {
            Vec3d vel = look.multiply(pr[0]);
            Vec3d pv = p.getVelocity();
            vel = vel.add(pv.x, p.isOnGround() ? 0 : pv.y, pv.z);
            show(g, mc, cam, simulate(mc, p, eye.subtract(0, 0.1, 0), vel, pr[1]), true, eye);
        }

        int drawn = 0;

        // 2) уже летящие снаряды (твои и чужие)
        for (Entity e : mc.world.getEntities()) {
            if (drawn >= 12) break;
            double gravity = -1;
            if (e instanceof PersistentProjectileEntity) gravity = 0.05;
            else if (e instanceof ThrownItemEntity t) {
                ItemStack s = t.getStack();
                if (s.isOf(Items.SPLASH_POTION) || s.isOf(Items.LINGERING_POTION)) gravity = 0.05;
                else if (s.isOf(Items.EXPERIENCE_BOTTLE)) gravity = 0.07;
                else gravity = 0.03;
            }
            if (gravity < 0) continue;
            if (e.squaredDistanceTo(p) > 128.0 * 128.0) continue;
            Vec3d vel = e.getVelocity();
            if (vel.lengthSquared() < 0.01) continue; // стоит / воткнулась
            Path path = simulate(mc, e, e.getLerpedPos(td), vel, gravity);
            if (path.landed && path.ticks <= 1) continue; // уже в блоке
            boolean mine = e instanceof ProjectileEntity pe && pe.getOwner() == p;
            show(g, mc, cam, path, mine, eye);
            drawn++;
        }

        // 3) другие игроки, которые натягивают лук
        for (AbstractClientPlayerEntity o : mc.world.getPlayers()) {
            if (drawn >= 12) break;
            if (o == p || o.squaredDistanceTo(p) > 64.0 * 64.0) continue;
            if (!o.isUsingItem()) continue;
            double[] op = params(o, o.getActiveItem());
            if (op == null) continue;
            Vec3d oeye = o.getCameraPosVec(td);
            Vec3d vel = o.getRotationVec(td).multiply(op[0]);
            Vec3d ov = o.getVelocity();
            vel = vel.add(ov.x, o.isOnGround() ? 0 : ov.y, ov.z);
            show(g, mc, cam, simulate(mc, o, oeye.subtract(0, 0.1, 0), vel, op[1]), false, eye);
            drawn++;
        }
    }
}
