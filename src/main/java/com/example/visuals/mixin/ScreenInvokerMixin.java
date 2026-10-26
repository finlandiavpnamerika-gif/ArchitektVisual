package com.example.visuals.mixin;

import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenInvokerMixin {
    @Invoker("clearChildren")
    void invokeClearChildren();
}
