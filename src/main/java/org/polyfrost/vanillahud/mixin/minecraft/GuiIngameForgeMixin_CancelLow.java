package org.polyfrost.vanillahud.mixin.minecraft;

import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.polyfrost.vanillahud.hud.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiIngameForge.class, priority = 999)
public abstract class GuiIngameForgeMixin_CancelLow {
    @Shadow protected abstract void post(RenderGameOverlayEvent.ElementType type);

    @Inject(method = "renderJumpBar", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V", ordinal = 0), cancellable = true, remap = false)
    private void cancelHorsePower(CallbackInfo ci) {
        if (!Experience.hud.shouldRender()) {
            post(RenderGameOverlayEvent.ElementType.JUMPBAR);
            ci.cancel();
        }
    }

    @Inject(method = "renderHealthMount", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V"), cancellable = true)
    private void cancelHorseHealth(CallbackInfo ci) {
        if (!Hunger.getMountHud().shouldRender()) {
            post(RenderGameOverlayEvent.ElementType.HEALTHMOUNT);
            ci.cancel();
        }
    }

    @Inject(method = "renderExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", ordinal = 0), cancellable = true)
    private void cancelExpBar(CallbackInfo ci) {
        if (!Experience.hud.shouldRender()) {
            post(RenderGameOverlayEvent.ElementType.EXPERIENCE);
            ci.cancel();
        }
    }

    @Inject(method = "renderHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V"), cancellable = true)
    private void cancelHealth(CallbackInfo ci) {
        if (!Health.hud.shouldRender()) {
            post(RenderGameOverlayEvent.ElementType.HEALTH);
            ci.cancel();
        }
    }

    @Inject(method = "renderFood", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private void cancelHunger(CallbackInfo ci) {
        if (!Hunger.hud.shouldRender()) {
            post(RenderGameOverlayEvent.ElementType.FOOD);
            ci.cancel();
        }
    }

    @Inject(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V"), cancellable = true)
    private void cancelArmor(CallbackInfo ci) {
        if (!Armor.hud.shouldRender()) {
            post(RenderGameOverlayEvent.ElementType.ARMOR);
            ci.cancel();
        }
    }

    @Inject(method = "renderAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V"), cancellable = true)
    private void cancelAir(CallbackInfo ci) {
        if (!Air.hud.shouldRender()) {
            post(RenderGameOverlayEvent.ElementType.AIR);
            ci.cancel();
        }
    }
}
