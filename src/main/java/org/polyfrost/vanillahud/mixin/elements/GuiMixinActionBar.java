package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
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

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
//@Mixin(Gui.class)
//?}
public class GuiMixinActionBar {
    @WrapMethod(
            //? if < 26 {
            /*method = "renderOverlayMessage"
            *///?} else {
            method = "extractOverlayMessage"
            //?}
    )
    private void vanillahud$actionBar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (!Huds.INSTANCE.getActionBar().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getActionBar());
        original.call(graphics, deltaTracker);
        HudTransform.end(graphics);
    }

    @ModifyExpressionValue(
            //? if < 26 {
            /*method = "renderOverlayMessage",
            *///?} else {
            method = "extractOverlayMessage",
            //?}
            at = @At(
                    value = "FIELD",
                    //? if >=26.2 {
                    target = "Lnet/minecraft/client/gui/Hud;animateOverlayMessageColor:Z"
                    //?} else {
                    //target = "Lnet/minecraft/client/gui/Gui;animateOverlayMessageColor:Z"
                    //?}
            )
    )
    private boolean vanillahud$actionBar$rainbowTimer(boolean original) {
        return original && Huds.INSTANCE.getActionBar().getRainbowTimer();
    }
}
