package org.polyfrost.vanillahud.mixin.minecraft;

import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import com.google.common.collect.Ordering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.*;
import net.minecraft.util.*;
import org.polyfrost.polyui.color.PolyColor;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.hooks.TabHook;
import org.polyfrost.vanillahud.mixin.minecraft.interfaces.GuiPlayerTabOverlayAccessor;
import org.polyfrost.vanillahud.utils.TabListManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

import static org.polyfrost.vanillahud.utils.TempUtilsKt.drawScaledString;

// 1100 priority to load before Patcher
@Mixin(value = GuiPlayerTabOverlay.class, priority = 1100)
public abstract class GuiPlayerTabOverlayMixin {

    @Shadow
    private IChatComponent header, footer;

    @Shadow @Final private Minecraft mc;

    @Unique
    private static final IChatComponent tab$exampleHeader = new ChatComponentText("Tab List");
    @Unique
    private static final IChatComponent tab$exampleFooter = new ChatComponentText("VanillaHud");

    @Unique
    private int entryX, entryWidth;

    @Unique
    List<NetworkPlayerInfo> renderingList;

    @Unique
    private List<NetworkPlayerInfo> currentList;

    @ModifyVariable(method = "renderPlayerlist", at = @At(value = "STORE", ordinal = 0), ordinal = 9)
    private int resetY(int y) {
        return 1;
    }

    @ModifyVariable(method = "renderPlayerlist", at = @At(value = "STORE", ordinal = 0), ordinal = 7)
    private int captureEntryWidth(int width) {
        entryWidth = width;
        return width;
    }

    @ModifyVariable(method = "renderPlayerlist", at = @At(value = "STORE", ordinal = 0), ordinal = 14)
    private int captureEntryX(int x) {
        entryX = x;
        return x;
    }

