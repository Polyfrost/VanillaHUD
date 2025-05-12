package org.polyfrost.vanillahud.hud;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.config.v1.core.OneColor;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.hud.v1.TextHud;
import org.polyfrost.oneconfig.hud.SingleTextHud;
import org.polyfrost.oneconfig.renderer.*;
import org.polyfrost.oneconfig.utils.v1.color.ColorUtils;
import org.polyfrost.universal.*;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.mixin.minecraft.GuiIngameAccessor;
import org.polyfrost.vanillahud.mixin.minecraft.MinecraftAccessor;

import java.awt.*;

public class ActionBar extends TextHud {

    @HUD(
            name = "Action Bar"
    )
    public ActionBarHUD hud = new ActionBarHUD();

    public ActionBar() {
        super("Action Bar", "actionbar.json");
        initialize();
    }

    @Override
    protected @Nullable String getText() {
        return "";
    }

    @Override
    public @NotNull String title() {
        return "Action Bar";
    }

    @Override
    public @NotNull Category category() {
        return Category.getINFO();
    }

    @Override
    public boolean multipleInstancesAllowed() {
        return false;
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
            showInDebug = true;
        }

        @Override
        protected void drawLine(String line, float x, float y, float scale) {
            GuiIngameAccessor ingameGUI = (GuiIngameAccessor) UMinecraft.getMinecraft().ingameGUI;
            int color = this.rainbowTimer && ingameGUI.getRecordIsPlaying() ? Color.HSBtoRGB(this.hue / 50.0F, 0.7F, 0.6F) & 16777215 : ColorUtils.setAlpha(this.color.getRGB(), Math.min(this.color.getAlpha(), this.opacity));
            UGraphics.enableBlend();
            TextRenderer.drawScaledString(line, x, y, color | this.opacity << 24, TextRenderer.TextType.toType(this.textType), scale);
        }

        @Override
        protected boolean shouldShow() {
            if (VanillaHUD.isApec()) { // I love Apec Mod Minecraft
                return false;
            }
            GuiIngameAccessor ingameGUI = (GuiIngameAccessor) Minecraft.getMinecraft().ingameGUI;
            if (ingameGUI.getRecordPlayingUpFor() <= 0 || ingameGUI.getRecordPlaying() == null || ingameGUI.getRecordPlaying().isEmpty()) {
                return false;
            }

            this.hue = (float) ingameGUI.getRecordPlayingUpFor() - ((MinecraftAccessor) Minecraft.getMinecraft()).getTimer().renderPartialTicks;
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

            if (ingameGUI == null || ingameGUI.getRecordPlaying() == null || ingameGUI.getRecordPlaying().isEmpty() || !this.shouldShow() && example) {
                this.opacity = 255;
                return EXAMPLE_TEXT;
            }

            return ingameGUI.getRecordPlaying();
        }
    }
}

