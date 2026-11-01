package org.polyfrost.vanillahud.mixin;

//? if = 1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.Font;
import org.polyfrost.vanillahud.compat.LegacyDrawContext;
import org.polyfrost.vanillahud.render.HudCapture;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Font.class)
public abstract class CaptureFontMixin {
    @WrapMethod(method = "draw(Ljava/lang/String;FFIZ)I")
    private int vanillahud$draw(String text, float x, float y, int color, boolean shadow, Operation<Integer> original) {
        if (text != null && HudCapture.text(LegacyDrawContext.INSTANCE, text, (int) x, (int) y, color, shadow)) {
            return (int) x + ((Font) (Object) this).width(text) + (shadow ? 1 : 0);
        }
        return original.call(text, x, y, color, shadow);
    }
}
*///?} else {
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

// only 1.8.9 needs this hook, see DrawCaptureMixin
@Mixin(Minecraft.class)
public abstract class CaptureFontMixin {
}
//?}
