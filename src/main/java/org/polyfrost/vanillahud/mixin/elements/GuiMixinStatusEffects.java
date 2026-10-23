package org.polyfrost.vanillahud.mixin.elements;

//? if > 1.8.9 {
import org.spongepowered.asm.mixin.Mixin;

//? if <26 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
*///?}
//? if >=26 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.hud.StatusEffectsHud;
import org.spongepowered.asm.mixin.injection.At;
//?}

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} elif > 1.8.9 {
/*import net.minecraft.client.gui.Gui;
*///?} else {
/*import net.minecraft.client.gui.GameGui;
*///?}

//? if >=26.2 {
@Mixin(Hud.class)
//?} elif > 1.8.9 {
/*@Mixin(Gui.class)
*///?} else {
/*@Mixin(GameGui.class)
*///?}
public class GuiMixinStatusEffects {
    //? if <26 {
    /*@WrapMethod(method = "renderEffects")
    private void vanillahud$statusEffects(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (!Huds.INSTANCE.getStatusEffects().shouldDraw()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getStatusEffects());
        original.call(graphics, deltaTracker);
        HudTransform.end(graphics);
    }
    *///?} else {
    @WrapOperation(
            method = "extractEffects",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V")
    )
    private void vanillahud$shiftEffectBackground(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> original) {
        StatusEffectsHud hud = Huds.INSTANCE.getStatusEffects();
        if (!hud.shouldDraw()) {
            original.call(graphics, pipeline, sprite, x, y, width, height);
            return;
        }

        original.call(graphics, pipeline, sprite, x + hud.rowShiftX(y), y + hud.rowShiftY(y), width, height);
    }

    @WrapOperation(
            method = "extractEffects",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V")
    )
    private void vanillahud$shiftEffectIcon(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, int color, Operation<Void> original) {
        StatusEffectsHud hud = Huds.INSTANCE.getStatusEffects();
        if (!hud.shouldDraw()) {
            original.call(graphics, pipeline, sprite, x, y, width, height, color);
            return;
        }

        original.call(graphics, pipeline, sprite, x + hud.rowShiftX(y), y + hud.rowShiftY(y), width, height, color);
    }
    //?}
}
//?} else {
/*// 1.8.9 has no status effect icons on the HUD
import net.minecraft.client.gui.GameGui;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameGui.class)
public class GuiMixinStatusEffects {
}
*///?}
