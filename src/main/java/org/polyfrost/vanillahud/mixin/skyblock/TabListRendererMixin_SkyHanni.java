package org.polyfrost.vanillahud.mixin.skyblock;

import at.hannibal2.skyhanni.config.features.misc.compacttablist.CompactTabListConfig;
import at.hannibal2.skyhanni.utils.TabListData;
import org.polyfrost.oneconfig.hud.Position;
import org.polyfrost.oneconfig.internal.hud.HudCore;
import org.polyfrost.universal.UResolution;
import org.polyfrost.oneconfig.renderer.TextRenderer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.polyfrost.vanillahud.hooks.TabHook;
import org.polyfrost.vanillahud.hud.TabList;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Pseudo
@Mixin(targets = "at.hannibal2.skyhanni.features.misc.compacttablist.TabListRenderer")
public class TabListRendererMixin_SkyHanni {

    @Unique
    private boolean shouldMove;

    @Dynamic
    @Inject(method = "onRenderOverlay*", at = @At("HEAD"), cancellable = true, remap = false)
    private void edit(CallbackInfo ci) {
        if (HudCore.editing) ci.cancel();
    }

    @Dynamic
    @Redirect(method = "onRenderOverlay*", at = @At(value = "FIELD", target = "Lat/hannibal2/skyhanni/config/features/misc/compacttablist/CompactTabListConfig;toggleTab:Z"))
    private boolean toggleTab(CompactTabListConfig config) {
        return false;
    }

    @Dynamic
    @ModifyArgs(method = "drawTabList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawRect(IIIII)V", ordinal = 0))
    private void captureSize(Args args) {
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
    @ModifyConstant(method = "drawTabList", constant = @Constant(intValue = 10), remap = false)
    private int modify(int constant) {
        return 3;
    }

    @Dynamic
    @Inject(method = "drawTabList", at = @At(value = "INVOKE", target = "Lat/hannibal2/skyhanni/features/misc/compacttablist/TabLine;getType()Lat/hannibal2/skyhanni/features/misc/compacttablist/TabStringType;", ordinal = 0), remap = false)
    private void reset(CallbackInfo ci) {
        shouldMove = false;
    }

    @Dynamic
    @Redirect(method = "drawTabList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawScaledCustomSizeModalRect(IIFFIIIIFF)V", ordinal = 0))
    private void playerHead(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        if (!TabList.TabHud.showHead) {
            shouldMove = true;
            return;
        }
        Gui.drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
    }

    @Dynamic
    @Redirect(method = "drawTabList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawScaledCustomSizeModalRect(IIFFIIIIFF)V", ordinal = 1))
    private void playerHat(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        if (!TabList.TabHud.showHead) return;
        if (TabList.TabHud.betterHatLayer) {
            GlStateManager.translate(-0.5f, -0.5f, 0f);
            Gui.drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, 9, 9, tileWidth, tileHeight);
            GlStateManager.translate(0.5f, 0.5f, 0f);
        } else {
            Gui.drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
        }
    }

    @Dynamic
    @ModifyVariable(method = "drawTabList", at = @At(value = "LOAD", ordinal = 6), name = "middleX", remap = false)
    private int removeHeadWidth2(int value) {
        return value - (shouldMove ? 10 : 0);
    }

    @Dynamic
    @ModifyConstant(method = "drawTabList", constant = @Constant(intValue = 0x20AAAAAA), remap = false)
    private int widgetColor(int color) {
        return TabList.TabHud.tabWidgetColor.getRGB();
    }

    @Dynamic
    @Redirect(method = "drawTabList", at = @At(value = "INVOKE", target = "Lat/hannibal2/skyhanni/utils/TabListData;getHeader()Ljava/lang/String;"), remap = false)
    private String modifyHeader(TabListData instance) {
        return TabList.TabHud.showHeader ? instance.getHeader() : "";
    }

    @Dynamic
    @Redirect(method = "drawTabList", at = @At(value = "INVOKE", target = "Lat/hannibal2/skyhanni/utils/TabListData;getFooter()Ljava/lang/String;"), remap = false)
    private String modifyFooter(TabListData instance) {
        return TabList.TabHud.showFooter ? instance.getFooter() : "";
    }

    @Dynamic
    @Redirect(method = "drawTabList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    private int redirectString(FontRenderer instance, String text, float x, float y, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        TextRenderer.drawScaledString(text, x, y, color, TextRenderer.TextType.toType(TabList.TabHud.textType), 1);
        GlStateManager.popMatrix();
        return 0;
    }

    @Dynamic
    @Inject(method = "drawTabList", at = @At("TAIL"), remap = false)
    private void pop(CallbackInfo ci) {
        GlStateManager.popMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

}