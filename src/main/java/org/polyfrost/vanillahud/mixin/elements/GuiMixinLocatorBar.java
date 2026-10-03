package org.polyfrost.vanillahud.mixin.elements;

//? if > 1.8.9 {
import org.spongepowered.asm.mixin.Mixin;

//? if >=1.21.6 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if > 1.8.9
import net.minecraft.client.DeltaTracker;
//? if > 1.8.9
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.injection.At;
//?}

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
public class GuiMixinLocatorBar {
    //? if >=1.21.6 {
    @WrapOperation(
            //? if >=26 {
            method = "extractHotbarAndDecorations",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V")
            //?} else {
            /*method = "renderHotbarAndDecorations",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;render(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V")
            *///?}
    )
    private void vanillahud$locatorBar(ContextualBar bar, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!Huds.INSTANCE.getHotbar().shouldDraw()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getHotbar());
        original.call(bar, graphics, delta);
        HudTransform.end(graphics);
    }
    //?}
}
//?} else {
/*// the locator bar arrived in 1.21.6
import net.minecraft.client.gui.GameGui;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameGui.class)
public class GuiMixinLocatorBar {
}
*///?}
