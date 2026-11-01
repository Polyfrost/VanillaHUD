package org.polyfrost.vanillahud.mixin;

//? if = 1.8.9 {
/*import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.Font;
import org.polyfrost.vanillahud.compat.LegacyDrawContext;
import org.polyfrost.vanillahud.hook.TextShadowHook;
import org.polyfrost.vanillahud.render.HudCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Font.class)
public abstract class CaptureFontMixin {
    @WrapMethod(method = "draw(Ljava/lang/String;FFIZ)I")
    private int vanillahud$draw(String text, float x, float y, int color, boolean shadow, Operation<Integer> original) {
        shadow = TextShadowHook.apply(shadow);
        if (text != null && HudCapture.text(LegacyDrawContext.INSTANCE, text, (int) x, (int) y, color, shadow)) {
            return (int) x + ((Font) (Object) this).width(text) + (shadow ? 1 : 0);
        }
        return original.call(text, x, y, color, shadow);
    }

    @ModifyReturnValue(method = "width(Ljava/lang/String;)I", at = @At("RETURN"))
    private int vanillahud$width(int width, String text) {
        return text == null ? width : HudCapture.fontWidth(width, text);
    }
}
*///?} else {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.polyfrost.vanillahud.render.HudCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Font.class)
public abstract class CaptureFontMixin {
    @ModifyReturnValue(method = "width(Ljava/lang/String;)I", at = @At("RETURN"))
    private int vanillahud$width(int width, String text) {
        return HudCapture.fontWidth(width, text);
    }

    @ModifyReturnValue(method = "width(Lnet/minecraft/network/chat/FormattedText;)I", at = @At("RETURN"))
    private int vanillahud$widthText(int width, FormattedText text) {
        return HudCapture.fontWidth(width, text);
    }

    @ModifyReturnValue(method = "width(Lnet/minecraft/util/FormattedCharSequence;)I", at = @At("RETURN"))
    private int vanillahud$widthSequence(int width, FormattedCharSequence text) {
        return HudCapture.fontWidth(width, text);
    }
}
//?}
