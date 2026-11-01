package org.polyfrost.vanillahud.mixin;

//? if = 1.8.9 {
/*import net.minecraft.client.render.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.polyfrost.vanillahud.render.HudCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureManager.class)
public abstract class CaptureTextureMixin {
    @Inject(method = "bind", at = @At("HEAD"))
    private void vanillahud$bind(ResourceLocation id, CallbackInfo ci) {
        HudCapture.bind(id);
    }
}
*///?} else {
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

// only 1.8.9 needs this hook, see DrawCaptureMixin
@Mixin(Minecraft.class)
public abstract class CaptureTextureMixin {
}
//?}
