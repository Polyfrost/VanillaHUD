package org.polyfrost.vanillahud.mixin;

import cc.polyfrost.oneconfig.renderer.TextRenderer;
import cc.polyfrost.oneconfig.utils.color.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.polyfrost.vanillahud.hud.TabList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GuiPlayerTabOverlay.class)
public class GuiPlayerTabOverlayMixin {

    @Unique
    int TRANSPARENT = ColorUtils.getColor(0, 0, 0, 0);

    @ModifyVariable(method = "renderPlayerlist", at = @At(value = "STORE", ordinal = 0), ordinal = 9)
    private int resetY(int y) {
        return 1;
    }

    @Inject(method = "renderPlayerlist", at = @At(value = "HEAD"))
    private void translate(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) (-width / 2) * TabList.hud.getScale() + (int) TabList.hud.position.getCenterX() , TabList.hud.position.getY(), 0);
        GlStateManager.scale(TabList.hud.getScale(), TabList.hud.getScale(), 1f);
    }

    @Inject(method = "renderPlayerlist", at = @At(value = "TAIL"))
    private void pop(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        GlStateManager.popMatrix();
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 0))
    private void captureWidth(Args args) {
        TabList.hud.drawBG();
        args.set(4, TRANSPARENT);
        int width = new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth() / 2;
        TabList.width = ((int) args.get(2) - width) * 2;
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 1))
    private void cancelRect(Args args) {
        args.set(4, TRANSPARENT);
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 3))
    private void captureHeight(Args args) {
        args.set(4, TRANSPARENT);
        TabList.height = args.get(3);
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    private int drawText(FontRenderer instance, String text, float x, float y, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        TextRenderer.drawScaledString(text, x, y, color, TextRenderer.TextType.toType(TabList.TabHud.textType), 1);
        GlStateManager.popMatrix();
        return 0;
    }

}
