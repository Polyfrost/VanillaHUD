package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.objectweb.asm.Opcodes;
import org.polyfrost.vanillahud.hud.HeldItemTooltipHud;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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
public class GuiMixinHeldItemTooltip {
    //? if <1.21.6 {
    /*@WrapMethod(method = "renderSelectedItemName")
    private void vanillahud$itemName(GuiGraphicsExtractor graphics, Operation<Void> original) {
        if (!Huds.INSTANCE.getHeldItemTooltip().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getHeldItemTooltip());
        original.call(graphics);
        HudTransform.end(graphics);
    }
    *///?}

    @ModifyExpressionValue(
            //? if < 26 {
            /*method = "renderSelectedItemName",
            *///?} else {
            method = "extractSelectedItemName",
            //?}
            //~ if >=26.2 'Gui' -> 'Hud'
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Hud;toolHighlightTimer:I", ordinal = 0,
                    opcode = Opcodes.GETFIELD))
    private int vanillahud$gateTimer(int original) {
        HeldItemTooltipHud hud = Huds.INSTANCE.getHeldItemTooltip();
        return !hud.getFadeOut() ? Integer.MAX_VALUE : original;
    }

    @ModifyExpressionValue(
            //? if < 26 {
            /*method = "renderSelectedItemName",
            *///?} else {
            method = "extractSelectedItemName",
            //?}
            //~ if >=26.2 'Gui' -> 'Hud'
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Hud;toolHighlightTimer:I", ordinal = 1,
                    opcode = Opcodes.GETFIELD))
    private int vanillahud$alphaTimer(int original) {
        HeldItemTooltipHud hud = Huds.INSTANCE.getHeldItemTooltip();
        return (!hud.getFadeOut() || hud.getInstantFade()) ? Integer.MAX_VALUE : original;
    }
}
