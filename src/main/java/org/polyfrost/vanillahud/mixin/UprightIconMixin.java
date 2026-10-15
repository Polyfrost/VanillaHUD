package org.polyfrost.vanillahud.mixin;

//? if > 1.8.9 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//? if > 1.8.9
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;

//? if >=1.21.6 {
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
//?}
//? if >=1.21.2 <1.21.6 {
/*import net.minecraft.client.renderer.RenderType;
import java.util.function.Function;
*///?}

@Mixin(GuiGraphicsExtractor.class)
public class UprightIconMixin {
    @WrapMethod(method = "blitSprite(" +
            //? if >=1.21.6 {
            "Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;" +
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
//?} else {
/*// 1.8.9 blits every HUD sprite through GameGui.drawTexture, so rotated elements keep their
// icons upright from there. HudTransform only acts while an icon layer is open, which is just
// the status bar pass, so the hotbar frame and the experience bar are left alone.
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.GameGui;
import org.polyfrost.vanillahud.compat.LegacyDrawContext;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameGui.class)
public class UprightIconMixin {
    @WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GameGui;drawTexture(IIIIII)V"))
    private void vanillahud$uprightIcon(GameGui self, int x, int y, int u, int v, int width, int height,
                                        Operation<Void> original) {
        boolean rotated = HudTransform.uprightIcon(LegacyDrawContext.INSTANCE, x, y, width, height);
        original.call(self, x, y, u, v, width, height);
        if (rotated) HudTransform.endUpright(LegacyDrawContext.INSTANCE);
    }
}
*///?}
