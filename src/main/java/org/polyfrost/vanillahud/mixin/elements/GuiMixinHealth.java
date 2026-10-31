package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//? if > 1.8.9
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if > 1.8.9 {
import net.minecraft.world.entity.player.Player;
//?} else {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
*///?}
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.injection.At;
//? if > 1.8.9
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.Mixin;

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
public class GuiMixinHealth {
    //? if > 1.8.9 {
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
        //? if <=1.21.6 && >1.8.9 {
        /*if (!Huds.INSTANCE.getHealth().shouldDraw()) return;
        HudTransform.beginIcons(graphics, Huds.INSTANCE.getHealth());
        *///?}

        original.call(graphics, player, xLeft, yLineBase, healthRowHeight, heartOffsetIndex, maxHealth,
                currentHealth, oldHealth, absorption, blink && Huds.INSTANCE.getHealth().getAnimation());

        //? if <=1.21.6 && >1.8.9 {
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
        if (Huds.INSTANCE.getHealth().getHardcoreHearts() == 1) return true;
        if (Huds.INSTANCE.getHealth().getHardcoreHearts() == 2) return false;
        return isHardcore;
    }
    //?} else {
    /*@ModifyExpressionValue(
            method = "renderStatusBars",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldData;isHardcore()Z")
    )
    private boolean vanillahud$hardcoreHearts(boolean isHardcore) {
        if (Huds.INSTANCE.getHealth().getHardcoreHearts() == 1) return true;
        if (Huds.INSTANCE.getHealth().getHardcoreHearts() == 2) return false;
        return isHardcore;
    }
    *///?}
}
