package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AltManagerScreen extends Screen {
    private final Screen parent;
    private String inputName = "";
    private boolean typing = false;

    public AltManagerScreen(Screen parent) {
        super(Text.literal("Alt Manager"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0x90000000);
        int panelW = 250, panelH = 220;
        int px = (width - panelW) / 2, py = (height - panelH) / 2;
        
        g.fill(px, py, px + panelW, py + panelH, 0xFF1A1A1A);
        g.fill(px, py, px + panelW, py + 2, 0xFF7CE38B);
        g.drawCenteredTextWithShadow(textRenderer, "Alt Manager", width / 2, py + 10, -1);
        
        int cy = py + 30;
        for (String alt : Config.alts) {
            g.drawTextWithShadow(textRenderer, alt, px + 10, cy, 0xFFAAAAAA);
            boolean hov = mouseX >= px + panelW - 60 && mouseX <= px + panelW - 10 && mouseY >= cy - 2 && mouseY <= cy + 10;
            g.fill(px + panelW - 60, cy - 2, px + panelW - 10, cy + 10, hov ? 0xFF7CE38B : 0xFF333333);
            g.drawTextWithShadow(textRenderer, "Login", px + panelW - 50, cy, -1);
            
            if (hov && GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), 0) == GLFW.GLFW_PRESS) {
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(Text.literal("§cСмена ника в разработке!"), true);
                }
            }
            cy += 15;
        }
        
        int inputY = py + panelH - 40;
        g.fill(px + 10, inputY, px + panelW - 50, inputY + 20, 0xFF333333);
        g.drawTextWithShadow(textRenderer, inputName + (typing ? "_" : ""), px + 15, inputY + 6, -1);
        
        boolean hovAdd = mouseX >= px + panelW - 45 && mouseX <= px + panelW - 10 && mouseY >= inputY && mouseY <= inputY + 20;
        g.fill(px + panelW - 45, inputY, px + panelW - 10, inputY + 20, hovAdd ? 0xFF7CE38B : 0xFF555555);
        g.drawCenteredTextWithShadow(textRenderer, "Add", px + panelW - 27, inputY + 6, -1);
        
        if (GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), 0) == GLFW.GLFW_PRESS) {
            if (hovAdd && !inputName.isEmpty() && !Config.alts.contains(inputName)) {
                Config.alts.add(inputName);
                inputName = "";
                Config.save();
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (typing) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) { 
                if (!inputName.isEmpty()) inputName = inputName.substring(0, inputName.length() - 1); 
                return true; 
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) { 
                if (!inputName.isEmpty() && !Config.alts.contains(inputName)) { 
                    Config.alts.add(inputName); 
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
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelW = 250, panelH = 220;
        int px = (width - panelW) / 2, py = (height - panelH) / 2;
        int inputY = py + panelH - 40;
        if (mouseX >= px + 10 && mouseX <= px + panelW - 50 && mouseY >= inputY && mouseY <= inputY + 20) {
            typing = true; 
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
