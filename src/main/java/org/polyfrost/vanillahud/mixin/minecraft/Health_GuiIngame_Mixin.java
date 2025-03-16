package org.polyfrost.vanillahud.mixin.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.VanillaHUD2;
import org.polyfrost.vanillahud.hud.hotbar.HealthHUD;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;


@Mixin(GuiIngameForge.class)
public abstract class Health_GuiIngame_Mixin {
    @Shadow
    public static int left_height;

    @Shadow
    protected abstract void post(RenderGameOverlayEvent.ElementType type);

    @Inject(method = "renderHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V"), cancellable = true)
    private void setupHealthTranslationAndScale(CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        HealthHUD hud = VanillaHUD2.getHealth();
        if (hud.getHidden()) {
            post(RenderGameOverlayEvent.ElementType.HEALTH);
            ci.cancel();
            return;
        }
        float scale = hud.getScale();
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) hud.getX() - 182, (int) hud.getY() - (hud.getMountLink() ? VanillaHUD2.getMountLinkAmount() : 0) - left_height, 0F);
        GlStateManager.scale(scale, scale, 1F);
    }

    @Inject(method = "renderHealth", at = @At("RETURN"), remap = false)
    private void popHealthMatrix(CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        GlStateManager.popMatrix();
    }

    @Redirect(method = "renderHealth", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V"))
    private void healthFlip(GuiIngameForge instance, int x, int y, int textureX, int textureY, int width, int height) {
        if (VanillaHUD.isApec()) {
            instance.drawTexturedModalRect(x, y, textureX, textureY, width, height);
            return;
        }
        HealthHUD hud = VanillaHUD2.getHealth();
        if (textureX < 61 || ((textureX - 16) / 9) % 2 == 0 || hud.getAlignment() == 0) {
            instance.drawTexturedModalRect(x, y, textureX, textureY, width, height);
        } else {
            Gui.drawScaledCustomSizeModalRect(x, y, textureX + 9, textureY, -9, 9, 9, 9, 256, 256);
        }
    }

    @ModifyVariable(method = "renderHealth", at = @At("STORE"), ordinal = 14, remap = false)
    private int healthAlignment(int x) {
        if (VanillaHUD.isApec()) {
            return x;
        }
        return VanillaHUD2.getHealth().getAlignment() == 1 ? 72 - x : x;
    }

    @ModifyVariable(method = "renderHealth", at = @At("STORE"), ordinal = 15, remap = false)
    private int healthMode(int y) {
        if (VanillaHUD.isApec()) {
            return y;
        }
        if (HudManager.INSTANCE.getPanelOpen()) return 0;
        if (VanillaHUD2.getHealth().getMode() == 1) return y;
        return -y;
    }

    @Redirect(method = "renderHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getRenderViewEntity()Lnet/minecraft/entity/Entity;"))
    private Entity healthEdit(Minecraft instance) {
        if (VanillaHUD.isApec()) {
            return instance.getRenderViewEntity();
        }
        return HudManager.INSTANCE.getPanelOpen() ? instance.thePlayer : instance.getRenderViewEntity();
    }


    @ModifyVariable(method = "renderHealth", at = @At(value = "STORE"), name = "regen", remap = false)
    private int healthAnimation(int value) {
        return VanillaHUD2.getHealth().getAnimation() ? value : -1;
    }

    @Redirect(method = "renderHealth", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"), remap = false)
    private int healthAnimation1(Random instance, int i) {
        return VanillaHUD2.getHealth().getAnimation() ? instance.nextInt(i) : 0;
    }
}
