package org.polyfrost.vanillahud.mixin.elements;

//? if = 1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.render.Window;
import org.polyfrost.vanillahud.render.TextBackdrop;
import org.polyfrost.vanillahud.compat.LegacyDrawContext;
import org.polyfrost.vanillahud.render.HudTransform;
*///?}

//? if > 1.8.9 {
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.polyfrost.vanillahud.hook.TextShadowHook;
import org.polyfrost.vanillahud.render.TextBackdrop;
//?}
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

    @WrapOperation(
            //? if >=26 {
            method = "extractSelectedItemName",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;textWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V")
            //?} elif >=1.21.8 {
            /*method = "renderSelectedItemName",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V")
            *///?} else {
            /*method = "renderSelectedItemName",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)I")
            *///?}
    )
    //? if >=1.21.8 {
    private void vanillahud$itemNameText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y,
                                      int width, int color, Operation<Void> original) {
    //?} else
    //private int vanillahud$itemNameText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int width, int color, Operation<Integer> original) {
        HeldItemTooltipHud hud = Huds.INSTANCE.getHeldItemTooltip();
        TextBackdrop.draw(graphics, hud, x, y, width, 9, color);
        TextShadowHook.push(hud.getTextShadow());
        try {
            //? if >=1.21.8 {
            original.call(graphics, font, text, x, y, width, color);
            //?} else
            //return original.call(graphics, font, text, x, y, width, color);
        } finally {
            TextShadowHook.pop();
        }
    }
    //?} else {
    /*@WrapMethod(method = "renderSelectedItemName")
    private void vanillahud$itemName(Window window, Operation<Void> original) {
        HeldItemTooltipHud hud = Huds.INSTANCE.getHeldItemTooltip();
        if (!hud.hasContent() || !hud.shouldDraw()) return;

        HudTransform.begin(LegacyDrawContext.INSTANCE, hud);
        original.call(window);
        HudTransform.end(LegacyDrawContext.INSTANCE);
    }

    @WrapOperation(method = "renderSelectedItemName", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;drawWithShadow(Ljava/lang/String;FFI)I"))
    private int vanillahud$itemNameDraw(Font font, String text, float x, float y, int color,
                                        Operation<Integer> original) {
        HeldItemTooltipHud hud = Huds.INSTANCE.getHeldItemTooltip();
        TextBackdrop.draw(LegacyDrawContext.INSTANCE, hud, (int) x, (int) y, font.width(text), 9, color);
        return font.draw(text, x, y, color, hud.getTextShadow());
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
