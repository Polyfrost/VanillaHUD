package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.Mixin;

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
public class GuiMixinHealth {
    @WrapMethod(
            //? if <26 {
            /*method = "renderHearts"
            *///?} else {
            method = "extractHearts"
            //?}
    )
    private void vanillahud$healthAnimation(
            GuiGraphicsExtractor graphics, Player player, int xLeft, int yLineBase, int healthRowHeight,
            int heartOffsetIndex, float maxHealth, int currentHealth, int oldHealth,
            int absorption, boolean blink, Operation<Void> original) {
        //? if <=1.21.6 {
        /*if (!Huds.INSTANCE.getHotbar().shouldDraw()) return;
        HudTransform.beginIcons(graphics, Huds.INSTANCE.getHotbar());
        *///?}

        original.call(graphics, player, xLeft, yLineBase, healthRowHeight, heartOffsetIndex, maxHealth,
                currentHealth, oldHealth, absorption, blink && Huds.INSTANCE.getHotbar().getHealthAnimation());

        //? if <=1.21.6 {
        /*HudTransform.endIcons(graphics);
        *///?}
    }

    @ModifyVariable(
            //? if >= 26 {
            method = "extractHeart", at = @At(value = "HEAD"), argsOnly = true, index = 5
            //?} else {
            /*method = "renderHearts", at = @At(value = "STORE"), ordinal = 1
            *///?}
    )
    private boolean setAlwaysHardcoreHearts(boolean isHardcore) {
        if (Huds.INSTANCE.getHotbar().getHardcoreHearts() == 1) return true;
        if (Huds.INSTANCE.getHotbar().getHardcoreHearts() == 2) return false;
        return isHardcore;
    }
}
