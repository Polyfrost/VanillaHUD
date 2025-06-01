package org.polyfrost.vanillahud.mixin.minecraft;

import dev.deftu.omnicore.client.render.OmniResolution;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.mixin.minecraft.interfaces.GuiIngameForgeAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderGameOverlay(F)V", shift = At.Shift.AFTER))
    private void draw(float partialTicks, long nanoTime, CallbackInfo ci) {
//        if (VanillaHUD.isCompactTab()) {
//            return;
//        } todo
        VanillaHUD.getPlayerList().setGuiIngame(false);
        ((GuiIngameForgeAccessor) Minecraft.getMinecraft().ingameGUI).drawPlayerList(OmniResolution.getScaledWidth(), OmniResolution.getScaledHeight());
    }
}
