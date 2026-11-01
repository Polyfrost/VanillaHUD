package org.polyfrost.vanillahud.mixin;

//? if = 1.8.9 {
/*import net.minecraft.client.render.platform.GlStateManager;
import org.polyfrost.vanillahud.render.HudCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public abstract class CaptureColorMixin {
    @Inject(method = "color4f", at = @At("HEAD"))
    private static void vanillahud$color(float r, float g, float b, float a, CallbackInfo ci) {
        HudCapture.color(r, g, b, a);
    }
}
*///?} else {
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

// only 1.8.9 needs this hook, see DrawCaptureMixin
@Mixin(Minecraft.class)
public abstract class CaptureColorMixin {
}
//?}
