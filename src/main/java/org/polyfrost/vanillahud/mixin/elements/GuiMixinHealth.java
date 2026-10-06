package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//? if > 1.8.9
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if > 1.8.9 {
import net.minecraft.world.entity.player.Player;
//?} else {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.Window;
import org.polyfrost.vanillahud.compat.LegacyDrawContext;
import org.polyfrost.vanillahud.compat.LegacyIconBatch;
import org.polyfrost.vanillahud.hud.HotbarHud;
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
        /*if (!Huds.INSTANCE.getHotbar().shouldDraw()) return;
        HudTransform.beginIcons(graphics, Huds.INSTANCE.getHotbar());
        *///?}

        original.call(graphics, player, xLeft, yLineBase, healthRowHeight, heartOffsetIndex, maxHealth,
                currentHealth, oldHealth, absorption, blink && Huds.INSTANCE.getHotbar().getHealthAnimation());

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
        if (Huds.INSTANCE.getHotbar().getHardcoreHearts() == 1) return true;
        if (Huds.INSTANCE.getHotbar().getHardcoreHearts() == 2) return false;
        return isHardcore;
    }
    //?} else {
    /*// 1.8.9 draws health, armour, hunger, air and mount health in a single pass, and all of
    // them belong to the fused hotbar element, so one wrap covers the lot. The health and
    // hunger jitter toggles have no separable hook there yet.
    @WrapMethod(method = "renderStatusBars")
    private void vanillahud$statusBars(Window window, Operation<Void> original) {
        HotbarHud hud = Huds.INSTANCE.getHotbar();
        if (!hud.shouldDraw()) return;

        HudTransform.beginIcons(LegacyDrawContext.INSTANCE, hud);
        original.call(window);
        LegacyIconBatch.draw();
        HudTransform.endIcons(LegacyDrawContext.INSTANCE);
    }

    @ModifyExpressionValue(
            method = "renderStatusBars",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldData;isHardcore()Z")
    )
    private boolean vanillahud$hardcoreHearts(boolean isHardcore) {
        if (Huds.INSTANCE.getHotbar().getHardcoreHearts() == 1) return true;
        if (Huds.INSTANCE.getHotbar().getHardcoreHearts() == 2) return false;
        return isHardcore;
    }
    *///?}
}
