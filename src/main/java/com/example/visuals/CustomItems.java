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
import net.minecraft.registry.Registries;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * CustomItems: своя 3D-модель вместо меча в руке (от первого лица).
 * Модели лежат в assets/visualsmod/models/*.bin (запечены из .glb). Начало координат - конец рукояти,
 * лезвие направлено по +Y, длина модели = 1.
 * Форматы: "KRMB" (цвет в вершинах) и "KRM2" (общие вершины, цвет на каждый треугольник).
 */
public final class CustomItems {
    private CustomItems() {}

    public static final String[] MODELS = {"Керамбит", "Катана", "Красный световой меч", "Нож-бабочка", "Кунай", "Штык-нож", "Майнкрафт-меч"};
    private static final String[] FILES = {"karambit", "katana", "redsword", "butterfly", "kunai", "bayonet", "mcsword"};
    private static final float[] SIZE = {1.0f, 1.5f, 1.3f, 0.95f, 0.75f, 0.85f, 1.4f};   // множитель длины под каждую модель

    public static final String[] SKINS = {"Оригинал", "Свой цвет", "Радуга", "Тёмная", "Золото", "Лёд", "Кровь"};

    private static final class Model {
        boolean loaded, failed;
        float[] pos;
        int[] idx;          // по 3 на треугольник
        int[] base;         // исходные цвета (RGB), на вершину (KRMB) или на треугольник (KRM2)
        boolean perTri;
        final java.util.HashMap<Integer, int[]> skins = new java.util.HashMap<>();
    }

    private static final Model[] CACHE = new Model[FILES.length];

    private static Model model(int i) {
        i = Math.max(0, Math.min(i, FILES.length - 1));
        Model m = CACHE[i];
        if (m == null) { m = new Model(); CACHE[i] = m; }
        if (m.loaded || m.failed) return m;
        try (InputStream in = CustomItems.class.getResourceAsStream("/assets/visualsmod/models/" + FILES[i] + ".bin")) {
            if (in == null) { m.failed = true; return m; }
            ByteBuffer b = ByteBuffer.wrap(in.readAllBytes()).order(ByteOrder.LITTLE_ENDIAN);
            byte k = b.get(3);
            b.position(4);
            int nv = b.getInt(), nt = b.getInt();
            m.pos = new float[nv * 3];
            for (int j = 0; j < m.pos.length; j++) m.pos[j] = b.getFloat();
            if (k == '2') {
                m.perTri = true;
                m.idx = new int[nt * 3];
                for (int j = 0; j < m.idx.length; j++) m.idx[j] = b.getInt();
                m.base = new int[nt];
                for (int j = 0; j < nt; j++) m.base[j] = b.getInt();
            } else {
                m.base = new int[nv];
                for (int j = 0; j < nv; j++) m.base[j] = b.getInt();
                m.idx = new int[nt * 3];
                for (int j = 0; j < m.idx.length; j++) m.idx[j] = b.getInt();
            }
            m.loaded = true;
        } catch (Exception e) {
            m.failed = true;
        }
        return m;
    }

    // ---------- текстуры (перекраска) ----------
    private static int rgb(float r, float g, float b) {
        int ri = Math.max(0, Math.min(255, (int) r)), gi = Math.max(0, Math.min(255, (int) g)), bi = Math.max(0, Math.min(255, (int) b));
        return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
    }

    private static int skinColor(int c, int skin, float hue, float sat) {
        float r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
        float l = (0.3f * r + 0.59f * g + 0.11f * b);
        switch (skin) {
            case 1, 2: {
                int t = MathHelper.hsvToRgb(hue, sat, 1f) & 0xFFFFFF;
                float k = l / 255f * 1.15f;
                return rgb(((t >> 16) & 255) * k, ((t >> 8) & 255) * k, (t & 255) * k);
            }
            case 3: return rgb(r * 0.38f, g * 0.38f, b * 0.4f);
            case 4: { float k = l / 255f * 1.25f; return rgb(255 * k, 200 * k, 70 * k); }
            case 5: { float k = l / 255f * 1.2f; return rgb(140 * k, 215 * k, 255 * k); }
            case 6: { float k = l / 255f * 1.25f; return rgb(255 * k, 25 * k, 30 * k); }
            default: return 0xFF000000 | (c & 0xFFFFFF);
        }
    }