    @ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = 13))
    private int pingWidth(int constant) {
        int width = 3;
        if (TabList.TabHud.numberPing && TabList.TabHud.pingType) {
            int maxWidth = 0;
            for (NetworkPlayerInfo info : renderingList) {
                int textWidth = mc.fontRendererObj.getStringWidth(String.valueOf(info.getResponseTime()));
                if (textWidth > maxWidth) maxWidth = textWidth;
            }
            width += maxWidth;
        } else {
            width += 10;
        }
        return Math.max(width, 13);
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;header:Lnet/minecraft/util/IChatComponent;"))
    private IChatComponent modifyHeader(GuiPlayerTabOverlay instance) {
        if (!TabList.TabHud.showHeader) return null;
        if (HudManager.isPanelOpen()) return tab$exampleHeader;
        return header;
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;footer:Lnet/minecraft/util/IChatComponent;"))
    private IChatComponent modifyFooter(GuiPlayerTabOverlay instance) {
        if (!TabList.TabHud.showFooter) return null;
        if (HudManager.isPanelOpen()) return tab$exampleFooter;
        return footer;
    }

    @Inject(method = "renderPlayerlist", at = @At(value = "HEAD"))
    private void translate(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        TabList.TabHud hud = TabList.hud;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) (-width / 2) * hud.getScale() + hud.position.getCenterX(), hud.position.getY() + hud.getPaddingY(), 0);
        GlStateManager.scale(hud.getScale(), hud.getScale(), 1f);
    }

    @Inject(method = "renderPlayerlist", at = @At(value = "TAIL"))
    private void pop(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        GlStateManager.popMatrix();
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Ordering;sortedCopy(Ljava/lang/Iterable;)Ljava/util/List;"))
    private List<NetworkPlayerInfo> list(Ordering<NetworkPlayerInfo> instance, Iterable<NetworkPlayerInfo> elements) {
        List<NetworkPlayerInfo> list = instance.sortedCopy(elements);
        if (TabList.TabHud.selfAtTop && !VanillaHUD.isCompactTab()) {
            for (NetworkPlayerInfo info : list) {
                if (info.getGameProfile().getId().equals(mc.thePlayer.getGameProfile().getId())) {
                    list.remove(info);
                    list.add(0, info);
                    break;
                }
            }
        }
        return renderingList = HudManager.isPanelOpen() ? TabListManager.devInfo : list;
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 0))
    private void cancelRects(Args args) {
        TabHook.cancelRect = true;
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 1))
    private void captureWidth(Args args) {
        TabList.width = (int) args.get(2) - (int) args.get(0);
        TabList.height = args.get(3);
        TabHook.cancelRect = true;
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 3))
    private void captureHeight(Args args) {
        TabList.height = args.get(3);
        TabHook.cancelRect = true;
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 2))
    private void widget(Args args) {
        if (TabHook.gettingSize) TabHook.cancelRect = true;
    }

    @ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = 20, ordinal = 0))
    private int limit(int constant) {
        return (HudManager.isPanelOpen() ? 10 : constant);
    }

    @Inject(method = "renderPlayerlist", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/NetworkPlayerInfo;getGameProfile()Lcom/mojang/authlib/GameProfile;", ordinal = 1),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawPing(IIILnet/minecraft/client/network/NetworkPlayerInfo;)V")
            ))
    private void preHeadTransform(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        if (TabHook.gettingSize) return;
        GlStateManager.pushMatrix();
        GlStateManager.translate(TabList.TabHud.showHead ? 0 : -8, 0, 0);
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    private int drawText(FontRenderer instance, String text, float x, float y, int color) {
        if (!TabHook.gettingSize) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            drawScaledString(text, x, y, color, TabList.TabHud.textType, 1);
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
        return 0;
    }

    @Inject(method = "renderPlayerlist", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I", shift = At.Shift.AFTER),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/NetworkPlayerInfo;getGameProfile()Lcom/mojang/authlib/GameProfile;", ordinal = 1),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawPing(IIILnet/minecraft/client/network/NetworkPlayerInfo;)V")
            ))
    private void postHeadTransform(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        if (TabHook.gettingSize) return;
        GlStateManager.popMatrix();
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawScaledCustomSizeModalRect(IIFFIIIIFF)V", ordinal = 0))
    private void playerHead(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        if (!TabList.TabHud.showHead || TabHook.gettingSize) return;

        Gui.drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawScaledCustomSizeModalRect(IIFFIIIIFF)V", ordinal = 1))
    private void playerHead1(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        if (!TabList.TabHud.showHead || TabHook.gettingSize) return;
        if (TabList.TabHud.betterHatLayer) {
            GlStateManager.translate(-0.5f, -0.5f, 0f);
            Gui.drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, 9, 9, tileWidth, tileHeight);
            GlStateManager.translate(0.5f, 0.5f, 0f);
        } else {
            Gui.drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
        }
    }

    @Unique
    private NetworkPlayerInfo info;

    @Inject(method = "drawScoreboardValues", at = @At("HEAD"))
    private void captureInfo(ScoreObjective scoreObjective, int i, String string, int j, int k, NetworkPlayerInfo networkPlayerInfo, CallbackInfo ci) {
        info = networkPlayerInfo;
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawScoreboardValues(Lnet/minecraft/scoreboard/ScoreObjective;ILjava/lang/String;IILnet/minecraft/client/network/NetworkPlayerInfo;)V"))
    private void scoreboard(GuiPlayerTabOverlay instance, ScoreObjective scoreObjective, int i, String string, int j, int k, NetworkPlayerInfo networkPlayerInfo) {
        if (TabHook.gettingSize) return;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        ((GuiPlayerTabOverlayAccessor) instance).renderScore(scoreObjective, i, string, j, k, networkPlayerInfo);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Redirect(method = "drawScoreboardValues", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I", ordinal = 1))
    private int translate(FontRenderer instance, String text, float x, float y, int color) {
        if (!TabHook.gettingSize) {
            int ping = info.getResponseTime();
            boolean offset = TabList.TabHud.numberPing && TabList.TabHud.hideFalsePing && (ping <= 1 || ping >= 999) || !TabList.TabHud.showPing;
            float textWidth = mc.fontRendererObj.getStringWidth(text);
            float textX = offset ? entryX + entryWidth - textWidth - 1 : x;
            drawScaledString(text, textX, y, color, TabList.TabHud.textType, 1F);
        }
        return 0;
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawPing(IIILnet/minecraft/client/network/NetworkPlayerInfo;)V"))
    private void redirectDrawPing(GuiPlayerTabOverlay instance, int width, int x, int y, NetworkPlayerInfo networkPlayerInfoIn) {
        if (TabHook.gettingSize) return;
        if (!TabList.TabHud.showPing) return;
        if (TabList.TabHud.numberPing) {
            int ping = networkPlayerInfoIn.getResponseTime();
            if (TabList.TabHud.hideFalsePing && (ping <= 1 || ping >= 999)) return;
            PolyColor color = tab$getColor(ping);
            String pingString = String.valueOf(ping);
            int textWidth = mc.fontRendererObj.getStringWidth(String.valueOf(ping));
            if (!TabList.TabHud.pingType) {
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                drawScaledString(pingString, 2 * (x + width) - textWidth - 4, 2 * y + 4, color.getRgba(), TabList.TabHud.textType, 1F);
                GlStateManager.scale(2F, 2F, 2F);
            } else
                drawScaledString(pingString, x + width - textWidth - 1, y, color.getRgba(), TabList.TabHud.textType, 1F);
        } else {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            ((GuiPlayerTabOverlayAccessor) instance).renderPing(width, x, y, networkPlayerInfoIn);
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    @Unique
    private static PolyColor tab$getColor(int ping) {
        return ping >= 400 ? TabList.TabHud.pingLevelSix
                : ping >= 300 ? TabList.TabHud.pingLevelFive
                : ping >= 200 ? TabList.TabHud.pingLevelFour
                : ping >= 145 ? TabList.TabHud.pingLevelThree
                : ping >= 75 ? TabList.TabHud.pingLevelTwo
                : TabList.TabHud.pingLevelOne;
    }

    @ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = 553648127))
    private int tabOpacity(int opacity) {
        return TabList.TabHud.tabWidgetColor.getRGB();
    }

    @ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = 80))
    private int changePlayerCount(int original) {
        return TabList.TabHud.getTabPlayerLimit();
    }

    @ModifyVariable(method = "renderPlayerlist", at = @At(value = "STORE"), ordinal = 0)
    private List<NetworkPlayerInfo> setLimit(List<NetworkPlayerInfo> value) {
        currentList = value.subList(0, Math.min(value.size(), TabList.TabHud.getTabPlayerLimit()));
        return currentList;
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I", ordinal = 1))
    private int noLimit(int a, int b) {
        if (!TabList.TabHud.fixWidth) return Math.min(a, b);
        return a;
    }

    @Inject(method = "renderPlayerlist", at = @At("TAIL"))
    private void alpha(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        GlStateManager.enableAlpha(); //somehow this fixes hud edit bug
    }

}
