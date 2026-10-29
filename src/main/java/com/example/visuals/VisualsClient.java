package com.example.visuals;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;

public class VisualsClient implements ClientModInitializer {
    public static KeyMapping menuKey;
    public static KeyMapping zoomKey;
    public static KeyMapping swapSelectKey;
    public static KeyMapping bindSelectKey;

    @Override
    public void onInitializeClient() {
        Config.load();
        Theme.apply();

        // 1. Создаем категорию для клавиш (Mojang-маппинги, 1.21.11)
        KeyMapping.Category category = KeyMapping.Category.register(
                ResourceLocation.fromNamespaceAndPath("visualsmod", "main")
        );

        // 2. Регистрируем клавиши через KeyBindingHelper
        // Right Shift
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.visualsmod.menu",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_RIGHT_SHIFT,
                category
        ));

        // C
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.visualsmod.zoom",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_C,
                category
        ));

        // V
        swapSelectKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.visualsmod.swap_select",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_V,
                category
        ));

        // B
        bindSelectKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.visualsmod.bind_select",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_B,
                category
        ));

        // 3. Тик-события
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.consumeClick()) {
                client.setScreen(new MenuScreen());
            }
            Features.tick(client);
            ItemFeatures.tick(client);
            TrailRenderer.tick();
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> Features.hud(drawContext, tickCounter));

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen) {
                ScreenEvents.afterRender(screen).register((s, drawContext, mouseX, mouseY, tickDelta) ->
                        ItemFeatures.drawPanel(drawContext, client));
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("visuals")
                    .executes(ctx -> {
                        Minecraft mc = ctx.getSource().getClient();
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
