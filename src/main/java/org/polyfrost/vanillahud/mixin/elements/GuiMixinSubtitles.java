package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.components.SubtitleOverlay;
import org.polyfrost.vanillahud.hud.Huds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

//? if <1.21.4 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.vanillahud.hud.SubtitlesHud;
import org.polyfrost.vanillahud.render.HudTransform;
*///?}

@Mixin(SubtitleOverlay.class)
public class GuiMixinSubtitles {
    //? if <1.21.4 {
    /*@WrapMethod(method = "render")
    private void vanillahud$subtitles(GuiGraphicsExtractor graphics, Operation<Void> original) {
        SubtitlesHud hud = Huds.INSTANCE.getSubtitles();
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
    private int vanillahud$subtitles$background(int original) {
        return Huds.INSTANCE.getSubtitles().getCaptionBgArgb();
    }

    @ModifyArg(
            //? if >=26 {
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
            ),
            //?} elif >=1.21.6 {
            /*method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
            ),
            *///?} else {
            /*method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"
            ),
            *///?}
            index = 4
    )
    private int vanillahud$subtitles$textColor(int color) {
        return Huds.INSTANCE.getSubtitles().captionTextArgb(color);
    }

    @ModifyArg(
            //? if >=26 {
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
            ),
            //?} elif >=1.21.6 {
            /*method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
            ),
            *///?} else {
            /*method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"
            ),
            *///?}
            index = 4
    )
    private int vanillahud$subtitles$arrowColor(int color) {
        return Huds.INSTANCE.getSubtitles().captionTextArgb(color);
    }
}