    private static int[] colors(Model m, CiRule r) {
        int skin = r.skin;
        if (skin <= 0 || skin >= SKINS.length) return m.base;
        float hue = (float) r.hue;
        if (skin == 2) hue = (System.currentTimeMillis() % 6000L) / 6000f;
        int key = skin * 100000 + (int) (hue * 90f) * 100 + (int) (r.sat * 10);
        int[] arr = m.skins.get(key);
        if (arr == null) {
            if (m.skins.size() > 12) m.skins.clear();
            arr = new int[m.base.length];
            for (int i = 0; i < arr.length; i++) arr[i] = skinColor(m.base[i], skin, hue, (float) r.sat);
            m.skins.put(key, arr);
        }
        return arr;
    }

    // ---------- поиск правила для предмета ----------
    private static CiRule find(ItemStack stack) {
        if (Config.ciRules.isEmpty()) return null;
        String id = Registries.ITEM.getId(stack.getItem()).toString();
        for (CiRule r : Config.ciRules) if (r.enabled && r.item.equals(id)) return r;
        return null;
    }

    /** true, если предмет нарисован нашей моделью (ванильный рендер тогда отменяется). */
    public static boolean renderHeld(ItemStack stack, ItemDisplayContext ctx, MatrixStack m, OrderedRenderCommandQueue queue) {
        if (!Config.customItems || !ctx.isFirstPerson()) return false;
        CiRule r = find(stack);
        if (r == null) return false;
        int mi = Math.max(0, Math.min(r.model, FILES.length - 1));
        Model mod = model(mi);
        if (!mod.loaded) return false;
        final int[] col = colors(mod, r);

        m.push();
        if (ctx.isLeftHand()) m.scale(-1f, 1f, 1f);                        // зеркалим для левой руки
        m.translate(r.x / 100.0, r.y / 100.0, -r.z / 100.0);
        m.multiply(new Quaternionf().rotationXYZ(
                (float) Math.toRadians(r.rx), (float) Math.toRadians(r.ry), (float) Math.toRadians(r.rz)));
        float s = (float) r.scale * SIZE[mi];
        m.scale(s, s, s);
        queue.submitCustom(m, RenderLayers.debugQuads(), (entry, vc) -> emit(mod, col, entry.getPositionMatrix(), vc));
        m.pop();
        return true;
    }

    private static void emit(Model md, int[] col, Matrix4f mat, VertexConsumer vc) {
        int[] idx = md.idx;
        for (int t = 0, n = 0; t < idx.length; t += 3, n++) {
            int a = idx[t], b = idx[t + 1], c = idx[t + 2];
            if (md.perTri) {
                int cc = col[n];
                tri(mat, vc, md.pos, a, cc, b, cc, c, cc);
                tri(mat, vc, md.pos, c, cc, b, cc, a, cc);
            } else {
                tri(mat, vc, md.pos, a, col[a], b, col[b], c, col[c]);
                tri(mat, vc, md.pos, c, col[c], b, col[b], a, col[a]);
            }
        }
    }

    // треугольник как вырожденный четырёхугольник, обе стороны (чтобы не зависеть от отсечения граней)
    private static void tri(Matrix4f mat, VertexConsumer vc, float[] p, int a, int ca, int b, int cb, int c, int cc) {
        vc.vertex(mat, p[a * 3], p[a * 3 + 1], p[a * 3 + 2]).color(ca);
        vc.vertex(mat, p[b * 3], p[b * 3 + 1], p[b * 3 + 2]).color(cb);
        vc.vertex(mat, p[c * 3], p[c * 3 + 1], p[c * 3 + 2]).color(cc);
        vc.vertex(mat, p[c * 3], p[c * 3 + 1], p[c * 3 + 2]).color(cc);
    }
}
