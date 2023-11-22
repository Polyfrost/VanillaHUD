package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.*;
import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.hud.SingleTextHud;
import cc.polyfrost.oneconfig.libs.universal.*;
import cc.polyfrost.oneconfig.renderer.*;
import cc.polyfrost.oneconfig.utils.color.ColorUtils;
import org.polyfrost.vanillahud.mixin.GuiIngameAccessor;
import net.minecraft.util.EnumChatFormatting;

public class ItemTooltip extends Config {

    @HUD(
            name = "Item Tooltip"
    )
    public HeldItemTooltipHUD hud = new HeldItemTooltipHUD();


    public ItemTooltip(){
        super(new Mod("Item Tooltip", ModType.HUD, "/vanillahud_dark.svg"), "itemtooltip.json");
        initialize();
    }

    public static class HeldItemTooltipHUD extends SingleTextHud {

        @Exclude
        private int opacity;

        @Exclude
        private static final String EXAMPLE_TEXT = "Item Tooltip";

        public HeldItemTooltipHUD() {
            super("", true, 1920f / 2, 1080f - 32f, 1, false, false, 0, 0, 0, new OneColor(0, 0, 0, 80), false, 2, new OneColor(0, 0, 0));
            EventManager.INSTANCE.register(this);
        }

        @Override
        protected void drawLine(String line, float x, float y, float scale) {
            int color = ColorUtils.setAlpha(this.color.getRGB(), Math.min(this.color.getAlpha(), this.opacity));
            UGraphics.enableBlend();
            TextRenderer.drawScaledString(line, x, y, color | this.opacity << 24, TextRenderer.TextType.toType(this.textType), scale);
            UGraphics.disableBlend();
        }

        @Override
        protected boolean shouldShow() {
            GuiIngameAccessor ingameGUI = (GuiIngameAccessor) UMinecraft.getMinecraft().ingameGUI;

            opacity = (int) ((float) ingameGUI.getRemainingHighlightTicks() * 256.0F / 10.0F);
            if (opacity > 255) {
                opacity = 255;
            }
            return opacity > 0 && super.shouldShow();
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
            GuiIngameAccessor ingameGUI = (GuiIngameAccessor) UMinecraft.getMinecraft().ingameGUI;

            if (example) return EXAMPLE_TEXT;

            if (ingameGUI.getRemainingHighlightTicks() > 0 && ingameGUI.getHighlightingItemStack() != null) {
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
