package com.example.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_490;

public class VisualsClient implements ClientModInitializer {
    public static class_304 menuKey;
    public static class_304 zoomKey;
    public static class_304 swapSelectKey;
    public static class_304 bindSelectKey;

    @Override
    public void onInitializeClient() {
        Config.load();
        Theme.apply();

        class_304.class_11900 category = class_304.class_11900.method_74698(class_2960.method_60655("visualsmod", "main"));
        menuKey = KeyBindingHelper.registerKeyBinding(new class_304("key.visualsmod.menu", class_3675.class_307.field_1668, 344, category));
        zoomKey = KeyBindingHelper.registerKeyBinding(new class_304("key.visualsmod.zoom", class_3675.class_307.field_1668, 67, category));
        swapSelectKey = KeyBindingHelper.registerKeyBinding(new class_304("key.visualsmod.swap_select", class_3675.class_307.field_1668, 86, category));
        bindSelectKey = KeyBindingHelper.registerKeyBinding(new class_304("key.visualsmod.bind_select", class_3675.class_307.field_1668, 66, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.method_1436()) client.method_1507(new MenuScreen());
            Features.tick(client);
            ItemFeatures.tick(client);
            TrailRenderer.tick();
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> Features.hud(drawContext, tickCounter));

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof class_490) {
                ScreenEvents.afterRender(screen).register((s, drawContext, mouseX, mouseY, tickDelta) ->
                        ItemFeatures.drawPanel(drawContext, client));
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("visuals")
                    .executes(ctx -> {
                        class_310 mc = ctx.getSource().getClient();
                        mc.method_63588(() -> mc.method_1507(new MenuScreen()));
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
