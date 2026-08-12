package org.polyfrost.vanillahud.mixin.elements;

import org.spongepowered.asm.mixin.Mixin;

//? if <1.21.4 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.vanillahud.hud.HotbarHud;
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
public class GuiMixinExperienceLevel {
    //? if <1.21.4 {
    /*@WrapMethod(method = "renderExperienceLevel")
    private void vanillahud$xpLevel(GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!Huds.INSTANCE.getHotbar().shouldDraw()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getHotbar());
        // the level is one block of text so counter rotating it whole keeps it readable
        HudTransform.beginUpright(graphics, Huds.INSTANCE.getHotbar(),
                graphics.guiWidth() / 2f, graphics.guiHeight() - HotbarHud.LEVEL_CENTER_Y);
        original.call(graphics, delta);
        HudTransform.endUpright(graphics);
        HudTransform.end(graphics);
    }
    *///?}
}
