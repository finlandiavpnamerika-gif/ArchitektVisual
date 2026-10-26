package com.example.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class MenuScreen extends Screen {
    private static final int BG = 0xFF101010;
    private static final int SIDEBAR = 0xFF1A1A1A;
    private static final int ACCENT = 0xFF7CE38B; // Зелёный акцент
    private static final int TEXT = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFF888888;
    private static final int MODULE_BG = 0xFF202020;
    private static final int MODULE_HOVER = 0xFF2A2A2A;

    private String activeCategory = "Visuals";
    private int mx, my;
    private boolean down, clicked, prevDown = true;
    private int cx, cy;

    public MenuScreen() {
        super(Text.literal("ArchitektVisuals"));
    }

    private boolean hit(int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public void render(DrawContext g, int mouseX, int mouseY, float delta) {
        mx = mouseX; my = mouseY;
        long window = client.getWindow().getHandle();
        down = GLFW.glfwGetMouseButton(window, 0) == GLFW.GLFW_PRESS;
        clicked = down && !prevDown;
        prevDown = down;

        // Фон
        g.fill(0, 0, width, height, 0x90000000);

        int panelW = 400;
        int panelH = 300;
        int px = (width - panelW) / 2;
        int py = (height - panelH) / 2;

        // Основное окно
        g.fill(px, py, px + panelW, py + panelH, BG);
        g.fill(px, py, px + panelW, py + 2, ACCENT); // Верхняя полоса

        // Заголовок
        g.drawTextWithShadow(textRenderer, "§lArchitektVisuals", px + 10, py + 8, TEXT);

        // Левая панель (Sidebar)
        int sidebarW = 100;
        g.fill(px, py + 20, px + sidebarW, py + panelH, SIDEBAR);
        g.fill(px + sidebarW, py + 20, px + sidebarW + 1, py + panelH, 0xFF333333); // Разделитель

        // Категории
        String[] cats = {"Visuals", "HUD", "Social", "Client"};
        int catY = py + 30;
        for (String cat : cats) {
            boolean isActive = cat.equals(activeCategory);
            boolean hov = hit(px, catY, sidebarW, 20);
            
            if (isActive) g.fill(px, catY, px + sidebarW, catY + 20, 0xFF2A2A2A);
            else if (hov) g.fill(px, catY, px + sidebarW, catY + 20, 0xFF222222);

            g.drawTextWithShadow(textRenderer, cat, px + 10, catY + 6, isActive ? ACCENT : TEXT_DIM);

            if (clicked && hov) activeCategory = cat;
            catY += 22;
        }

        // Правая панель (Модули)
        int modX = px + sidebarW + 10;
        int modY = py + 30;

        if (activeCategory.equals("Visuals")) {
            // Прицел
            modY = drawModule(g, "Свой прицел", modX, modY, Config.customCrosshair, () -> Config.customCrosshair = !Config.customCrosshair);
            // Трейлы
            modY = drawModule(g, "Трейлы", modX, modY, Config.trails, () -> Config.trails = !Config.trails);
            // Время
            modY = drawModule(g, "Время", modX, modY, Config.customTime, () -> Config.customTime = !Config.customTime);
            // Погода
            modY = drawModule(g, "Погода", modX, modY, Config.customWeather, () -> Config.customWeather = !Config.customWeather);
        } 
        else if (activeCategory.equals("HUD")) {
            modY = drawModule(g, "Info HUD", modX, modY, Config.hud, () -> Config.hud = !Config.hud);
            modY = drawModule(g, "ArrayList", modX, modY, Config.arrayList, () -> Config.arrayList = !Config.arrayList);
            modY = drawModule(g, "Keystrokes", modX, modY, Config.keystrokes, () -> Config.keystrokes = !Config.keystrokes);
            modY = drawModule(g, "Armor", modX, modY, Config.armorHud, () -> Config.armorHud = !Config.armorHud);
        }
        else if (activeCategory.equals("Social")) {
            // Кнопка открытия друзей
            boolean hov = hit(modX, modY, 200, 20);
            g.fill(modX, modY, modX + 200, modY + 20, hov ? MODULE_HOVER : MODULE_BG);
            g.drawTextWithShadow(textRenderer, "-> Открыть Friends", modX + 10, modY + 6, TEXT);
            if (clicked && hov) client.setScreen(new FriendsScreen(this));
            modY += 25;
        }

        // Кнопка закрытия
        int bw = 100, bh = 20;
        int bx = px + panelW - bw - 10;
        int by = py + panelH - bh - 10;
        boolean hovClose = hit(bx, by, bw, bh);
        g.fill(bx, by, bx + bw, by + bh, hovClose ? ACCENT : 0xFF333333);
        g.drawCenteredTextWithShadow(textRenderer, "Закрыть", bx + bw / 2, by + 6, 0xFF000000);
        if (clicked && hovClose) close();
    }

    private int drawModule(DrawContext g, String label, int x, int y, boolean enabled, Runnable toggle) {
        int w = 200;
        int h = 36;
        boolean hov = hit(x, y, w, h);
        
        g.fill(x, y, x + w, y + h, hov ? MODULE_HOVER : MODULE_BG);
        g.fill(x, y, x + 2, y + h, enabled ? ACCENT : 0xFF555555); // Полоска слева
        
        g.drawTextWithShadow(textRenderer, label, x + 10, y + 6, enabled ? TEXT : TEXT_DIM);
        g.drawTextWithShadow(textRenderer, enabled ? "ВКЛ" : "ВЫКЛ", x + 10, y + 20, enabled ? ACCENT : TEXT_DIM);

        if (clicked && hov) toggle.run();
        return y + h + 5;
    }

    @Override
    public void removed() { Config.save(); }
}
