package org.polyfrost.vanillahud.mixin;

import net.minecraft.client.gui.inventory.GuiContainerCreative;
import org.polyfrost.vanillahud.hooks.GuiHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainerCreative.class)
public class GuiContainerCreativeMixin {

    @Inject(method = "handleMouseInput", at = @At("HEAD"), cancellable = true)
    private void cancelScrolling(CallbackInfo ci) {
        if (GuiHook.isScrolling) ci.cancel();
    }
}