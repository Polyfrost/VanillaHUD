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
        if (rotated) HudTransform.endUprightIcon(self);
    }

    //? if >=1.21.2 {
    //~ if >=1.21.6 'Ljava/util/function/Function;' -> 'Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;'
    @WrapMethod(method = "innerBlit(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIIFFFFI)V")
    private void vanillahud$mirrorQuad(
            //~ if >=1.21.6 'Function<?, RenderType>' -> 'RenderPipeline'
            RenderPipeline pipeline,
            Identifier location, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color,
            Operation<Void> original) {
        int dy = HudTransform.mirrorShift(y0, y1);
        original.call(pipeline, location, x0, x1, y0 + dy, y1 + dy, u0, u1, v0, v1, color);
    }
    //?} else {
    /*@WrapMethod(method = "innerBlit(Lnet/minecraft/resources/Identifier;IIIIIFFFF)V")
    private void vanillahud$mirrorQuad(Identifier location, int x0, int x1, int y0, int y1, int z,
                                       float u0, float u1, float v0, float v1, Operation<Void> original) {
        int dy = HudTransform.mirrorShift(y0, y1);
        original.call(location, x0, x1, y0 + dy, y1 + dy, z, u0, u1, v0, v1);
    }

    @WrapMethod(method = "innerBlit(Lnet/minecraft/resources/Identifier;IIIIIFFFFFFFF)V")
    private void vanillahud$mirrorTintedQuad(Identifier location, int x0, int x1, int y0, int y1, int z,
                                             float u0, float u1, float v0, float v1,
                                             float r, float g, float b, float a, Operation<Void> original) {
        int dy = HudTransform.mirrorShift(y0, y1);
        original.call(location, x0, x1, y0 + dy, y1 + dy, z, u0, u1, v0, v1, r, g, b, a);
    }
    *///?}
}
//?} else {
/*// 1.8.9 blits every HUD sprite through GameGui.drawTexture, so rotated elements keep their
// icons upright from there. HudTransform only acts while an icon layer is open, which is just
// the status bar pass, so the hotbar frame and the experience bar are left alone unless the
// hotbar is mirrored.
//
// Argentum batches these blits and draws them later, losing any GL rotation around them,
// so rotated icons are turned upright in LegacyIconBatch instead.
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.gui.GuiElement;
import org.polyfrost.vanillahud.compat.LegacyIconBatch;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameGui.class)
public abstract class UprightIconMixin extends GuiElement {
    @WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GameGui;drawTexture(IIIIII)V"))
    private void vanillahud$uprightIcon(GameGui self, int x, int y, int u, int v, int width, int height,
                                        Operation<Void> original) {
        y += HudTransform.mirrorShift(y, y + height);
        int turns = HudTransform.getIconTurns();
        if (turns == 0) {
            original.call(self, x, y, u, v, width, height);
            return;
        }
        LegacyIconBatch.quad(x, y, u, v, width, height, width, height, 256, 256, this.drawOffset, turns);
    }

    @WrapOperation(method = {"renderHotbar", "renderXpBar", "renderJumpBar"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GameGui;drawTexture(IIIIII)V"))
    private void vanillahud$mirrorQuad(GameGui self, int x, int y, int u, int v, int width, int height,
                                       Operation<Void> original) {
        original.call(self, x, y + HudTransform.mirrorShift(y, y + height), u, v, width, height);
    }
}
*///?}
