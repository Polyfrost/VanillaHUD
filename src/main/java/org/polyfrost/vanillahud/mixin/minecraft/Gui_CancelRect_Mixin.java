package org.polyfrost.vanillahud.mixin.minecraft;

import net.minecraft.client.gui.Gui;
import org.polyfrost.vanillahud.hooks.TabHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class Gui_CancelRect_Mixin {

    @Inject(method = "drawRect", at = @At("HEAD"), cancellable = true)
    private static void cancelRect(int left, int top, int right, int bottom, int color, CallbackInfo ci) {
        if (TabHook.cancelRect) {
            TabHook.cancelRect = false;
            ci.cancel();
        }
    }
}