package org.polyfrost.vanillahud.mixin.elements;

//? if > 1.8.9 {
import org.spongepowered.asm.mixin.Mixin;

//? if <26 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
public class GuiMixinStatusEffects {
    //? if <26 {
    /*@WrapMethod(method = "renderEffects")
    private void vanillahud$statusEffects(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (!Huds.INSTANCE.getStatusEffects().shouldDraw()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getStatusEffects());
        original.call(graphics, deltaTracker);
        HudTransform.end(graphics);
    }
    *///?}
}
//?} else {
/*// 1.8.9 has no status effect icons on the HUD
import net.minecraft.client.gui.GameGui;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameGui.class)
public class GuiMixinStatusEffects {
}
*///?}
