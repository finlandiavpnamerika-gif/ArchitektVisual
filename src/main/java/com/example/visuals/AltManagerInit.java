package com.example.visuals;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class AltManagerInit {
    private AltManagerInit() {}

    /** Вызвать один раз из VisualsClient#onInitializeClient. */
    public static void register() {
        AltManager.load();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            boolean multiplayer = screen instanceof MultiplayerScreen;
            // на кастомном главном меню ванильные кнопки не рисуются, поэтому там кнопку не добавляем
            boolean vanillaTitle = screen instanceof TitleScreen && !Config.customMainMenu;
            if (multiplayer || vanillaTitle) {
                Screens.getButtons(screen).add(
                        ButtonWidget.builder(Text.literal("Alts"),
                                        b -> client.setScreen(new AltManagerScreen(screen)))
                                .dimensions(5, 5, 60, 20)
                                .build());
            }
        });
    }
                                                                                   }
