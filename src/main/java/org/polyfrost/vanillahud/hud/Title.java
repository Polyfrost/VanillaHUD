package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.*;
import cc.polyfrost.oneconfig.hud.SingleTextHud;
import cc.polyfrost.oneconfig.libs.universal.*;
import cc.polyfrost.oneconfig.renderer.*;
import cc.polyfrost.oneconfig.utils.color.ColorUtils;
import org.polyfrost.vanillahud.mixin.GuiIngameAccessor;
import org.polyfrost.vanillahud.mixin.MinecraftAccessor;

public class Title extends Config {

    @HUD(
            name = "Title", category = "Title"
    )
    public TitleHUD titleHUD = new TitleHUD();

    @HUD(
            name = "Subtitle", category = "Subtitle"
    )
    public SubTitleHUD subtitleHUD = new SubTitleHUD();

    public Title() {
        super(new Mod("Title", ModType.HUD, "/vanillahud_dark.svg"), "title.json");
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
            GuiIngameAccessor ingameGUI = (GuiIngameAccessor) UMinecraft.getMinecraft().ingameGUI;
            if (ingameGUI.getTitlesTimer() <= 0) {
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
            opacity = instantFade? 255 : o;
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

