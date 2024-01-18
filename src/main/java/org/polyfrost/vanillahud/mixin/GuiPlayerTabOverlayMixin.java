package org.polyfrost.vanillahud.mixin;

import cc.polyfrost.oneconfig.internal.hud.HudCore;
import cc.polyfrost.oneconfig.renderer.TextRenderer;
import cc.polyfrost.oneconfig.utils.color.ColorUtils;
import com.google.common.collect.Ordering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.polyfrost.vanillahud.hud.TabList;
import org.polyfrost.vanillahud.utils.GameProfileHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

@Mixin(GuiPlayerTabOverlay.class)
public class GuiPlayerTabOverlayMixin {

    @Shadow private IChatComponent header;
    @Shadow private IChatComponent footer;
    @Unique private static final IChatComponent tab$exampleHeader = new ChatComponentText("TabList");
    @Unique private static final IChatComponent tab$exampleFooter = new ChatComponentText("Vanilla Hud");

    @Unique
    int TRANSPARENT = ColorUtils.getColor(0, 0, 0, 0);

    @ModifyVariable(method = "renderPlayerlist", at = @At(value = "STORE", ordinal = 0), ordinal = 9)
    private int resetY(int y) {
        return 1;
    }


    @Redirect(method = "renderPlayerlist", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;header:Lnet/minecraft/util/IChatComponent;"))
    private IChatComponent modifyHeader(GuiPlayerTabOverlay instance) {
        if (HudCore.editing) return tab$exampleHeader;
        return header;
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;footer:Lnet/minecraft/util/IChatComponent;"))
    private IChatComponent modifyFooter(GuiPlayerTabOverlay instance) {
        if (HudCore.editing) return tab$exampleFooter;
        return footer;
    }


    @Inject(method = "renderPlayerlist", at = @At(value = "HEAD"))
    private void translate(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        TabList.TabHud hud = TabList.hud;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) (-width / 2) * hud.getScale() + hud.position.getCenterX() , hud.position.getY() + hud.getPaddingY(), 0);
        GlStateManager.scale(hud.getScale(), hud.getScale(), 1f);
    }

    @Inject(method = "renderPlayerlist", at = @At(value = "TAIL"))
    private void pop(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        GlStateManager.popMatrix();
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Ordering;sortedCopy(Ljava/lang/Iterable;)Ljava/util/List;"))
    private List<NetworkPlayerInfo> list(Ordering<NetworkPlayerInfo> instance, Iterable<NetworkPlayerInfo> elements) {
        if (HudCore.editing) return GameProfileHelper.devInfo;
        return instance.sortedCopy(elements);
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
