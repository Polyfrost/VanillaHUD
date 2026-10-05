package org.polyfrost.vanillahud.mixin.elements;

import org.spongepowered.asm.mixin.Mixin;

//? if <1.21.4 && >1.8.9 {
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
public class GuiMixinExperienceLevel {
    //? if <1.21.4 && >1.8.9 {
    /*@WrapMethod(method = "renderExperienceLevel")
    private void vanillahud$xpLevel(GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!Huds.INSTANCE.getHotbar().shouldDraw() || !Huds.INSTANCE.getHotbar().getExperienceLevel()) return;

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
