package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
//import net.minecraft.client.gui.Gui;
//?}
//? if >=1.21.6 {
import net.minecraft.client.gui.contextualbar.ContextualBar;
//?}

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
//@Mixin(Gui.class)
//?}
public class GuiMixinExperienceBar {
    //? if <=1.21.5 {
    /*@WrapMethod(method = "renderExperienceBar")
    private void vanillahud$xpBar(GuiGraphicsExtractor graphics, int xpBarX, Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceBar().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getExperienceBar());
        original.call(graphics, xpBarX);
        HudTransform.end(graphics);
    }
    *///?}

    //? if >=1.21.6 && <26 {
    /*@WrapOperation(
            method = "renderHotbarAndDecorations",
            at = {
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;renderBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
                    ),
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;render(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
                    )
            }
    )
    private void vanillahud$xpBar(ContextualBar instance, GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker,
                                  Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceBar().shouldRender()) return;

        HudTransform.begin(guiGraphics, Huds.INSTANCE.getExperienceBar());
        original.call(instance, guiGraphics, deltaTracker);
        HudTransform.end(guiGraphics);
    }
    *///?}

    //? if >=26 {
    @WrapOperation(
            method = "extractHotbarAndDecorations",
            at = {
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
                    ),
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
                    )
            }
    )
    private void vanillahud$xpBar(ContextualBar instance, GuiGraphicsExtractor graphics, DeltaTracker delta,
                                  Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceBar().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getExperienceBar());
        original.call(instance, graphics, delta);
        HudTransform.end(graphics);
    }
    //?}
}
