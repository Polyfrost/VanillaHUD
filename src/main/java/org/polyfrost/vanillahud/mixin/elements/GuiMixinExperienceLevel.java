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
/*import net.minecraft.client.gui.Gui;
*///?}
//? if >=1.21.6 {
import net.minecraft.client.gui.contextualbar.ContextualBar;
//?}

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public class GuiMixinExperienceLevel {
    // Visibility + position handled by Fabric on 1.21.4+ (HudElementRegistry EXPERIENCE_LEVEL
    // on 1.21.6+, IdentifiedLayer.EXPERIENCE_LEVEL on 1.21.4/1.21.5). 1.21.1 keeps the Mixin.
    //? if <1.21.4 {
    /*@WrapMethod(method = "renderExperienceLevel")
    private void vanillahud$xpLevel(GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceLevel().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getExperienceLevel());
        original.call(graphics, delta);
        HudTransform.end(graphics);
    }
    *///?}
}
