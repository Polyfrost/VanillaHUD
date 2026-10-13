package org.polyfrost.vanillahud.mixin.elements;

import org.spongepowered.asm.mixin.Mixin;

//? if = 1.21.1 {
/*//? if > 1.8.9
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
*///?}
//? if 1.21.1 {
/*import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
public class GuiMixinAir {
    //? if 1.21.1 {
    /*@Inject(
            method = "renderPlayerHealth",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getMaxAirSupply()I"),
            cancellable = true
    )
    private void vanillahud$air(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        if (!Huds.INSTANCE.getHotbar().shouldDraw()) {
            ci.cancel();
            return;
        }

        HudTransform.beginIcons(guiGraphics, Huds.INSTANCE.getHotbar());
    }

    @Inject(
            method = "renderPlayerHealth",
            at = @At("TAIL")
    )
    private void vanillahud$airEnd(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        HudTransform.endIcons(guiGraphics);
    }
    *///?}
}
