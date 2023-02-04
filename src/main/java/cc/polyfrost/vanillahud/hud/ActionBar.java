package cc.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.hud.BasicHud;
import cc.polyfrost.oneconfig.hud.SingleTextHud;
import cc.polyfrost.oneconfig.libs.universal.UGraphics;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.RenderManager;
import cc.polyfrost.oneconfig.renderer.TextRenderer;
import cc.polyfrost.oneconfig.utils.dsl.ColorUtilsDSLKt;
import cc.polyfrost.oneconfig.utils.dsl.RenderManagerDSLKt;
import cc.polyfrost.oneconfig.utils.dsl.VG;
import cc.polyfrost.vanillahud.VanillaHUD;
import cc.polyfrost.vanillahud.mixin.GuiIngameAccessor;
import cc.polyfrost.vanillahud.mixin.MinecraftAccessor;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.lang.Number;
import java.util.Collection;

public class ActionBar extends Config {

    @HUD(
            name = "Action bar"
    )
    public ActionBarHUD hud = new ActionBarHUD();

    public ActionBar() {
        super(new Mod("Action Bar", ModType.HUD), "actionbar.json");
        initialize();
    }

    public static class ActionBarHUD extends SingleTextHud {

        @Exclude
        private float hue;
        @Exclude
        private int opacity;

        @Exclude
        private final String EXAMPLE_TEXT = "None playing";

        @Switch(
                name = "Use Rainbow Timer"
        )
        private boolean rainbowTimer = true;

        public ActionBarHUD() {
            super("", true, 1920f / 2, 1080f - 62f, 1, false, false, 0, 0, 0, new OneColor(0, 0, 0, 80), false, 2, new OneColor(0, 0, 0));
            EventManager.INSTANCE.register(this);
        }

        @Override
        protected void drawLine(String line, float x, float y, float scale) {
            GuiIngameAccessor ingameGUI = (GuiIngameAccessor) UMinecraft.getMinecraft().ingameGUI;
            int color = this.rainbowTimer ? ingameGUI.getRecordIsPlaying() ? Color.HSBtoRGB(this.hue / 50.0F, 0.7F, 0.6F) & 16777215 : 16777215 : ColorUtilsDSLKt.setAlpha(this.color.getRGB(), RangesKt.coerceAtMost(this.color.getAlpha(), this.opacity));
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
                int bgColor = ColorUtilsDSLKt.setAlpha(this.bgColor.getRGB(), RangesKt.coerceAtMost(this.bgColor.getAlpha(), this.opacity));
                int borderColor = ColorUtilsDSLKt.setAlpha(this.borderColor.getRGB(), RangesKt.coerceAtMost(this.borderColor.getAlpha(), this.opacity));
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
            if (ingameGUI == null) {
                return this.EXAMPLE_TEXT;
            }

            return !ingameGUI.getRecordIsPlaying() && example ? this.EXAMPLE_TEXT : ingameGUI.getRecordPlaying();
        }
    }
}

