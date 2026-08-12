package org.polyfrost.vanillahud.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;

//? if >=1.21.6 {
import com.mojang.blaze3d.pipeline.RenderPipeline;
//?}
//? if >=1.21.2 <1.21.6 {
/*import net.minecraft.client.renderer.RenderType;
import java.util.function.Function;
*///?}

@Mixin(GuiGraphicsExtractor.class)
public class UprightIconMixin {
    @WrapMethod(method = "blitSprite(" +
            //? if >=1.21.6 {
            "Lcom/mojang/blaze3d/pipeline/RenderPipeline;" +
            //?} else if >=1.21.2 {
            /*"Ljava/util/function/Function;" +
            *///?}
            "Lnet/minecraft/resources/Identifier;IIII)V")
    private void vanillahud$uprightIcon(
            //? if >=1.21.6 {
            RenderPipeline pipeline,
            //?} else if >=1.21.2 {
            /*Function<Identifier, RenderType> pipeline,
            *///?}
            Identifier sprite, int x, int y, int width, int height, Operation<Void> original) {
        GuiGraphicsExtractor self = (GuiGraphicsExtractor) (Object) this;
        boolean rotated = HudTransform.uprightIcon(self, x, y, width, height);
        original.call(
                //? if >=1.21.2 {
                pipeline,
                //?}
                sprite, x, y, width, height);
        if (rotated) HudTransform.endUpright(self);
    }
}
