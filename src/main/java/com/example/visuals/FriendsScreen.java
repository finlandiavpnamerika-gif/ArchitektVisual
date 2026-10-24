package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class FriendsScreen extends Screen {
    private final Screen parent;
    private String inputName = "";
    private boolean typing = false;
    private int cx, cy;

    public FriendsScreen(Screen parent) {
        super(Text.literal("Friends"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0x90000000);
        
        int panelW = 250;
        int panelH = 220;
        int px = (width - panelW) / 2;
        int py = (height - panelH) / 2;
        
        // Фон
        g.fill(px, py, px + panelW, py + panelH, 0xFF1A1A1A);
        g.fill(px, py, px + panelW, py + 2, 0xFF7CE38B);
        
        g.drawCenteredTextWithShadow(textRenderer, "Friends Manager", width / 2, py + 10, -1);
        
        // Список друзей
        cy = py + 30;
        cx = px + 10;
        for (String f : Config.friends) {
            g.drawTextWithShadow(textRenderer, f, cx, cy, 0xFF7CE38B);
            cy += 12;
        }
        
        // Поле ввода (внизу)
        int inputY = py + panelH - 40;
        g.fill(px + 10, inputY, px + panelW - 50, inputY + 20, 0xFF333333);
        g.drawTextWithShadow(textRenderer, inputName + (typing ? "_" : ""), px + 15, inputY + 6, -1);
        
        // Кнопка "Добавить"
        boolean hov = mouseX >= px + panelW - 45 && mouseX <= px + panelW - 10 && mouseY >= inputY && mouseY <= inputY + 20;
        g.fill(px + panelW - 45, inputY, px + panelW - 10, inputY + 20, hov ? 0xFF7CE38B : 0xFF555555);
        g.drawCenteredTextWithShadow(textRenderer, "Add", px + panelW - 27, inputY + 6, -1);
        
        // Кнопка "Назад"
        g.drawCenteredTextWithShadow(textRenderer, "Press ESC to go back", width / 2, py + panelH - 15, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelW = 250;
        int panelH = 220;
        int px = (width - panelW) / 2;
        int py = (height - panelH) / 2;
        int inputY = py + panelH - 40;

        // Клик по полю ввода
        if (mouseX >= px + 10 && mouseX <= px + panelW - 50 && mouseY >= inputY && mouseY <= inputY + 20) {
            typing = true;
            return true;
        }
        // Клик по кнопке Add
        if (mouseX >= px + panelW - 45 && mouseX <= px + panelW - 10 && mouseY >= inputY && mouseY <= inputY + 20) {
            if (!inputName.isEmpty() && !Config.friends.contains(inputName)) {
                Config.friends.add(inputName);
                inputName = "";
                Config.save();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (typing) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!inputName.isEmpty()) inputName = inputName.substring(0, inputName.length() - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                if (!inputName.isEmpty() && !Config.friends.contains(inputName)) {
                    Config.friends.add(inputName);
                    inputName = "";
                    Config.save();
                }
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            MinecraftClient.getInstance().setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (typing && inputName.length() < 16) {
            inputName += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }
}
