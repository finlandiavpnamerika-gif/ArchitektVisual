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
import net.minecraft.util.Identifier;

public class VisualsClient implements ClientModInitializer {
    public static KeyBinding menuKey;
    public static KeyBinding zoomKey;
    public static KeyBinding swapSelectKey;
    public static KeyBinding bindSelectKey;

    @Override
    public void onInitializeClient() {
        Config.load();

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("visualsmod", "main"));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsmod.menu", InputUtil.Type.KEYSYM, 344, category));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsmod.zoom", InputUtil.Type.KEYSYM, 67, category));
        swapSelectKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsmod.swap_select", InputUtil.Type.KEYSYM, 86, category));
        bindSelectKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsmod.bind_select", InputUtil.Type.KEYSYM, 66, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                client.setScreen(new MenuScreen());
            }
            Features.tick(client);
            ItemFeatures.tick(client);
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> Features.hud(drawContext, tickCounter));

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof InventoryScreen) {
                ScreenEvents.afterRender(screen).register((s, drawContext, mouseX, mouseY, tickDelta) ->
                        ItemFeatures.drawPanel(drawContext, client));
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // Меню
            dispatcher.register(ClientCommandManager.literal("visuals")
                    .executes(ctx -> {
                        MinecraftClient mc = ctx.getSource().getClient();
                        mc.send(() -> mc.setScreen(new MenuScreen()));
                        return 1;
                    })
            );

            // Друзья (без точки)
            dispatcher.register(ClientCommandManager.literal("friends")
                    .then(ClientCommandManager.literal("add")
                            .then(ClientCommandManager.argument("name", com.mojang.brigadier.arguments.StringArgumentType.word())
                                    .executes(ctx -> {
                                        String name = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "name");
                                        if (!Config.friends.contains(name)) {
                                            Config.friends.add(name);
                                            Config.save();
                                            ctx.getSource().sendFeedback(net.minecraft.text.Text.literal("§aДруг " + name + " добавлен!"));
                                        } else {
                                            ctx.getSource().sendFeedback(net.minecraft.text.Text.literal("§cЭтот игрок уже в друзьях!"));
                                        }
                                        return 1;
                                    })
                            )
                    )
                    .then(ClientCommandManager.literal("remove")
                            .then(ClientCommandManager.argument("name", com.mojang.brigadier.arguments.StringArgumentType.word())
                                    .executes(ctx -> {
                                        String name = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "name");
                                        if (Config.friends.remove(name)) {
                                            Config.save();
                                            ctx.getSource().sendFeedback(net.minecraft.text.Text.literal("§aДруг " + name + " удален!"));
                                        } else {
                                            ctx.getSource().sendFeedback(net.minecraft.text.Text.literal("§cЭтого игрока нет в друзьях!"));
                                        }
                                        return 1;
                                    })
                            )
                    )
                    .then(ClientCommandManager.literal("clear")
                            .executes(ctx -> {
                                Config.friends.clear();
                                Config.save();
                                ctx.getSource().sendFeedback(net.minecraft.text.Text.literal("§aСписок друзей очищен!"));
                                return 1;
                            })
                    )
            );
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            Features.restore(client);
            Config.save();
        });
    }
}
