package org.polyfrost.vanillahud.mixin;

//? if >= 26.2 {
import net.minecraft.client.gui.Gui;
import org.polyfrost.vanillahud.render.HudCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class CaptureSwapMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void vanillahud$swapCaptures(CallbackInfo ci) {
        HudCapture.swapBeforePrepare();
    }
}
//?} elif >= 26.1 {
/*import net.minecraft.client.renderer.GameRenderer;
import org.polyfrost.vanillahud.render.HudCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class CaptureSwapMixin {
    @Inject(method = "extractGui", at = @At("HEAD"))
    private void vanillahud$swapCaptures(CallbackInfo ci) {
        HudCapture.swapBeforePrepare();
    }
}
*///?} elif > 1.8.9 {
/*import net.minecraft.client.renderer.GameRenderer;
import org.polyfrost.vanillahud.render.HudCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class CaptureSwapMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void vanillahud$swapCaptures(CallbackInfo ci) {
        HudCapture.swapBeforePrepare();
    }
}
*///?} else {
/*import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Minecraft.class)
public class CaptureSwapMixin {
}
*///?}
