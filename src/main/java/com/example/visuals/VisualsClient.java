package com.example.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class VisualsClient implements ClientModInitializer {
    public static KeyBinding menuKey;
    public static KeyBinding zoomKey;
    public static KeyBinding swapSelectKey;
    public static KeyBinding bindSelectKey;

    @Override
    public void onInitializeClient() {
        Config.load();

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visualsmod.menu", InputUtil.Type.KEYSYM, 344, "Visuals"));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visualsmod.zoom", InputUtil.Type.KEYSYM, 67, "Visuals"));
        swapSelectKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visualsmod.swap_select", InputUtil.Type.KEYSYM, 86, "Visuals"));
        bindSelectKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visualsmod.bind_select", InputUtil.Type.KEYSYM, 66, "Visuals"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                client.setScreen(new MenuScreen());
            }
            Features.tick(client);
            ItemFeatures.tick(client);
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) ->
                Features.hud(drawContext, tickCounter));

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof InventoryScreen) {
                ScreenEvents.afterRender(screen).register((s, drawContext, mouseX, mouseY, tickDelta) ->
                        ItemFeatures.drawPanel(drawContext, client));
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("visuals").executes(ctx -> {
                    MinecraftClient mc = ctx.getSource().getClient();
                    mc.send(() -> mc.setScreen(new MenuScreen()));
                    return 1;
                })));

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            Features.restore(client);
            Config.save();
        });
    }
}
