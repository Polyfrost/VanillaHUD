package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.polyfrost.vanillahud.hud.Huds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if <1.21.6 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
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
public class GuiMixinHunger {
    //? if <1.21.6 {
    /*@WrapMethod(method = "renderFood")
    private void vanillahud$hunger(
            GuiGraphicsExtractor graphics, Player player, int yLineBase, int xRight, Operation<Void> original) {
        if (!Huds.INSTANCE.getHunger().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getHunger());
        original.call(graphics, player, yLineBase, xRight);
        HudTransform.end(graphics);
    }
    *///?}

    @ModifyExpressionValue(
            //? if < 26 {
            /*method = "renderFood",
            *///?} else {
            method = "extractFood",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
    )
    private int vanillahud$hungerShake(int original) {
        return Huds.INSTANCE.getHunger().getAnimation() ? original : 1;
    }
}
