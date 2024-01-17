package org.polyfrost.vanillahud.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.polyfrost.vanillahud.hud.Tablist;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GuiPlayerTabOverlay.class)
public class GuiPlayerTabOverlayMixin {

    @ModifyVariable(method = "renderPlayerlist", at = @At(value = "STORE", ordinal = 0), ordinal = 9)
    private int resetY(int y) {
        return 1;
    }

    @Inject(method = "renderPlayerlist", at = @At(value = "HEAD"))
    private void translate(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) (-width / 2) * Tablist.hud.getScale() + (int) Tablist.hud.position.getCenterX() , Tablist.hud.position.getY(), 0);
        GlStateManager.scale(Tablist.hud.getScale(), Tablist.hud.getScale(), 1f);
    }

    @Inject(method = "renderPlayerlist", at = @At(value = "TAIL"))
    private void pop(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        GlStateManager.popMatrix();
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 0))
    private void captureWidth(Args args) {
        int width = new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth() / 2;
        Tablist.width = ((int) args.get(2) - width) * 2;
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 3))
    private void captureHeight(Args args) {
        Tablist.height = args.get(3);
    }

}
