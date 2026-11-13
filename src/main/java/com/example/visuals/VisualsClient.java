package com.example.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_465;
import org.lwjgl.glfw.GLFW;

public class VisualsClient implements ClientModInitializer {
    public static class_304 menuKey;
    public static class_304 zoomKey;
    public static class_304 freelookKey;
    public static class_304 swapSelectKey;
    public static class_304 bindSelectKey;
    public static class_304 highlightKey;

    @Override
    public void onInitializeClient() {
        ModuleDefaults.capture();
        Config.load();
        ProfileManager.startup();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (ProfileManager.autoOnJoin()) client.execute(ProfileManager::applyAuto);
        });
        AltManagerInit.register();
        FuntimeHelper.register();
        TargetEsp.register();
        Particles.register();
        DamageFever.register();
        Combat.register();
        Waypoints.load();
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) ->
                TooltipsPlus.add(stack, type != null && type.method_8035(), lines));
        TargetHud.register();
        Theme.THEME_ID = Config.menuTheme;
        Theme.apply();

        // В 1.21.9+ категория - объект, а не строка
        class_304.class_11900 category = class_304.class_11900.method_74698(class_2960.method_60655("visualsmod", "main"));

        menuKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.visualsmod.menu",
                class_3675.class_307.field_1668,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category
        ));

        zoomKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.visualsmod.zoom",
                class_3675.class_307.field_1668,
                GLFW.GLFW_KEY_C,
                category
        ));

        freelookKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.visualsmod.freelook",
                class_3675.class_307.field_1668,
                GLFW.GLFW_KEY_LEFT_ALT,
                category
        ));

        swapSelectKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.visualsmod.swap_select",
                class_3675.class_307.field_1668,
                GLFW.GLFW_KEY_V,
                category
        ));

        bindSelectKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.visualsmod.bind_select",
                class_3675.class_307.field_1668,
                GLFW.GLFW_KEY_B,
                category
        ));

        highlightKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.visualsmod.highlight_select",
                class_3675.class_307.field_1668,
                GLFW.GLFW_KEY_H,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.method_1436()) {
                client.method_1507(new MenuScreen());
            }
            Features.tick(client);
            DamageFever.tick(client);
            Combat.tick(client);
            GhostFx.tick(client);
            WorldEsp.tick(client);
            Waypoints.tick(client);
            JumpCircle.tick(client);
            TargetHud.tick(client);
            HandView.tick(client);
            ItemFeatures.tick(client);
            TrailRenderer.tick();
            FuntimeHelper.tick();
            Macros.poll();
            RadialMenu.poll();
            ItemHighlight.tick(client);
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            Features.hud(drawContext, tickCounter);
            FuntimeHelper.hud(drawContext);
            DamageFever.hud(drawContext);
            CombatFx.hud(drawContext);
            Waypoints.hud(drawContext);
            Nametags.hud(drawContext);
            TargetHud.hud(drawContext);
            CooldownHud.hud(drawContext);
            Macros.poll();   // и каждый кадр: тапы не теряются
            RadialMenu.poll();
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof class_465) {
                ScreenEvents.afterRender(screen).register((s, drawContext, mouseX, mouseY, tickDelta) ->
                        ItemFeatures.drawPanel(drawContext, client));
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("visuals")
                    .executes(ctx -> {
                        class_310 mc = ctx.getSource().getClient();
                        mc.execute(() -> mc.method_1507(new MenuScreen()));
                        return 1;
                    })
            );
            dispatcher.register(ClientCommandManager.literal("waypoint")
                    .then(ClientCommandManager.literal("add")
                            .executes(ctx -> {
                                Waypoints.addHere(ctx.getSource().getClient(), null);
                                ctx.getSource().sendFeedback(net.minecraft.class_2561.method_43470("Waypoint added"));
                                return 1;
                            })
                            .then(ClientCommandManager.argument("name", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                                    .executes(ctx -> {
                                        Waypoints.addHere(ctx.getSource().getClient(),
                                                com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "name"));
                                        ctx.getSource().sendFeedback(net.minecraft.class_2561.method_43470("Waypoint added"));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("remove")
                            .then(ClientCommandManager.argument("name", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                                    .executes(ctx -> {
                                        boolean ok = Waypoints.remove(com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "name"));
                                        ctx.getSource().sendFeedback(net.minecraft.class_2561.method_43470(ok ? "Waypoint removed" : "Waypoint not found"));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("clear")
                            .executes(ctx -> {
                                Waypoints.clear();
                                ctx.getSource().sendFeedback(net.minecraft.class_2561.method_43470("Waypoints cleared"));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("list")
                            .executes(ctx -> {
                                if (Waypoints.LIST.isEmpty()) ctx.getSource().sendFeedback(net.minecraft.class_2561.method_43470("No waypoints"));
                                for (Waypoints.Wp w : Waypoints.LIST)
                                    ctx.getSource().sendFeedback(net.minecraft.class_2561.method_43470(w.name + ": " + w.x + " " + w.y + " " + w.z));
                                return 1;
                            }))
            );
            dispatcher.register(ClientCommandManager.literal("radial")
                    .executes(ctx -> {
                        class_310 mc = ctx.getSource().getClient();
                        mc.execute(() -> mc.method_1507(new RadialMenuScreen()));
                        return 1;
                    })
            );
            dispatcher.register(ClientCommandManager.literal("freelook")
                    .executes(ctx -> {
                        class_310 mc = ctx.getSource().getClient();
                        mc.execute(Freelook::toggleLatch);
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
