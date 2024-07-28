package org.polyfrost.vanillahud.mixin.overflowanimations;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.Position;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.renderer.TextRenderer;
import com.google.common.collect.Ordering;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.polyfrost.vanillahud.hooks.TabHook;
import org.polyfrost.vanillahud.hud.TabList;
import org.polyfrost.vanillahud.mixin.minecraft.GuiPlayerTabOverlayAccessor;
import org.polyfrost.vanillahud.utils.TabListManager;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

@Pseudo
@Mixin(targets = {"org.polyfrost.overflowanimations.hooks.TabOverlayHook"})
public class TabOverlayHookMixin {

    @Dynamic
    @ModifyConstant(method = "renderOldTab", constant = @Constant(intValue = 10), remap = false)
    private static int modify(int constant) {
        return 1;
    }

    @Dynamic
    @ModifyArgs(method = "renderOldTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawRect(IIIII)V", ordinal = 0))
    private static void captureSize(Args args) {
        TabList.TabHud hud = TabList.hud;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) (-UResolution.getScaledWidth() / 2) * hud.getScale() + hud.position.getCenterX(), hud.position.getY() + hud.getPaddingY(), 0);
        GlStateManager.scale(hud.getScale(), hud.getScale(), 1f);
        TabList.width = (int) args.get(2) - (int) args.get(0);
        TabList.height = (int) args.get(3) - (int) args.get(1);
        TabHook.cancelRect = true;
        TabList.hud.drawBG();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        Position position = TabList.hud.position;
        int scale = (int) UResolution.getScaleFactor();
        GL11.glScissor((int) (position.getX() * scale), (int) ((UResolution.getScaledHeight() - position.getY() - TabList.animation.get()) * scale), (int) (position.getWidth() * scale), (int) (TabList.animation.get() * scale));
    }

    @Dynamic
    @Redirect(method = "renderOldTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    private static int redirectString(FontRenderer instance, String text, float x, float y, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        TextRenderer.drawScaledString(text, x, y, color, TextRenderer.TextType.toType(TabList.TabHud.textType), 1);
        GlStateManager.popMatrix();
        return 0;
    }

    @Dynamic
    @ModifyConstant(method = "renderOldTab", constant = @Constant(intValue = 553648127), remap = false)
    private static int widgetColor(int color) {
        return TabList.TabHud.tabWidgetColor.getRGB();
    }

    @Dynamic
    @Redirect(method = "renderOldTab", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Ordering;sortedCopy(Ljava/lang/Iterable;)Ljava/util/List;"))
    private static List<NetworkPlayerInfo> list(Ordering<NetworkPlayerInfo> instance, Iterable<NetworkPlayerInfo> elements) {
        return HudCore.editing ? TabListManager.devInfo : instance.sortedCopy(elements);
    }

    @Dynamic
    @Redirect(method = "renderOldTab", at = @At(value = "INVOKE", target = "Lorg/polyfrost/overflowanimations/mixin/interfaces/GuiPlayerTabOverlayInvoker;invokeDrawPing(IIILnet/minecraft/client/network/NetworkPlayerInfo;)V"))
    private static void redirectDrawPing(@Coerce Object instance, int width, int x, int y, NetworkPlayerInfo networkPlayerInfoIn) {
        if (TabHook.gettingSize) return;
        if (!TabList.TabHud.showPing) return;
        if (TabList.TabHud.numberPing) {
            int ping = networkPlayerInfoIn.getResponseTime();
            if (TabList.TabHud.hideFalsePing && (ping <= 1 || ping >= 999)) return;
            OneColor color = TabHook.getColor(ping);
            String pingString = String.valueOf(ping);
            int textWidth = UMinecraft.getMinecraft().fontRendererObj.getStringWidth(String.valueOf(ping));
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
            ((GuiPlayerTabOverlayAccessor) UMinecraft.getMinecraft().ingameGUI.getTabList()).renderPing(width, x, y, networkPlayerInfoIn);
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    @Dynamic
    @Inject(method = "renderOldTab", at = @At("TAIL"), remap = false)
    private static void pop(CallbackInfo ci) {
        GlStateManager.popMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

}