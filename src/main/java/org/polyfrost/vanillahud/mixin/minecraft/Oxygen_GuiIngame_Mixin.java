package org.polyfrost.vanillahud.mixin.minecraft;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.GuiIngameForge;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.VanillaHUD2;
import org.polyfrost.vanillahud.hud.Air;
import org.polyfrost.vanillahud.hud.hotbar.OxygenHUD;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.polyfrost.vanillahud.hud.Health.healthLink;
import static org.polyfrost.vanillahud.hud.Hunger.mountLink;

@Mixin(GuiIngameForge.class)
public class Oxygen_GuiIngame_Mixin {
    @Shadow
    public static int right_height;

    @Inject(method = "renderAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V"))
    private void setupAirTranslationAndScale(int width, int height, CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        GlStateManager.pushMatrix();
        OxygenHUD hud = VanillaHUD2.INSTANCE.getOxygen();
        float scale = hud.getScale();
        GlStateManager.translate((int) hud.getX() - width - 182, (int) hud.getY() - (Air.AirHud.healthLink ? healthLink() : 0) - (Air.AirHud.mountLink ? mountLink() : 0) - height + right_height, 0F);
        GlStateManager.scale(scale, scale, 1f);
    }

    @Inject(method = "renderAir", at = @At("RETURN"))
    private void popAirMatrix(CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        GlStateManager.popMatrix();
    }

    @Redirect(method = "renderAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isInsideOfMaterial(Lnet/minecraft/block/material/Material;)Z"))
    private boolean airExample(EntityPlayer player, Material material) {
        if (VanillaHUD.isApec()) {
            return player.isInsideOfMaterial(material);
        }
        return player.isInsideOfMaterial(material) || HudManager.INSTANCE.getPanelOpen();
    }

    @ModifyArg(method = "renderAir", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V", ordinal = 0))
    private int airFixLeftParam(int left) {
        if (VanillaHUD.isApec()) return left;
        if (VanillaHUD2.INSTANCE.getOxygen().getAlignment() == 1) {
            return left + 81;
        } else return -(left + 9);
    }

    @Redirect(method = "renderAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getRenderViewEntity()Lnet/minecraft/entity/Entity;"))
    private Entity airFixRenderViewEntity(Minecraft instance) {
        if (VanillaHUD.isApec()) {
            return instance.getRenderViewEntity();
        }
        return HudManager.INSTANCE.getPanelOpen() ? instance.thePlayer : instance.getRenderViewEntity();
    }
}
