package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Chams: подсветка сущностей сквозь стены через ванильное свечение (контур цвета команды, иначе белый).
 * Решение принимается в миксине EntityGlowMixin (Entity.isGlowing), только для клиентских сущностей.
 */
public final class Chams {
    private Chams() {}

    public static boolean want(Entity e) {
        if (!Config.chamsOn) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return false;
        // серверные копии сущностей (одиночная игра) игнорируем
        if (e != mc.player && mc.world.getEntityById(e.getId()) != e) return false;

        if (e == mc.player) return Config.chSelf && !mc.options.getPerspective().isFirstPerson();
        if (e instanceof PlayerEntity p) {
            if (!Config.chPlayers) return false;
            return Config.chFriends || !Config.friends.contains(p.getName().getString());
        }
        return e instanceof LivingEntity && Config.chMobs;
    }
}
