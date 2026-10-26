package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;

public class FriendGlow {
    public static void tick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            // Пропускаем себя
            if (player == mc.player) continue;

            boolean isFriend = Config.friends.contains(player.getName().getString());
            
            if (isFriend && Config.friendGlow) {
                // Проверяем, находится ли игрок в прямой видимости
                boolean isVisible = mc.player.canSee(player);
                
                // Если друг виден — включаем свечение (Outline), иначе выключаем
                if (isVisible) {
                    // Выдаем эффект свечения на 1 секунду (20 тиков), чтобы он обновлялся
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.GLOWING, 
                            20, // Длительность (1 сек)
                            0,  // Уровень
                            false, // Ambient (нежно)
                            false, // Show Particles
                            false  // Show Icon
                    ));
                } else {
                    // Если не виден — убираем свечение (если оно не от зелья)
                    StatusEffectInstance effect = player.getStatusEffect(StatusEffects.GLOWING);
                    if (effect != null && effect.getDuration() <= 20) {
                        player.removeStatusEffect(StatusEffects.GLOWING);
                    }
                }
            } else {
                // Если это не друг, убираем свечение, если оно было выдано нами
                StatusEffectInstance effect = player.getStatusEffect(StatusEffects.GLOWING);
                if (effect != null && effect.getDuration() <= 20) {
                    player.removeStatusEffect(StatusEffects.GLOWING);
                }
            }
        }
    }
}
