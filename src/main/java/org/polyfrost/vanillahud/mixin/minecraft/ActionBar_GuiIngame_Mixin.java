package org.polyfrost.vanillahud.mixin.minecraft;

import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class ActionBar_GuiIngame_Mixin {
    @Inject(method = "setRecordPlaying(Ljava/lang/String;Z)V", at = @At("HEAD"))
    private void dispatchRecordPlayingEvent(String message, boolean isPlaying, CallbackInfo ci) {

    }

}
