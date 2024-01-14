package org.polyfrost.vanillahud.mixin;

import net.minecraftforge.client.GuiIngameForge;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.hud.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiIngameForge.class, remap = false, priority = 9000)
public class GuiIngameForgeMixin_Cancel {

    @Inject(method = "renderRecordOverlay", at = @At("HEAD"), cancellable = true)
    private void cancelActionBar(int width, int height, float partialTicks, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderTitle", at = @At("HEAD"), cancellable = true)
    private void cancelTitle(int width, int height, float partialTicks, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderToolHightlight", at = @At("HEAD"), cancellable = true)
    private void cancelHeldItem(CallbackInfo ci) {
        if (!VanillaHUD.isApec()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderJumpBar", at = @At("HEAD"), cancellable = true)
    private void cancelHorsePower(CallbackInfo ci) {
        if (!Experience.hud.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderHealthMount", at = @At("HEAD"), cancellable = true)
    private void cancelHorseHealth(CallbackInfo ci) {
        if (!Hunger.getMountHud().isEnabled()) ci.cancel();
    }

    @Inject(method = "renderExperience", at = @At("HEAD"), cancellable = true)
    private void cancelExpBar(CallbackInfo ci) {
        if (!Experience.hud.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderHealth", at = @At("HEAD"), cancellable = true)
    private void cancelHealth(CallbackInfo ci) {
        if (!Health.hud.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void cancelHunger(CallbackInfo ci) {
        if (!Hunger.hud.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private void cancelArmor(CallbackInfo ci) {
        if (!Armor.hud.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderAir", at = @At("HEAD"), cancellable = true)
    private void cancelAir(CallbackInfo ci) {
        if (!Air.hud.isEnabled()) ci.cancel();
    }

}
