package org.polyfrost.vanillahud.mixin;

import cc.polyfrost.oneconfig.libs.universal.UGraphics;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScoreObjective;
import org.polyfrost.vanillahud.hud.Hotbar;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = GuiIngame.class, priority = 9000)
public abstract class GuiIngameMixin {

    @Shadow @Final protected Minecraft mc;

    @Shadow protected abstract void renderHotbarItem(int index, int xPos, int yPos, float partialTicks, EntityPlayer player);

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
        if (!Hotbar.hud.isEnabled()) return;
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.translate(Hotbar.hud.position.getX(), Hotbar.hud.position.getY(), 0f);
        GlStateManager.scale(Hotbar.hud.getScale(), Hotbar.hud.getScale(), 1f);
    }

    @Inject(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;drawTexturedModalRect(IIIIII)V", ordinal = 1, shift = At.Shift.AFTER))
    private void pop(ScaledResolution sr, float partialTicks, CallbackInfo ci) {
        if (!Hotbar.hud.isEnabled()) return;
        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
    }

    @ModifyConstant(method = "renderTooltip", constant = @Constant(floatValue = -90.0F))
    private float zLevel(float constant) {
        return Hotbar.hud.isEnabled() ? 0F : constant;
    }

    @ModifyArgs(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;drawTexturedModalRect(IIIIII)V"))
    private void setPosition(Args args) {
        if (!Hotbar.hud.isEnabled()) return;
        ScaledResolution sr = new ScaledResolution(mc);
        int x = sr.getScaledWidth() / 2 - 91;
        int y = sr.getScaledHeight() - 22;
        args.set(0, (int) args.get(0) - x);
        args.set(1, (int) args.get(1) - y);
    }

    @Redirect(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderHotbarItem(IIIFLnet/minecraft/entity/player/EntityPlayer;)V"))
    private void scaleItems(GuiIngame instance, int index, int xPos, int yPos, float partialTicks, EntityPlayer player) {
        if (!Hotbar.hud.isEnabled()) {
            renderHotbarItem(index, xPos, yPos, partialTicks, player);
        } else {
            GlStateManager.pushMatrix();
            GlStateManager.translate(Hotbar.hud.position.getX(), Hotbar.hud.position.getY(), 0f);
            GlStateManager.scale(Hotbar.hud.getScale(), Hotbar.hud.getScale(), 1f);

            renderHotbarItem(index, index * 20 + 3, 3, partialTicks, player);

            GlStateManager.popMatrix();
        }
    }
}
