package com.example.visuals;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * CustomItems: своя 3D-модель вместо предмета в руке (от первого лица).
 * Сейчас: незеритовый меч -> керамбит.
 * Файл модели karambit.bin получен из .glb: цвета текстуры запечены в вершины (assets/visualsmod/models/karambit.bin).
 * Формат: "KRMB", int nVerts, int nTris, float[nVerts*3] позиции, int[nVerts] ARGB, int[nTris*3] индексы.
 * Начало координат модели - рукоять, лезвие направлено по +Y, длина модели = 1.
 */
public final class CustomItems {
    private CustomItems() {}

    private static boolean loaded = false, failed = false;
    private static float[] pos;
    private static int[] argb, idx;

    private static boolean load() {
        if (loaded) return true;
        if (failed) return false;
        try (InputStream in = CustomItems.class.getResourceAsStream("/assets/visualsmod/models/karambit.bin")) {
            if (in == null) { failed = true; return false; }
            ByteBuffer b = ByteBuffer.wrap(in.readAllBytes()).order(ByteOrder.LITTLE_ENDIAN);
            b.position(4);   // "KRMB"
            int nv = b.getInt(), nt = b.getInt();
            pos = new float[nv * 3];
            for (int i = 0; i < pos.length; i++) pos[i] = b.getFloat();
            argb = new int[nv];
            for (int i = 0; i < nv; i++) argb[i] = b.getInt();
            idx = new int[nt * 3];
            for (int i = 0; i < idx.length; i++) idx[i] = b.getInt();
            loaded = true;
            return true;
        } catch (Exception e) {
            failed = true;
            return false;
        }
    }

    /** true, если предмет нарисован нашей моделью (ванильный рендер тогда отменяется). */
    public static boolean renderHeld(ItemStack stack, ItemDisplayContext ctx, MatrixStack m, OrderedRenderCommandQueue queue) {
        if (!Config.customItems || !ctx.isFirstPerson() || !stack.isOf(Items.NETHERITE_SWORD)) return false;
        if (!load()) return false;

        m.push();
        if (ctx.isLeftHand()) m.scale(-1f, 1f, 1f);                        // зеркалим для левой руки
        m.translate(Config.ciX / 100.0, Config.ciY / 100.0, -Config.ciZ / 100.0);
        m.multiply(new Quaternionf().rotationXYZ(
                (float) Math.toRadians(Config.ciRX), (float) Math.toRadians(Config.ciRY), (float) Math.toRadians(Config.ciRZ)));
        float s = (float) Config.ciScale;
        m.scale(s, s, s);
        queue.submitCustom(m, RenderLayers.debugQuads(), (entry, vc) -> emit(entry.getPositionMatrix(), vc));
        m.pop();
        return true;
    }

    private static void emit(Matrix4f mat, VertexConsumer vc) {
        for (int t = 0; t < idx.length; t += 3) {
            int a = idx[t], b = idx[t + 1], c = idx[t + 2];
            // треугольник как вырожденный четырёхугольник, обе стороны (чтобы не зависеть от отсечения граней)
            tri(mat, vc, a, b, c);
            tri(mat, vc, c, b, a);
        }
    }

    private static void tri(Matrix4f mat, VertexConsumer vc, int a, int b, int c) {
        v(mat, vc, a); v(mat, vc, b); v(mat, vc, c); v(mat, vc, c);
    }

    private static void v(Matrix4f mat, VertexConsumer vc, int i) {
        vc.vertex(mat, pos[i * 3], pos[i * 3 + 1], pos[i * 3 + 2]).color(argb[i]);
    }
}
