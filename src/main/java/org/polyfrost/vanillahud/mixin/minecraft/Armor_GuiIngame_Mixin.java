package org.polyfrost.vanillahud.mixin.minecraft;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.ForgeHooks;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.vanillahud.VanillaHUDOld;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.hud.bars.ArmorHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameForge.class)
public abstract class Armor_GuiIngame_Mixin {
    @Shadow
    public static int left_height;

    @Shadow
    protected abstract void post(RenderGameOverlayEvent.ElementType type);

    @Inject(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V"), cancellable = true)
    private void setupArmorTranslationAndScale(CallbackInfo ci) {
        if (VanillaHUDOld.isApec()) {
            return;
        }
        ArmorHud hud = VanillaHUD.getArmor();
        if (hud.getHidden()) {
            post(RenderGameOverlayEvent.ElementType.ARMOR);
            ci.cancel();
            return;
        }
        float scale = hud.getScale();
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) hud.getX() - 182, (int) hud.getY() - (hud.getHealthLink() ? VanillaHUD.getHealthLinkAmount() : 0) - (hud.getMountLink() ? VanillaHUD.getMountLinkAmount() : 0) - left_height, 0F);
        GlStateManager.scale(scale, scale, 1F);
    }

    @Inject(method = "renderArmor", at = @At("RETURN"), remap = false)
    private void popArmorMatrix(CallbackInfo ci) {
        if (VanillaHUDOld.isApec()) {
            return;
        }
        GlStateManager.popMatrix();
    }

    @Redirect(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;getTotalArmorValue(Lnet/minecraft/entity/player/EntityPlayer;)I"), remap = false)
    private int armorExample(EntityPlayer player) {
        if (VanillaHUDOld.isApec()) {
            return ForgeHooks.getTotalArmorValue(player);
        }
        int value = ForgeHooks.getTotalArmorValue(player);
        return HudManager.isPanelOpen() ? (value > 0 ? value : 10) : value;
    }

    @Redirect(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V"))
    private void armorFlip(GuiIngameForge instance, int x, int y, int textureX, int textureY, int width, int height) {
        if (VanillaHUDOld.isApec()) {
            instance.drawTexturedModalRect(x, y, textureX, textureY, width, height);
            return;
        }
        ArmorHud hud = VanillaHUD.getArmor();
        int left = hud.getAlignment() == 1 ? 72 - x : x;
        if (hud.getAlignment() == 1 && textureX == 25) {
            Gui.drawScaledCustomSizeModalRect(left, y, textureX + 9, textureY, -9, 9, 9, 9, 256, 256);
        } else {
            instance.drawTexturedModalRect(left, y, textureX, textureY, width, height);
        }
    }
}
