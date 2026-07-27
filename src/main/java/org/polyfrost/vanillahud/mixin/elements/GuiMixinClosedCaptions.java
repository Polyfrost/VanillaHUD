package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.network.chat.Component;
import org.polyfrost.vanillahud.hud.ClosedCaptionsHud;
import org.polyfrost.vanillahud.hud.Huds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if <1.21.4 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import org.polyfrost.vanillahud.render.HudTransform;
*///?}

@Mixin(SubtitleOverlay.class)
public class GuiMixinClosedCaptions {
    //? if <1.21.4 {
    /*@WrapMethod(method = "render")
    private void vanillahud$closedCaptions(GuiGraphicsExtractor graphics, Operation<Void> original) {
        ClosedCaptionsHud hud = Huds.INSTANCE.getClosedCaptions();
        if (!hud.shouldRender()) return;

        HudTransform.begin(graphics, hud);
        original.call(graphics);
        HudTransform.end(graphics);
    }
    *///?}

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "render",
            *///?} else {
            method = "extractRenderState",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getBackgroundColor(F)I")
    )
    private int vanillahud$closedCaptions$background(int original) {
        return Huds.INSTANCE.getClosedCaptions().getCaptionBgArgb();
    }

    @WrapOperation(
            //? if >=26 {
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
            )
            //?} elif >=1.21.6 {
            /*method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
            )
            *///?} else {
            /*method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"
            )
            *///?}
    )
    //? if <1.21.6 {
    /*private int vanillahud$closedCaptions$text(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color, Operation<Integer> original) {
        ClosedCaptionsHud hud = Huds.INSTANCE.getClosedCaptions();
        return graphics.drawString(font, text, x, y, hud.captionTextArgb(color), hud.getTextShadow());
    }
    *///?} elif <26 {
    /*private void vanillahud$closedCaptions$text(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color, Operation<Void> original) {
        ClosedCaptionsHud hud = Huds.INSTANCE.getClosedCaptions();
        graphics.drawString(font, text, x, y, hud.captionTextArgb(color), hud.getTextShadow());
    }
    *///?} else {
    private void vanillahud$closedCaptions$text(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color, Operation<Void> original) {
        ClosedCaptionsHud hud = Huds.INSTANCE.getClosedCaptions();
        graphics.text(font, text, x, y, hud.captionTextArgb(color), hud.getTextShadow());
    }
    //?}

    @WrapOperation(
            //? if >=26 {
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
            )
            //?} elif >=1.21.6 {
            /*method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
            )
            *///?} else {
            /*method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"
            )
            *///?}
    )
    //? if <1.21.6 {
    /*private int vanillahud$closedCaptions$arrow(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color, Operation<Integer> original) {
        ClosedCaptionsHud hud = Huds.INSTANCE.getClosedCaptions();
        return graphics.drawString(font, text, x, y, hud.captionTextArgb(color), hud.getTextShadow());
    }
    *///?} elif <26 {
    /*private void vanillahud$closedCaptions$arrow(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color, Operation<Void> original) {
        ClosedCaptionsHud hud = Huds.INSTANCE.getClosedCaptions();
        graphics.drawString(font, text, x, y, hud.captionTextArgb(color), hud.getTextShadow());
    }
    *///?} else {
    private void vanillahud$closedCaptions$arrow(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color, Operation<Void> original) {
        ClosedCaptionsHud hud = Huds.INSTANCE.getClosedCaptions();
        graphics.text(font, text, x, y, hud.captionTextArgb(color), hud.getTextShadow());
    }
    //?}
}
