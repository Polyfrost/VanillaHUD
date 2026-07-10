package org.polyfrost.vanillahud.mixin.elements;

import org.spongepowered.asm.mixin.Mixin;

//? if <=1.21.5 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
*///?}

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
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
}
