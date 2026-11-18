package com.example.visuals.mixin;

import com.example.visuals.AspectRatio;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Aspect Ratio: подменяет aspect (2-й аргумент) при построении матрицы перспективы в GameRenderer.
 *
 * Метод, где Mojang строит проекцию, переименовывали между версиями, поэтому цель выбирается по регулярке
 * (любой метод GameRenderer), а точка внедрения - вызов JOML Matrix4f.perspective / setPerspective.
 * require = 0: если в вашей версии такого вызова нет, мод не упадёт, а просто ничего не изменит.
 */
@Mixin(class_757.class)
public class AspectRatioMixin {
    @ModifyArg(method = "/.*/", index = 1, require = 0,
            at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;perspective(FFFF)Lorg/joml/Matrix4f;"))
    private float visuals$aspectA(float aspect) {
        return AspectRatio.apply(aspect);
    }

    @ModifyArg(method = "/.*/", index = 1, require = 0,
            at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;perspective(FFFFZ)Lorg/joml/Matrix4f;"))
    private float visuals$aspectB(float aspect) {
        return AspectRatio.apply(aspect);
    }

    @ModifyArg(method = "/.*/", index = 1, require = 0,
            at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;setPerspective(FFFF)Lorg/joml/Matrix4f;"))
    private float visuals$aspectC(float aspect) {
        return AspectRatio.apply(aspect);
    }

    @ModifyArg(method = "/.*/", index = 1, require = 0,
            at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;setPerspective(FFFFZ)Lorg/joml/Matrix4f;"))
    private float visuals$aspectD(float aspect) {
        return AspectRatio.apply(aspect);
    }
}
