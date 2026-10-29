package com.example.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class VisualsClient implements ClientModInitializer {
    public static KeyBinding menuKey;
    public static KeyBinding zoomKey;
    public static KeyBinding swapSelectKey;
    public static KeyBinding bindSelectKey;

    @Override
    public void onInitializeClient() {
        Config.load();
        Theme.apply();

        // Категория передаётся строкой, как в старых версиях Yarn
        String category = "key.category.visualsmod.main";

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visualsmod.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category
        ));

        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visualsmod.zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                category
        ));

        swapSelectKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visualsmod.swap_select",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                category
        ));

        bindSelectKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visualsmod.bind_select",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                client.setScreen(new MenuScreen());
            }
            Features.tick(client);
            ItemFeatures.tick(client);
            TrailRenderer.tick();
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> Features.hud(drawContext, tickCounter));

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof HandledScreen) {
                ScreenEvents.afterRender(screen).register((s, drawContext, mouseX, mouseY, tickDelta) ->
                        ItemFeatures.drawPanel(drawContext, client));
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("visuals")
                    .executes(ctx -> {
                        MinecraftClient mc = ctx.getSource().getClient();
                        mc.execute(() -> mc.setScreen(new MenuScreen()));
                        return 1;
                    })
            );
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            Features.restore(client);
            Config.save();
        });
    }
}
