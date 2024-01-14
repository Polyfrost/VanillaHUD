package org.polyfrost.vanillahud.mixin;

import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.libs.universal.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
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

    @Unique
    @Exclude
    private final Minecraft mc = Minecraft.getMinecraft();

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
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) Hotbar.hud.position.getX(), (int) Hotbar.hud.position.getY(), 0f);
        GlStateManager.scale(Hotbar.hud.getScale(), Hotbar.hud.getScale(), 1f);
    }

    @Inject(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderHelper;disableStandardItemLighting()V"))
    private void pop(ScaledResolution sr, float partialTicks, CallbackInfo ci) {
        GlStateManager.popMatrix();
    }

    @ModifyArgs(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;drawTexturedModalRect(IIIIII)V"))
    private void setPosition(Args args) {
        ScaledResolution sr = new ScaledResolution(mc);
        int x = sr.getScaledWidth() / 2 - 91;
        int y = sr.getScaledHeight() - 22;
        args.set(0, (int) args.get(0) - x);
        args.set(1, (int) args.get(1) - y);
    }

    @Redirect(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderHotbarItem(IIIFLnet/minecraft/entity/player/EntityPlayer;)V"))
    private void scaleItems(GuiIngame instance, int index, int xPos, int yPos, float partialTicks, EntityPlayer player) {
        renderHotbarItem(index, index * 20 + 3, 3, partialTicks, player);
    }
}
