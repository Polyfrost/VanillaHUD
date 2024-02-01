package org.polyfrost.vanillahud.mixin;

import net.minecraft.client.gui.inventory.GuiContainerCreative;
import org.polyfrost.vanillahud.hooks.GuiHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainerCreative.class)
public abstract class GuiContainerCreativeMixin {

    @Inject(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/InventoryEffectRenderer;handleMouseInput()V", shift = At.Shift.AFTER), cancellable = true)
    private void cancelScrolling(CallbackInfo ci) {
        if (GuiHook.isScrolling) ci.cancel();
    }
}