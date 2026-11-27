package org.polyfrost.vanillahud.mixin.elements;

//? if >=1.21.6 {
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=26.2 {
import net.minecraft.client.gui.contextualbar.LocatorBar;
//?} else {
/*import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
*///?}
import org.polyfrost.vanillahud.render.HudCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.2 {
@Mixin(LocatorBar.class)
//?} else {
/*@Mixin(LocatorBarRenderer.class)
*///?}
public class LocatorBarMixin {
    //? if >=26 {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    //?} else
    //@Inject(method = "render", at = @At("HEAD"))
    private void vanillahud$drawn(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        HudCapture.locatorDrawn = true;
    }
}
//?} elif > 1.8.9 {
/*// the locator bar arrived in 1.21.6
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Gui.class)
public class LocatorBarMixin {
}
*///?} else {
/*import net.minecraft.client.gui.GameGui;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameGui.class)
public class LocatorBarMixin {
}
*///?}
