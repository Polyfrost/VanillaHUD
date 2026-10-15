package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.polyfrost.vanillahud.hud.Huds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if <1.21.6 && >1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
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
public class GuiMixinHunger {
    //? if <1.21.6 && >1.8.9 {
    /*@WrapMethod(method = "renderFood")
    private void vanillahud$hunger(
            GuiGraphicsExtractor graphics, Player player, int yLineBase, int xRight, Operation<Void> original) {
        if (!Huds.INSTANCE.getHotbar().shouldDraw()) return;

        HudTransform.beginIcons(graphics, Huds.INSTANCE.getHotbar());
        original.call(graphics, player, yLineBase, xRight);
        HudTransform.endIcons(graphics);
    }
    *///?}

    // 1.8.9 shakes the food icons from inside renderStatusBars, where the call cannot be told
    // apart from the heart jitter, so the toggle has no hook there yet
    //? if > 1.8.9 {
    @ModifyExpressionValue(
            //? if < 26 {
            /*method = "renderFood",
            *///?} else {
            method = "extractFood",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
    )
    private int vanillahud$hungerShake(int original) {
        return Huds.INSTANCE.getHotbar().getHungerAnimation() ? original : 1;
    }
    //?}
}
