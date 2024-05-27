package org.polyfrost.vanillahud.mixin;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import cc.polyfrost.oneconfig.renderer.TextRenderer;
import cc.polyfrost.oneconfig.utils.color.ColorUtils;
import com.google.common.collect.Ordering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.*;
import net.minecraft.util.*;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.hooks.TabHook;
import org.polyfrost.vanillahud.hud.TabList;
import org.polyfrost.vanillahud.utils.DummyClassUtils;
import org.polyfrost.vanillahud.utils.TabListManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Iterator;
import java.util.List;

// 1100 priority to load before Patcher
@Mixin(value = GuiPlayerTabOverlay.class, priority = 1100)
public abstract class GuiPlayerTabOverlayMixin {

    @Shadow
    private IChatComponent header, footer;

    @Shadow @Final private Minecraft mc;
    @Shadow @Final private static Ordering<NetworkPlayerInfo> field_175252_a;

    @Shadow public abstract String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn);

    @Unique
    private static final IChatComponent tab$exampleHeader = new ChatComponentText("Tab List");
    @Unique
    private static final IChatComponent tab$exampleFooter = new ChatComponentText("VanillaHud");

    @Unique
    private int entryX, entryWidth, tab$TRANSPARENT = ColorUtils.getColor(0, 0, 0, 0);

    @Unique
    List<NetworkPlayerInfo> renderingList;

    @Unique
    private List<NetworkPlayerInfo> currentList;

    @ModifyVariable(method = "renderPlayerlist", at = @At(value = "STORE", ordinal = 0), ordinal = 9)
    private int resetY(int y) {
        if (VanillaHUD.inSBASkyblock()) return y;
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
        if (VanillaHUD.inSBASkyblock()) return constant;
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
        if (VanillaHUD.inSBASkyblock()) return header;
        if (!TabList.TabHud.showHeader) return null;
        if (HudCore.editing) return tab$exampleHeader;
        return header;
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;footer:Lnet/minecraft/util/IChatComponent;"))
    private IChatComponent modifyFooter(GuiPlayerTabOverlay instance) {
        if (VanillaHUD.inSBASkyblock()) return footer;
        if (!TabList.TabHud.showFooter) return null;
        if (HudCore.editing) return tab$exampleFooter;
        return footer;
    }

    @Inject(method = "renderPlayerlist", at = @At(value = "HEAD"), cancellable = true)
    private void translate(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        if (TabHook.gettingSize) {
            vanillaHUD$setSize(width, scoreboardIn, scoreObjectiveIn);
            TabHook.gettingSize = false;
            ci.cancel();
            return;
        }
        if (VanillaHUD.inSBASkyblock()) return;
        TabList.TabHud hud = TabList.hud;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) (-width / 2) * hud.getScale() + hud.position.getCenterX(), hud.position.getY() + hud.getPaddingY(), 0);
        GlStateManager.scale(hud.getScale(), hud.getScale(), 1f);
        if (DummyClassUtils.willPatcherShiftDown()) {
            GlStateManager.translate(0, -DummyClassUtils.patcherTabHeight(), 0);
        }
    }

    @Inject(method = "renderPlayerlist", at = @At(value = "TAIL"))
    private void pop(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        if (VanillaHUD.inSBASkyblock()) return;
        GlStateManager.popMatrix();
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Ordering;sortedCopy(Ljava/lang/Iterable;)Ljava/util/List;"))
    private List<NetworkPlayerInfo> list(Ordering<NetworkPlayerInfo> instance, Iterable<NetworkPlayerInfo> elements) {
        if (VanillaHUD.inSBASkyblock()) return instance.sortedCopy(elements);
        List<NetworkPlayerInfo> list = instance.sortedCopy(elements);
        if (TabList.TabHud.selfAtTop) {
            for (NetworkPlayerInfo info : list) {
                if (info.getGameProfile().getId().equals(mc.thePlayer.getGameProfile().getId())) {
                    list.remove(info);
                    list.add(0, info);
                    break;
                }
            }
        }
        return renderingList = HudCore.editing ? TabListManager.devInfo : list;
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 0))
    private void cancelRect(Args args) {
        if (VanillaHUD.inSBASkyblock()) return;
        args.set(4, tab$TRANSPARENT);
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 1))
    private void cancelRect1(Args args) {
        if (VanillaHUD.inSBASkyblock()) return;
        args.set(4, tab$TRANSPARENT);
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 3))
    private void cancelRect2(Args args) {
        if (VanillaHUD.inSBASkyblock()) return;
        args.set(4, tab$TRANSPARENT);
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 2))
    private void fixWidth(Args args) {
        if (VanillaHUD.inSBASkyblock()) return;
        args.set(2, (int) args.get(2) - 1);
    }

    @ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = 20, ordinal = 0))
    private int limit(int constant) {
        if (VanillaHUD.inSBASkyblock()) return constant;
        return (HudCore.editing ? 10 : constant);
    }

    @Inject(method = "renderPlayerlist", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/NetworkPlayerInfo;getGameProfile()Lcom/mojang/authlib/GameProfile;", ordinal = 1),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawPing(IIILnet/minecraft/client/network/NetworkPlayerInfo;)V")
            ))
    private void preHeadTransform(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        if (VanillaHUD.inSBASkyblock()) return;
        GlStateManager.pushMatrix();
        GlStateManager.translate(TabList.TabHud.showHead ? 0 : -8, 0, 0);
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    private int drawText(FontRenderer instance, String text, float x, float y, int color) {
        if (VanillaHUD.inSBASkyblock()) return instance.drawStringWithShadow(text, x, y, color);
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        TextRenderer.drawScaledString(text, x, y, color, TextRenderer.TextType.toType(TabList.TabHud.textType), 1);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        return 0;
    }

    @Inject(method = "renderPlayerlist", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I", shift = At.Shift.AFTER),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/NetworkPlayerInfo;getGameProfile()Lcom/mojang/authlib/GameProfile;", ordinal = 1),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawPing(IIILnet/minecraft/client/network/NetworkPlayerInfo;)V")
            ))
    private void postHeadTransform(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        if (VanillaHUD.inSBASkyblock()) return;
        GlStateManager.popMatrix();
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawScaledCustomSizeModalRect(IIFFIIIIFF)V", ordinal = 0))
    private void playerHead(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        if (!TabList.TabHud.showHead && !VanillaHUD.inSBASkyblock()) return;

        Gui.drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawScaledCustomSizeModalRect(IIFFIIIIFF)V", ordinal = 1))
    private void playerHead1(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        if (!TabList.TabHud.showHead && !VanillaHUD.inSBASkyblock()) return;
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
        if (VanillaHUD.inSBASkyblock()) return instance.drawStringWithShadow(text, x, y, color);
        int ping = info.getResponseTime();
        boolean offset = TabList.TabHud.numberPing && TabList.TabHud.hideFalsePing && (ping <= 1 || ping >= 999) || !TabList.TabHud.showPing;
        float textWidth = mc.fontRendererObj.getStringWidth(text);
        float textX = offset ? entryX + entryWidth - textWidth - 1 : x;
        TextRenderer.drawScaledString(text, textX, y, color, TextRenderer.TextType.toType(TabList.TabHud.textType), 1F);
        return 0;
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawPing(IIILnet/minecraft/client/network/NetworkPlayerInfo;)V"))
    private void redirectDrawPing(GuiPlayerTabOverlay instance, int width, int x, int y, NetworkPlayerInfo networkPlayerInfoIn) {
        if (VanillaHUD.inSBASkyblock()) {
            ((GuiPlayerTabOverlayAccessor) instance).renderPing(width, x, y, networkPlayerInfoIn);
            return;
        }
        if (!TabList.TabHud.showPing) return;
        if (TabList.TabHud.numberPing) {
            int ping = networkPlayerInfoIn.getResponseTime();
            if (TabList.TabHud.hideFalsePing && (ping <= 1 || ping >= 999)) return;
            OneColor color = tab$getColor(ping);
            String pingString = String.valueOf(ping);
            int textWidth = mc.fontRendererObj.getStringWidth(String.valueOf(ping));
            if (!TabList.TabHud.pingType) {
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                TextRenderer.drawScaledString(pingString, 2 * (x + width) - textWidth - 4, 2 * y + 4, color.getRGB(), TextRenderer.TextType.toType(TabList.TabHud.textType), 1F);
                GlStateManager.scale(2F, 2F, 2F);
            } else
                TextRenderer.drawScaledString(pingString, x + width - textWidth - 1, y, color.getRGB(), TextRenderer.TextType.toType(TabList.TabHud.textType), 1F);
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
    private static OneColor tab$getColor(int ping) {
        return ping >= 400 ? TabList.TabHud.pingLevelSix
                : ping >= 300 ? TabList.TabHud.pingLevelFive
                : ping >= 200 ? TabList.TabHud.pingLevelFour
                : ping >= 145 ? TabList.TabHud.pingLevelThree
                : ping >= 75 ? TabList.TabHud.pingLevelTwo
                : TabList.TabHud.pingLevelOne;
    }

    @ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = 553648127))
    private int tabOpacity(int opacity) {
        if (VanillaHUD.inSBASkyblock()) return opacity;
        return TabList.TabHud.tabWidgetColor.getRGB();
    }

    @ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = 80))
    private int changePlayerCount(int original) {
        if (VanillaHUD.inSBASkyblock()) return original;
        return TabList.TabHud.getTabPlayerLimit();
    }

    @ModifyVariable(method = "renderPlayerlist", at = @At(value = "STORE"), ordinal = 0)
    private List<NetworkPlayerInfo> setLimit(List<NetworkPlayerInfo> value) {
        currentList = VanillaHUD.inSBASkyblock() ? value : value.subList(0, Math.min(value.size(), TabList.TabHud.getTabPlayerLimit()));
        return currentList;
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I", ordinal = 1))
    private int noLimit(int a, int b) {
        if (VanillaHUD.inSBASkyblock() || !TabList.TabHud.fixWidth) return Math.min(a, b);
        return a;
    }

    @Inject(method = "renderPlayerlist", at = @At("TAIL"))
    private void alpha(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        GlStateManager.enableAlpha(); //somehow this fixes hud edit bug
    }

    @Unique
    private void vanillaHUD$setSize(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn) {
        NetHandlerPlayClient netHandlerPlayClient = this.mc.thePlayer.sendQueue;
        List<NetworkPlayerInfo> list = setLimit(list(field_175252_a, field_175252_a.sortedCopy(netHandlerPlayClient.getPlayerInfoMap())));
        int i = 0;
        int j = 0;
        Iterator iterator = list.iterator();

        int k;
        while(iterator.hasNext()) {
            NetworkPlayerInfo networkPlayerInfo = (NetworkPlayerInfo)iterator.next();
            k = this.mc.fontRendererObj.getStringWidth(this.getPlayerName(networkPlayerInfo));
            i = Math.max(i, k);
            if (scoreObjectiveIn != null && scoreObjectiveIn.getRenderType() != IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
                k = this.mc.fontRendererObj.getStringWidth(" " + scoreboardIn.getValueFromObjective(networkPlayerInfo.getGameProfile().getName(), scoreObjectiveIn).getScorePoints());
                j = Math.max(j, k);
            }
        }

        list = list.subList(0, Math.min(list.size(), changePlayerCount(80)));
        int l = list.size();
        int m = l;

        for(k = 1; m > limit(20); m = (l + k - 1) / k) {
            ++k;
        }

        boolean bl = this.mc.isIntegratedServerRunning() || this.mc.getNetHandler().getNetworkManager().getIsencrypted();
        int n;
        if (scoreObjectiveIn != null) {
            if (scoreObjectiveIn.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
                n = 90;
            } else {
                n = j;
            }
        } else {
            n = 0;
        }

        int o = noLimit(k * ((bl ? 9 : 0) + i + n + pingWidth(13)), width - 50) / k;
        int q = 1;
        int r = o * k + (k - 1) * 5;
        List<String> list2 = null;
        List<String> list3 = null;
        Iterator iterator2;
        String string;
        if (modifyHeader((GuiPlayerTabOverlay) (Object) this) != null) {
            list2 = this.mc.fontRendererObj.listFormattedStringToWidth(modifyHeader((GuiPlayerTabOverlay) (Object) this).getFormattedText(), width - 50);

            for(iterator2 = list2.iterator(); iterator2.hasNext(); r = Math.max(r, this.mc.fontRendererObj.getStringWidth(string))) {
                string = (String)iterator2.next();
            }
        }

        if (modifyFooter((GuiPlayerTabOverlay) (Object) this) != null) {
            list3 = this.mc.fontRendererObj.listFormattedStringToWidth(modifyFooter((GuiPlayerTabOverlay) (Object) this).getFormattedText(), width - 50);

            for(iterator2 = list3.iterator(); iterator2.hasNext(); r = Math.max(r, this.mc.fontRendererObj.getStringWidth(string))) {
                string = (String)iterator2.next();
            }
        }

        TabList.width = r + 2;

        if (list2 != null) {
            q += this.mc.fontRendererObj.FONT_HEIGHT * list2.size();

            ++q;
        }

        TabList.height = q + m * 9;

        if (list3 != null) {
            q += m * 9 + 1;
            TabList.height = q + list3.size() * this.mc.fontRendererObj.FONT_HEIGHT;
        }
    }

}
