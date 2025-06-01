package org.polyfrost.vanillahud.mixin.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.vanillahud.Compatibility;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.hud.bars.HungerHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.FOOD;

@Mixin(GuiIngameForge.class)
public abstract class Hunger_GuiIngame_Mixin {
    @Shadow
    public static boolean renderFood;

    @Shadow
    protected abstract void post(RenderGameOverlayEvent.ElementType type);

    @Shadow
    public static int right_height;

    @Inject(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderFood(II)V"), cancellable = true)
    private void setupHungerTranslateAndScale(float partialTicks, CallbackInfo ci) {
        if (Compatibility.INSTANCE.isApec()) {
            return;
        }
        HungerHud hud = VanillaHUD.getHunger();
        if (hud.getHidden()) {
            post(FOOD);
            ci.cancel();
            return;
        }
        float scale = hud.getScale();
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) hud.getX() + 182, (int) hud.getY() - (hud.getHealthLink() ? VanillaHUD.getHealthLinkAmount() : 0) - (hud.getMountLink() ? VanillaHUD.getMountLinkAmount() : 0) - right_height, 0F);
        GlStateManager.scale(scale, scale, 1F);
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true, remap = false)
    private void hungerCancel(int width, int height, CallbackInfo ci) {
        if (Compatibility.INSTANCE.isApec()) {
            return;
        }
        if (!renderFood) {
            post(FOOD);
            ci.cancel();
        }
    }

    @Inject(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderFood(II)V", shift = At.Shift.AFTER))
    private void hungerReturn(CallbackInfo ci) {
        if (Compatibility.INSTANCE.isApec()) {
            return;
        }
        GlStateManager.popMatrix();
    }

    @ModifyVariable(method = "renderFood", at = @At("STORE"), ordinal = 8, remap = false)
    private int hungerAlignment(int x) {
        if (Compatibility.INSTANCE.isApec()) {
            return x;
        }
        return VanillaHUD.getHunger().getAlignment() == 1 ? 81 + x : -(x + 9);
    }

    @Redirect(method = "renderFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getRenderViewEntity()Lnet/minecraft/entity/Entity;"))
    private Entity hungerEdit(Minecraft instance) {
        if (Compatibility.INSTANCE.isApec()) {
            return instance.getRenderViewEntity();
        }
        return HudManager.isPanelOpen() ? instance.thePlayer : instance.getRenderViewEntity();
    }

    @Redirect(method = "renderFood", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"), remap = false)
    private int hungerAnimation(Random instance, int i) {
        return VanillaHUD.getHunger().getAnimation() ? instance.nextInt(i) : 1;
    }
}
