package org.polyfrost.vanillahud.mixin;

import cc.polyfrost.oneconfig.libs.universal.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScoreObjective;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.hud.Hotbar;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiIngame.class, priority = 9000)
public abstract class GuiIngameMixin {

    @Shadow
    protected abstract void renderHotbarItem(int index, int xPos, int yPos, float partialTicks, EntityPlayer player);

    @Inject(method = "renderBossHealth", at = @At("HEAD"), cancellable = true)
    private void cancelBossBar(CallbackInfo ci) {
        ci.cancel();
        UGraphics.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        UMinecraft.getMinecraft().getTextureManager().bindTexture(Gui.icons);
    }

    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void cancelScoreboard(ScoreObjective s, ScaledResolution sr, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;drawTexturedModalRect(IIIIII)V", ordinal = 0))
    private void translate(ScaledResolution sr, float partialTicks, CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) Hotbar.hud.position.getX(), (int) Hotbar.hud.position.getY(), 0f);
        GlStateManager.scale(Hotbar.hud.getScale(), Hotbar.hud.getScale(), 1f);
        GlStateManager.pushMatrix();
        if (!Hotbar.HotBarHud.hotbarMode) {
            GlStateManager.translate(22, 0, 0);
            GlStateManager.rotate(90 , 0 ,0 ,1);
        }
    }

    @Inject(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;drawTexturedModalRect(IIIIII)V", ordinal = 1, shift = At.Shift.AFTER))
    private void popRotate(ScaledResolution sr, float partialTicks, CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        GlStateManager.popMatrix();
    }
    @Inject(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderHelper;disableStandardItemLighting()V"))
    private void pop(ScaledResolution sr, float partialTicks, CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        GlStateManager.popMatrix();
    }

    @Inject(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;drawTexturedModalRect(IIIIII)V"))
    private void prePosition(ScaledResolution sr, float partialTicks, CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        int x = sr.getScaledWidth() / 2 - 91;
        int y = sr.getScaledHeight() - 22;
        GlStateManager.pushMatrix();
        GlStateManager.translate(-x, -y, 0);
    }

    @Inject(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;drawTexturedModalRect(IIIIII)V", shift = At.Shift.AFTER))
    private void postPosition(ScaledResolution sr, float partialTicks, CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        GlStateManager.popMatrix();
    }

    @Redirect(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderHotbarItem(IIIFLnet/minecraft/entity/player/EntityPlayer;)V"))
    private void scaleItems(GuiIngame instance, int index, int xPos, int yPos, float partialTicks, EntityPlayer player) {
        if (VanillaHUD.isApec()) {
            renderHotbarItem(index, xPos, yPos, partialTicks, player);
            return;
        }
        renderHotbarItem(index, (Hotbar.HotBarHud.hotbarMode ? index * 20 : 0) + 3, (Hotbar.HotBarHud.hotbarMode ? 0 : index * 20) + 3, partialTicks, player);
    }
}
