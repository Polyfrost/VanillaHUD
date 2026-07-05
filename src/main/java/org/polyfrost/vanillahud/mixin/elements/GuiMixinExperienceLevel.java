package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
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
public class GuiMixinExperienceLevel {
    //? if <=1.21.5 {
    /*@WrapMethod(method = "renderExperienceLevel")
    private void vanillahud$xpLevel(GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceLevel().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getExperienceLevel());
        original.call(graphics, delta);
        HudTransform.end(graphics);
    }
    *///?}

    //? if >=1.21.6 && <26 {
    /*@WrapOperation(
            method = "renderHotbarAndDecorations",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"
            )
    )
    private void vanillahud$xpLevel(GuiGraphicsExtractor guiGraphics, Font font, int i, Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceLevel().shouldRender()) return;

        HudTransform.begin(guiGraphics, Huds.INSTANCE.getExperienceLevel());
        original.call(guiGraphics, font, i);
        HudTransform.end(guiGraphics);
    }
    *///?}

    //? if >=26 {
    @WrapOperation(
            method = "extractHotbarAndDecorations",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"
            )
    )
    private void vanillahud$xpLevel(GuiGraphicsExtractor graphics, Font font,
                                    int experienceLevel, Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceLevel().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getExperienceLevel());
        original.call(graphics, font, experienceLevel);
        HudTransform.end(graphics);
    }
    //?}
}
