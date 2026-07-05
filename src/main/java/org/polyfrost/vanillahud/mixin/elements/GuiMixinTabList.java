package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.DeltaTracker;
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
public class GuiMixinTabList {
    @WrapMethod(
            //? if < 26 {
            /*method = "renderTabList"
            *///?} else {
            method = "extractTabList"
            //?}
    )
    private void vanillahud$tabList(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker,
                                    Operation<Void> original) {
        if (!Huds.INSTANCE.getTabList().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getTabList());
        original.call(graphics, deltaTracker);
        HudTransform.end(graphics);
    }
}
