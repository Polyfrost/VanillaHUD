package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;

//? if 1.21.1 {
/*import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
//import net.minecraft.client.gui.Gui;
//?}

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
//@Mixin(Gui.class)
//?}
public class GuiMixinAir {
    //? if 1.21.1 {
    /*@Inject(
            method = "renderPlayerHealth",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getMaxAirSupply()I"),
            cancellable = true
    )
    private void vanillahud$air(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        if (!Huds.INSTANCE.getAir().shouldRender()) ci.cancel();

        HudTransform.begin(guiGraphics, Huds.INSTANCE.getAir());
    }

    @Inject(
            method = "renderPlayerHealth",
            at = @At("TAIL")
    )
    private void vanillahud$airEnd(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        HudTransform.end(guiGraphics);
    }
    *///?} elif >=1.21.4 {
    @WrapMethod(
            //? if < 26 {
            /*method = "renderAirBubbles"
            *///?} else {
            method = "extractAirBubbles"
            //?}
    )
    private void vanillahud$air(GuiGraphicsExtractor graphics, Player player, int vehicleHearts, int yLineAir,
                                int xRight, Operation<Void> original) {
        if (!Huds.INSTANCE.getAir().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getAir());
        original.call(graphics, player, vehicleHearts, yLineAir, xRight);
        HudTransform.end(graphics);
    }
    //?}
}
