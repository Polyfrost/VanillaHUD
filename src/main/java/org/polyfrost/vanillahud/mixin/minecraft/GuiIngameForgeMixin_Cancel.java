package org.polyfrost.vanillahud.mixin.minecraft;

import net.minecraftforge.client.GuiIngameForge;
import org.polyfrost.vanillahud.VanillaHUD2;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiIngameForge.class, priority = 9000)
public class GuiIngameForgeMixin_Cancel {

    @Inject(method = "renderRecordOverlay", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelActionBar(int width, int height, float partialTicks, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderTitle", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelTitle(int width, int height, float partialTicks, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderToolHightlight", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelHeldItem(CallbackInfo ci) {
        if (!VanillaHUD2.isApec()) {
            ci.cancel();
        }
    }

}
