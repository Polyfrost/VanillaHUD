package org.polyfrost.vanillahud.mixin;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import cc.polyfrost.oneconfig.renderer.TextRenderer;
import cc.polyfrost.oneconfig.utils.color.ColorUtils;
import club.sk1er.patcher.config.PatcherConfig;
import com.google.common.collect.Ordering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.hud.BossBar;
import org.polyfrost.vanillahud.hud.TabList;
import org.polyfrost.vanillahud.utils.TabListManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

// 1100 priority to load before Patcher
@Mixin(value = GuiPlayerTabOverlay.class, priority = 1100)
public class GuiPlayerTabOverlayMixin {

    @Shadow private IChatComponent header;
    @Shadow private IChatComponent footer;
    @Unique private static final IChatComponent tab$exampleHeader = new ChatComponentText("Tab List");
    @Unique private static final IChatComponent tab$exampleFooter = new ChatComponentText("VanillaHud");

    @Unique
    int tab$TRANSPARENT = ColorUtils.getColor(0, 0, 0, 0);

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
        if (HudCore.editing) return TabListManager.devInfo;
        return instance.sortedCopy(elements);
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 0))
    private void captureWidth(Args args) {
        boolean shouldTranslate = VanillaHUD.isPatcher && PatcherConfig.tabHeightAllow && BossBar.hud.drawingBossBar();
        TabList.hud.drawBG(shouldTranslate ? PatcherConfig.tabHeight : 0);
        args.set(4, tab$TRANSPARENT);
        int width = new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth() / 2;
        TabList.width = ((int) args.get(2) - width) * 2;
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 1))
    private void cancelRect(Args args) {
        args.set(4, tab$TRANSPARENT);
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 3))
    private void captureHeight(Args args) {
        args.set(4, tab$TRANSPARENT);
        TabList.height = args.get(3);
    }

    @ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = 20, ordinal = 0))
    private int limit(int constant) {
        return (HudCore.editing ? 10 : constant);
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    private int drawText(FontRenderer instance, String text, float x, float y, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        TextRenderer.drawScaledString(text, x - (TabList.TabHud.showHead ? 0 : 8), y, color, TextRenderer.TextType.toType(TabList.TabHud.textType), 1);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        return 0;
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawScaledCustomSizeModalRect(IIFFIIIIFF)V"))
    private void playerHead(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        if (!TabList.TabHud.showHead) return;

        Gui.drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawPing(IIILnet/minecraft/client/network/NetworkPlayerInfo;)V"))
    private void drawPing(GuiPlayerTabOverlay instance, int width, int x, int y, NetworkPlayerInfo networkPlayerInfoIn) {
        if (!TabList.TabHud.showPing) return;
        if (!TabList.TabHud.numberPing) { // in order to prevent blending issues we need to set the proper gl state
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GuiPlayerTabOverlayAccessor accessor = (GuiPlayerTabOverlayAccessor) instance;
            accessor.renderPing(width, x, y, networkPlayerInfoIn);
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
            return;
        }
        int ping = networkPlayerInfoIn.getResponseTime();
        OneColor color = tab$getColor(ping);
        String pingString = String.valueOf(ping);
        if (TabList.TabHud.hideFalsePing && (ping <= 1 || ping >= 999)) pingString = "";
        if (!TabList.TabHud.pingType) {
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            TextRenderer.drawScaledString(pingString, 2 * (x + width) - Minecraft.getMinecraft().fontRendererObj.getStringWidth(String.valueOf(ping)) - 4, 2 * y + 4, color.getRGB(), TextRenderer.TextType.toType(TabList.TabHud.textType), 1F);
            GlStateManager.scale(2F, 2F, 2F);
        } else TextRenderer.drawScaledString(pingString, x + width - Minecraft.getMinecraft().fontRendererObj.getStringWidth(String.valueOf(ping)), y, color.getRGB(), TextRenderer.TextType.toType(TabList.TabHud.textType), 1F);
    }

    @Unique
    private static OneColor tab$getColor(int ping) {
        OneColor color = new OneColor(-5636096);
        if (ping >= 0 && ping < 75) color = TabList.TabHud.pingLevelOne;
        else if (ping >= 75 && ping < 145) color = TabList.TabHud.pingLevelTwo;
        else if (ping >= 145 && ping < 200) color = TabList.TabHud.pingLevelThree;
        else if (ping >= 200 && ping < 300) color = TabList.TabHud.pingLevelFour;
        else if (ping >= 300 && ping < 400) color = TabList.TabHud.pingLevelFive;
        else if (ping >= 400) color = TabList.TabHud.pingLevelSix;
        return color;
    }

    @ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = 553648127))
    private int tabOpacity(int opacity) {
        return TabList.TabHud.tabWidgetColor.getRGB();
    }
}
