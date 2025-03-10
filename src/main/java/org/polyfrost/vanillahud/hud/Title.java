package org.polyfrost.vanillahud.hud;

import org.polyfrost.oneconfig.api.config.v1.annotations.*;
import org.polyfrost.polyui.color.PolyColor;
import org.polyfrost.oneconfig.api.config.v1.core.OneColor;
import org.polyfrost.oneconfig.hud.SingleTextHud;
import org.polyfrost.universal.*;
import org.polyfrost.oneconfig.renderer.*;
import org.polyfrost.oneconfig.utils.v1.color.ColorUtils;
import org.polyfrost.vanillahud.config.HudConfig;
import org.polyfrost.vanillahud.mixin.minecraft.GuiIngameAccessor;
import org.polyfrost.vanillahud.mixin.minecraft.MinecraftAccessor;

public class Title extends HudConfig {

    @HUD(
            name = "Title", category = "Title"
    )
    public TitleHUD titleHUD = new TitleHUD();

    @HUD(
            name = "Subtitle", category = "Subtitle"
    )
    public SubTitleHUD subtitleHUD = new SubTitleHUD();

    public Title() {
        super("Title", "title.json");
        initialize();
    }

    public static class TitleHUD extends SingleTextHud {

        @Switch(name = "Instant Fade")
        private static boolean instantFade = false;

        @Exclude
        protected int opacity;

        @Exclude
        private static final String EXAMPLE_TEXT = "Title";

        public TitleHUD() {
            this(1920f / 2, 1080f / 2 - 24f, 4);
        }

        public TitleHUD(float x, float y, float scale) {
            super("", true, x, y, scale, false, false, 2, 2, 2, new OneColor(0, 0, 0, 80), false, 2, new OneColor(0, 0, 0));
            this.textType = 1;
            showInDebug = true;
        }

        @Override
        protected void drawLine(String line, float x, float y, float scale) {
            int color = ColorUtils.setAlpha(this.color.getRGB(), Math.min(this.color.getAlpha(), this.opacity));
            UGraphics.enableBlend();
            TextRenderer.drawScaledString(line, x, y, color, TextRenderer.TextType.toType(textType), scale);
        }

        @Override
        protected void drawLine(String line, float x, float y, OneColor c, float scale) {
            int color = ColorUtils.setAlpha(c.getRGB(), Math.min(c.getAlpha(), this.opacity));
            UGraphics.enableBlend();
            TextRenderer.drawScaledString(line, x, y, color, TextRenderer.TextType.toType(textType), scale);
        }

        @Override
        protected boolean shouldShow() {
            GuiIngameAccessor ingameGUI = (GuiIngameAccessor) UMinecraft.getMinecraft().ingameGUI;
            if (ingameGUI == null || ingameGUI.getTitlesTimer() <= 0) {
                return false;
            }

            float age = (float) ingameGUI.getTitlesTimer() - ((MinecraftAccessor) UMinecraft.getMinecraft()).getTimer().renderPartialTicks;

            int o = 255;

            if (ingameGUI.getTitlesTimer() > ingameGUI.getTitleFadeOut() + ingameGUI.getTitleDisplayTime()) {
                float f = ingameGUI.getTitleFadeIn() + ingameGUI.getTitleFadeOut() + ingameGUI.getTitleDisplayTime() - age;
                o = (int) (f * 255 / ingameGUI.getTitleFadeIn());
            } else if (ingameGUI.getTitlesTimer() <= ingameGUI.getTitleFadeOut()) {
                o = (int) (age * 255 / ingameGUI.getTitleFadeOut());
            }

            if (o > 255) {
                o = 255;
            }
            opacity = instantFade ? 255 : o;
            return o > 8 && super.shouldShow();
        }

        protected void drawBackground(float x, float y, float width, float height, float scale) {
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            nanoVGHelper.setupAndDraw(true, (vg) -> {
                int bgColor = ColorUtils.setAlpha(this.bgColor.getRGB(), Math.min(this.bgColor.getAlpha(), this.opacity));
                int borderColor = ColorUtils.setAlpha(this.borderColor.getRGB(), Math.min(this.borderColor.getAlpha(), this.opacity));
                if (this.rounded) {
                    nanoVGHelper.drawRoundedRect(vg, x, y, width, height, bgColor, cornerRadius * scale);
                    if (this.border) {
                        nanoVGHelper.drawHollowRoundRect(vg, x - borderSize * scale, y - borderSize * scale, width + borderSize * scale, height + borderSize * scale, borderColor, cornerRadius * scale, borderSize * scale);
                    }
                } else {
                    nanoVGHelper.drawRect(vg, x, y, width, height, bgColor);
                    if (this.border) {
                        nanoVGHelper.drawHollowRoundRect(vg, x - borderSize * scale, y - borderSize * scale, width + borderSize * scale, height + borderSize * scale, borderColor, 0, borderSize * scale);
                    }
                }
            });
        }

        @Override
        protected String getText(boolean example) {
            GuiIngameAccessor ingameGUI = (GuiIngameAccessor) UMinecraft.getMinecraft().ingameGUI;

            if (example) {
                this.opacity = 255;
                return EXAMPLE_TEXT;
            }

            return ingameGUI.getDisplayedTitle();
        }

        public OneColor getColor() {
            return color;
        }
    }

    public static class SubTitleHUD extends TitleHUD {

        @Exclude
        private static final String EXAMPLE_TEXT = "Subtitle";

        public SubTitleHUD() {
            super(1920f / 2, 1080f / 2 + 18f, 2);
        }

        @Override
        protected String getText(boolean example) {
            GuiIngameAccessor ingameGUI = (GuiIngameAccessor) UMinecraft.getMinecraft().ingameGUI;

            if ((ingameGUI == null || ingameGUI.getDisplayedSubTitle().isEmpty() || !this.shouldShow()) && example) {
                this.opacity = 255;
                return EXAMPLE_TEXT;
            }

            return ingameGUI.getDisplayedSubTitle();
        }
    }
}

