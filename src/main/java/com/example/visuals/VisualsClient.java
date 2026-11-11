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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class VisualsClient implements ClientModInitializer {
    public static KeyBinding menuKey;
    public static KeyBinding zoomKey;
    public static KeyBinding freelookKey;
    public static KeyBinding swapSelectKey;
    public static KeyBinding bindSelectKey;
    public static KeyBinding highlightKey;

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
                TooltipsPlus.add(stack, type != null && type.isAdvanced(), lines));
        TargetHud.register();
        Theme.THEME_ID = Config.menuTheme;
        Theme.apply();

        // В 1.21.9+ категория - объект, а не строка
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("visualsmod", "main"));

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

        freelookKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visualsmod.freelook",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
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

        highlightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visualsmod.highlight_select",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                client.setScreen(new MenuScreen());
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
            Macros.poll();   // и каждый кадр: тапы не теряются
        });

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
            dispatcher.register(ClientCommandManager.literal("waypoint")
                    .then(ClientCommandManager.literal("add")
                            .executes(ctx -> {
                                Waypoints.addHere(ctx.getSource().getClient(), null);
                                ctx.getSource().sendFeedback(net.minecraft.text.Text.literal("Waypoint added"));
                                return 1;
                            })
                            .then(ClientCommandManager.argument("name", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                                    .executes(ctx -> {
                                        Waypoints.addHere(ctx.getSource().getClient(),
                                                com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "name"));
                                        ctx.getSource().sendFeedback(net.minecraft.text.Text.literal("Waypoint added"));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("remove")
                            .then(ClientCommandManager.argument("name", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                                    .executes(ctx -> {
                                        boolean ok = Waypoints.remove(com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "name"));
                                        ctx.getSource().sendFeedback(net.minecraft.text.Text.literal(ok ? "Waypoint removed" : "Waypoint not found"));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("clear")
                            .executes(ctx -> {
                                Waypoints.clear();
                                ctx.getSource().sendFeedback(net.minecraft.text.Text.literal("Waypoints cleared"));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("list")
                            .executes(ctx -> {
                                if (Waypoints.LIST.isEmpty()) ctx.getSource().sendFeedback(net.minecraft.text.Text.literal("No waypoints"));
                                for (Waypoints.Wp w : Waypoints.LIST)
                                    ctx.getSource().sendFeedback(net.minecraft.text.Text.literal(w.name + ": " + w.x + " " + w.y + " " + w.z));
                                return 1;
                            }))
            );
            dispatcher.register(ClientCommandManager.literal("freelook")
                    .executes(ctx -> {
                        MinecraftClient mc = ctx.getSource().getClient();
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
