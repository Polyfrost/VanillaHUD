package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
//import net.minecraft.client.gui.Gui;
//?}

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
//@Mixin(Gui.class)
//?}
public class GuiMixinHeldItemTooltip {
    @WrapMethod(
            //? if < 26 {
            /*method = "renderSelectedItemName"
            *///?} else {
            method = "extractSelectedItemName"
            //?}
    )
    private void vanillahud$itemName(GuiGraphicsExtractor graphics, Operation<Void> original) {
        if (!Huds.INSTANCE.getHeldItemTooltip().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getHeldItemTooltip());
        original.call(graphics);
        HudTransform.end(graphics);
    }
}
