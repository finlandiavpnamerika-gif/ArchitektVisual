package com.example.visuals.mixin;

import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Нужен для Fullbright: setValue() обрезает яркость до 0..1, поэтому пишем значение в поле напрямую. */
@Mixin(SimpleOption.class)
public interface SimpleOptionAccessor<T> {
    @Accessor("value")
    void visuals$setRawValue(T value);
}
