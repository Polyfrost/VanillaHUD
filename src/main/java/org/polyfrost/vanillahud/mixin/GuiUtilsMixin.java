package org.polyfrost.vanillahud.mixin;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.input.Mouse;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.config.ModConfig;
import org.polyfrost.vanillahud.hooks.TooltipHook;
import org.polyfrost.vanillahud.hud.ScrollableTooltip;
import org.polyfrost.vanillahud.utils.EaseOutQuart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static org.polyfrost.vanillahud.hooks.TooltipHook.*;

@Mixin(value = GuiUtils.class, remap = false)
public class GuiUtilsMixin {

    @Unique
    private static int gui$tooltipY, gui$tooltipHeight;

    @Unique
    private static boolean gui$overScreen;

    @Unique
    private static List<String> gui$lines;

    @ModifyVariable(method = "drawHoveringText", at = @At("STORE"), name = "tooltipY")
    private static int captureY(int y) {
        gui$tooltipY = gui$overScreen && ModConfig.scrollableTooltip.enabled && ScrollableTooltip.startAtTop ? 6 : y;
        return gui$tooltipY;
    }

    @ModifyVariable(method = "drawHoveringText", at = @At("STORE"), name = "tooltipHeight")
    private static int captureHeight(int height) {
        gui$tooltipHeight = height;
        return height;
    }

    @Inject(method = "drawHoveringText", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/config/GuiUtils;drawGradientRect(IIIIIII)V", ordinal = 0))
    private static void setY(List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, FontRenderer font, CallbackInfo ci) {
        float current = gui$animationY.get();
        int top = gui$tooltipY - 4;
        int bottom = gui$tooltipY + gui$tooltipHeight + 3;
        int height = bottom - top;
        gui$overScreen = height + 4 > screenHeight;
        if (!textLines.equals(gui$lines)) {
            TooltipHook.resetScrolling();
            gui$lines = textLines;
        }
        TooltipHook.isScrolling = ModConfig.scrollableTooltip.enabled && (gui$overScreen || top < 0);
        int mouseDWheel = Mouse.getDWheel();
        if (TooltipHook.isScrolling) {
            if (mouseDWheel < 0) {
                gui$scrollY -= 10;
            } else if (mouseDWheel > 0) {
                gui$scrollY += 10;
            }
            gui$scrollY = MathHelper.clamp_int(gui$scrollY, screenHeight - bottom - 5, 2 - top);

        }
        if (gui$scrollY != gui$animationY.getEnd()) {
            gui$animationY = new EaseOutQuart(200, current, gui$scrollY, false);
        }
        GlStateManager.translate(0, gui$animationY.get(), 0);
    }

    @Inject(method = "drawHoveringText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;enableLighting()V"))
    private static void pop(List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, FontRenderer font, CallbackInfo ci) {
        GlStateManager.translate(0, -gui$animationY.get(), 0);
    }

}