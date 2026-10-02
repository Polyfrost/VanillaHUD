package org.polyfrost.vanillahud.mixin.elements;

//? if = 1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.render.Window;
import org.polyfrost.vanillahud.compat.LegacyDrawContext;
import org.polyfrost.vanillahud.render.HudTransform;
*///?}

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.objectweb.asm.Opcodes;
import org.polyfrost.vanillahud.hud.HeldItemTooltipHud;
import org.polyfrost.vanillahud.hud.Huds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if <1.21.6 && >1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
public class GuiMixinHeldItemTooltip {
    //? if > 1.8.9 {
    //? if <1.21.6 && >1.8.9 {
    /*@WrapMethod(method = "renderSelectedItemName")
    private void vanillahud$itemName(GuiGraphicsExtractor graphics, Operation<Void> original) {
        if (!Huds.INSTANCE.getHeldItemTooltip().shouldDraw()) return;

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
    //?} else {
    /*@WrapMethod(method = "renderSelectedItemName")
    private void vanillahud$itemName(Window window, Operation<Void> original) {
        HeldItemTooltipHud hud = Huds.INSTANCE.getHeldItemTooltip();
        if (!hud.shouldDraw()) return;

        HudTransform.begin(LegacyDrawContext.INSTANCE, hud);
        original.call(window);
        HudTransform.end(LegacyDrawContext.INSTANCE);
    }

    // the first read gates the draw and the second one scales the alpha, same as the newer versions
    @ModifyExpressionValue(method = "renderSelectedItemName", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/gui/GameGui;itemSelectedTimer:I", ordinal = 0,
            opcode = Opcodes.GETFIELD))
    private int vanillahud$gateTimer(int original) {
        HeldItemTooltipHud hud = Huds.INSTANCE.getHeldItemTooltip();
        return !hud.getFadeOut() ? Integer.MAX_VALUE : original;
    }

    @ModifyExpressionValue(method = "renderSelectedItemName", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/gui/GameGui;itemSelectedTimer:I", ordinal = 1,
            opcode = Opcodes.GETFIELD))
    private int vanillahud$alphaTimer(int original) {
        HeldItemTooltipHud hud = Huds.INSTANCE.getHeldItemTooltip();
        return (!hud.getFadeOut() || hud.getInstantFade()) ? Integer.MAX_VALUE : original;
    }
    *///?}
}
