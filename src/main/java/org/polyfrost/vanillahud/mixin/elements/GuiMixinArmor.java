package org.polyfrost.vanillahud.mixin.elements;

import org.spongepowered.asm.mixin.Mixin;

//? if <1.21.6 && >1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import org.polyfrost.vanillahud.hud.Huds;
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
public class GuiMixinArmor {
    //? if <1.21.6 && >1.8.9 {
    /*@WrapMethod(method = "renderArmor")
    private static void vanillahud$armor(
            GuiGraphicsExtractor graphics, Player player, int yLineBase, int numHealthRows, int healthRowHeight, int xLeft, Operation<Void> original) {
        if (!Huds.INSTANCE.getHotbar().shouldDraw()) return;

        HudTransform.beginIcons(graphics, Huds.INSTANCE.getHotbar());
        original.call(graphics, player, yLineBase, numHealthRows, healthRowHeight, xLeft);
        HudTransform.endIcons(graphics);
    }
    *///?}
}
