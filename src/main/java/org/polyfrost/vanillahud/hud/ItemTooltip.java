package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.*;
import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.hud.SingleTextHud;
import cc.polyfrost.oneconfig.libs.universal.*;
import cc.polyfrost.oneconfig.renderer.*;
import cc.polyfrost.oneconfig.utils.color.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.spectator.*;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.config.HudConfig;
import org.polyfrost.vanillahud.mixin.*;
import net.minecraft.util.EnumChatFormatting;

public class ItemTooltip extends HudConfig {

    @HUD(
            name = "Held Item Tooltip"
    )
    public HeldItemTooltipHUD hud = new HeldItemTooltipHUD();


    public ItemTooltip() {
        super("Held Item Tooltip", "itemtooltip.json");
    }

    public static class HeldItemTooltipHUD extends SingleTextHud {

        @Switch(name = "Fade Out")
        private static boolean fadeOut = true;

        @Switch(name = "Instant Fade")
        private static boolean instantFade = false;

        @Exclude
        private int opacity;

        @Exclude
        private static String specText;

        @Exclude
        private static final String EXAMPLE_TEXT = "Item Tooltip";

        @Exclude
        private static final Minecraft mc = UMinecraft.getMinecraft();

        public HeldItemTooltipHUD() {
            super("", true, 1920f / 2, 1080f - 37f, 1, false, false, 0, 0, 0, new OneColor(0, 0, 0, 80), false, 2, new OneColor(0, 0, 0));
            this.textType = 1;
            EventManager.INSTANCE.register(this);
        }

        @Override
        protected void drawLine(String line, float x, float y, float scale) {
            OneColor color = new OneColor(ColorUtils.setAlpha(this.color.getRGB(), Math.min(this.color.getAlpha(), this.opacity)) | this.opacity << 24);
            UGraphics.enableBlend();
            super.drawLine(line, x, y, color, scale);
            UGraphics.disableBlend();
        }

        @Override
        protected void drawLine(String line, float x, float y, OneColor c, float scale) {
            OneColor color = new OneColor(ColorUtils.setAlpha(c.getRGB(), Math.min(c.getAlpha(), this.opacity)) | this.opacity << 24);
            UGraphics.enableBlend();
            super.drawLine(line, x, y, color, scale);
            UGraphics.disableBlend();
        }

        @Override
        protected boolean shouldShow() {
            if (VanillaHUD.isApec()) { // I love Apec Mod Minecraft
                return false;
            }
            GuiIngameAccessor ingameGUI = (GuiIngameAccessor) mc.ingameGUI;

            int o = fadeOut ? (int) ((float) ingameGUI.getRemainingHighlightTicks() * 256.0F / 10.0F) : 255;
            if (o > 255) {
                o = 255;
            }
            opacity = instantFade ? 255 : o;
            String spectatorText = null;
            if (mc.thePlayer != null && mc.thePlayer.isSpectator()) {
                GuiSpectatorAccessor spectatorAccessor = (GuiSpectatorAccessor) mc.ingameGUI.getSpectatorGui();
                int i = (int)(spectatorAccessor.alpha() * 255.0F);
                if (i > 3 && spectatorAccessor.getField_175271_i() != null) {
                    ISpectatorMenuObject iSpectatorMenuObject = spectatorAccessor.getField_175271_i().func_178645_b();
                    spectatorText = iSpectatorMenuObject != SpectatorMenu.field_178657_a ? iSpectatorMenuObject.getSpectatorName().getFormattedText() : spectatorAccessor.getField_175271_i().func_178650_c().func_178670_b().getFormattedText();
                    if (spectatorText != null) {
                        specText = spectatorText;
                        opacity = instantFade ? 255 : i;
                    }
                }
            }

            return (o > 0 || spectatorText != null) && super.shouldShow();
        }

        protected void drawBackground(float x, float y, float width, float height, float scale) {

            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            nanoVGHelper.setupAndDraw(true, (vg) -> {
                int bgColor = ColorUtils.setAlpha(this.bgColor.getRGB(), Math.min(this.bgColor.getAlpha(), this.opacity));
                int borderColor = ColorUtils.setAlpha(this.borderColor.getRGB(), Math.min(this.borderColor.getAlpha(), this.opacity));
                if (rounded) {
                    nanoVGHelper.drawRoundedRect(vg, x, y, width, height, bgColor, cornerRadius * scale);
                    if (border)
                        nanoVGHelper.drawHollowRoundRect(vg, x - borderSize * scale, y - borderSize * scale, width + borderSize * scale, height + borderSize * scale, borderColor, cornerRadius * scale, borderSize * scale);
                } else {
                    nanoVGHelper.drawRect(vg, x, y, width, height, bgColor);
                    if (border)
                        nanoVGHelper.drawHollowRoundRect(vg, x - borderSize * scale, y - borderSize * scale, width + borderSize * scale, height + borderSize * scale, borderColor, 0, borderSize * scale);
                }
            });
        }

        @Override
        protected String getText(boolean example) {
            GuiIngameAccessor ingameGUI = (GuiIngameAccessor) mc.ingameGUI;
            if (example) return EXAMPLE_TEXT;
            if (mc.thePlayer != null && mc.thePlayer.isSpectator()) {
                GuiSpectatorAccessor spectatorAccessor = (GuiSpectatorAccessor) mc.ingameGUI.getSpectatorGui();
                int i = (int)(spectatorAccessor.alpha() * 255.0F);
                if (i > 3 && spectatorAccessor.getField_175271_i() != null) {
                    return specText;
                }
            } else if ((ingameGUI.getRemainingHighlightTicks() > 0 || !fadeOut) && ingameGUI.getHighlightingItemStack() != null) {
                String string = ingameGUI.getHighlightingItemStack().getDisplayName();
                if (ingameGUI.getHighlightingItemStack().hasDisplayName()) {
                    string = EnumChatFormatting.ITALIC + string;
                }
                return string;

            }
            return "";
        }
    }
}
