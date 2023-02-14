package cc.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.hud.SingleTextHud;
import cc.polyfrost.oneconfig.libs.universal.UGraphics;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.TextRenderer;
import cc.polyfrost.oneconfig.utils.color.ColorUtils;
import cc.polyfrost.vanillahud.mixin.GuiIngameAccessor;
import cc.polyfrost.vanillahud.mixin.MinecraftAccessor;

import java.awt.Color;

public class ActionBar extends Config {

    @HUD(
            name = "Action bar"
    )
    public ActionBarHUD hud = new ActionBarHUD();

    public ActionBar() {
        super(new Mod("Action Bar", ModType.HUD, "/vanillahud_dark.svg"), "actionbar.json");
        initialize();
    }

    public static class ActionBarHUD extends SingleTextHud {

        @Exclude
        private float hue;
        @Exclude
        private int opacity;

        @Exclude
        private static final String EXAMPLE_TEXT = "Action Bar";

        @Switch(
                name = "Use Jukebox Rainbow Timer Color",
                description = "Use the rainbow timer color when a jukebox begins playing."
        )
        private boolean rainbowTimer = true;

        public ActionBarHUD() {
            super("", true, 1920f / 2, 1080f - 62f, 1, false, false, 0, 0, 0, new OneColor(0, 0, 0, 80), false, 2, new OneColor(0, 0, 0));
            EventManager.INSTANCE.register(this);
        }

        @Override
        protected void drawLine(String line, float x, float y, float scale) {
            GuiIngameAccessor ingameGUI = (GuiIngameAccessor) UMinecraft.getMinecraft().ingameGUI;
            int color = this.rainbowTimer && ingameGUI.getRecordIsPlaying() ? Color.HSBtoRGB(this.hue / 50.0F, 0.7F, 0.6F) & 16777215 : ColorUtils.setAlpha(this.color.getRGB(), Math.min(this.color.getAlpha(), this.opacity));
            UGraphics.enableBlend();
            TextRenderer.drawScaledString(line, x, y, color | this.opacity << 24, TextRenderer.TextType.toType(this.textType), scale);
            UGraphics.disableBlend();
        }

        @Override
        protected boolean shouldShow() {
            GuiIngameAccessor ingameGUI = (GuiIngameAccessor) UMinecraft.getMinecraft().ingameGUI;
            if (ingameGUI.getRecordPlayingUpFor() <= 0) {
                return false;
            }

            this.hue = (float) ingameGUI.getRecordPlayingUpFor() - ((MinecraftAccessor)UMinecraft.getMinecraft()).getTimer().renderPartialTicks;
            this.opacity = (int) (this.hue * 256.0f / 20.0f);
            if (this.opacity > 255) {
                this.opacity = 255;
            }

            return opacity > 0 && super.shouldShow();
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

            if (ingameGUI == null || ingameGUI.getRecordPlaying().isEmpty() || !this.shouldShow() && example) {
                this.opacity = 255;
                return EXAMPLE_TEXT;
            }

            return ingameGUI.getRecordPlaying();
        }
    }
}

