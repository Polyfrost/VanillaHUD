package cc.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.hud.BasicHud;
import cc.polyfrost.oneconfig.libs.universal.UGraphics;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.TextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collection;

public class Scoreboard extends Config {

    @HUD(
            name = "Scoreboard"
    )
    public ScoreboardHUD hud = new ScoreboardHUD();

    public Scoreboard() {
        super(new Mod("Scoreboard", ModType.HUD, "/vanillahud_dark.svg"), "scoreboard.json");
        initialize();
    }

    public static class ScoreboardHUD extends BasicHud {

        @Switch(
                name = "Show Score Points",
                category = "Score Points"
        )
        public boolean scoreboardPoints = false;

        @Color(
                name = "Score Points Color"
        )
        public OneColor scorePointsColor = new OneColor(255, 85, 85);

        @Switch(
                name = "Scoreboard Title"
        )
        public boolean scoreboardTitle = true;

        @Color(
                name = "Title Background Color"
        )
        public OneColor titleColor = new OneColor(0, 0, 0, 96);

        @Dropdown(
                name = "Text Type",
                options = {"No Shadow", "Shadow", "Full Shadow"}
        )
        public int textType = 0;

        /** Gets OneConfig's Universal Minecraft instance. */
        @Exclude
        public final Minecraft mc = UMinecraft.getMinecraft();

        /** Gets OneConfig's Universal Minecraft fontRenderer. */
        @Exclude public final FontRenderer fontRenderer = UMinecraft.getFontRenderer();

        @Exclude public float width = 0f;
        @Exclude public float height = 0f;

        public ScoreboardHUD() {
            super(true, 1919, 1080f / 2f, 1, true, false, 0, 1, 0, new OneColor(0, 0, 0, 80), false, 2, new OneColor(0, 0, 0));
            EventManager.INSTANCE.register(this);
        }

        @Override
        public void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
            UGraphics.GL.pushMatrix();
            UGraphics.GL.scale(scale, scale, 1);
            UGraphics.GL.translate(x / scale, y / scale, 1);

            ScoreObjective objective = this.mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);

            if (objective == null) {
                if (example) {
                    net.minecraft.scoreboard.Scoreboard scoreboard = new net.minecraft.scoreboard.Scoreboard();
                    objective = new ScoreObjective(scoreboard, "OneConfig", IScoreObjectiveCriteria.DUMMY);
                    objective.setDisplayName(EnumChatFormatting.AQUA + "" + EnumChatFormatting.BOLD + "Scoreboard");
                    scoreboard.getValueFromObjective("Drag me around!", objective);
                    scoreboard.getValueFromObjective("Click to drag", objective);
                } else {
                    return;
                }
            }

            this.renderObjective(objective);
            UGraphics.GL.popMatrix();
        }

        @Override
        protected boolean shouldShow() {
            return this.mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1) != null && super.shouldShow();
        }

        private void renderObjective(ScoreObjective scoreObjective) {
            UGraphics.enableBlend();
            net.minecraft.scoreboard.Scoreboard scoreboard = scoreObjective.getScoreboard();
            Collection<Score> sortedScores = scoreboard.getSortedScores(scoreObjective);
            boolean showScorePoints = this.scoreboardPoints;
            if (sortedScores.size() <= 15) {
                String displayName = scoreObjective.getDisplayName();
                int displayNameStringWidth = this.fontRenderer.getStringWidth(displayName);
                for (Score score : sortedScores) {
                    ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
                    String totalString = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()) + (showScorePoints ? ": " + EnumChatFormatting.RED + score.getScorePoints() : "");
                    displayNameStringWidth = Math.max(displayNameStringWidth, this.fontRenderer.getStringWidth(totalString));
                }

                if (this.scoreboardTitle) {
                    TextRenderer.drawScaledString(displayName, this.width / 2.0f - this.fontRenderer.getStringWidth(displayName) / 2.0f, 1 - this.paddingY, -1, TextRenderer.TextType.toType(textType), 1);
                }

                UGraphics.GL.translate(0.0f, this.height, 0.0f);
                int counter = 0;
                for (Score score : sortedScores) {
                    ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
                    String playerName = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName());
                    float yPos = -++counter * this.fontRenderer.FONT_HEIGHT;
                    TextRenderer.drawScaledString(playerName, 1, yPos, -1, TextRenderer.TextType.toType(textType), 1);

                    if (showScorePoints) {
                        String scorePoints = "" + score.getScorePoints();
                        TextRenderer.drawScaledString(scorePoints, this.width - this.fontRenderer.getStringWidth(scorePoints) - 1, yPos, this.scorePointsColor.getRGB(), TextRenderer.TextType.toType(textType), 1);
                    }
                }

                UGraphics.disableBlend();

                this.width = displayNameStringWidth + 2;
                this.height = sortedScores.size() * this.fontRenderer.FONT_HEIGHT + (this.scoreboardTitle ? 10 : 1);
            }
        }

        protected void drawBackground(float x, float y, float width, float height, float scale) {
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            nanoVGHelper.setupAndDraw(true, (vg) -> {
                if (this.rounded) {
                    if (this.scoreboardTitle) {
                        nanoVGHelper.drawRoundedRectVaried(vg, x, y, width, this.fontRenderer.FONT_HEIGHT, this.titleColor.getRGB(), cornerRadius * scale, cornerRadius * scale, 0, 0);
                        nanoVGHelper.drawRoundedRectVaried(vg, x, y + this.fontRenderer.FONT_HEIGHT, width, height - this.fontRenderer.FONT_HEIGHT, bgColor.getRGB(), 0, 0, cornerRadius * scale, cornerRadius * scale);
                    } else {
                        nanoVGHelper.drawRoundedRect(vg, x, y, width, height, bgColor.getRGB(), cornerRadius * scale);
                    }
                    if (this.border) {
                        nanoVGHelper.drawHollowRoundRect(vg, x - borderSize * scale, y - borderSize * scale, width + borderSize * scale, height + borderSize * scale, borderColor.getRGB(), cornerRadius * scale, borderSize * scale);
                    }
                } else {
                    if (this.scoreboardTitle) {
                        nanoVGHelper.drawRect(vg, x, y, width, this.fontRenderer.FONT_HEIGHT, this.titleColor.getRGB());
                        nanoVGHelper.drawRect(vg, x, y + this.fontRenderer.FONT_HEIGHT, width, height - this.fontRenderer.FONT_HEIGHT, bgColor.getRGB());
                    } else {
                        nanoVGHelper.drawRect(vg, x, y, width, height, bgColor.getRGB());
                    }
                    if (this.border) {
                        nanoVGHelper.drawHollowRoundRect(vg, x - borderSize * scale, y - borderSize * scale, width + borderSize * scale, height + borderSize * scale, borderColor.getRGB(), 0, borderSize * scale);
                    }
                }
            });
        }

        @Override
        protected float getWidth(float scale, boolean example) {
            return this.width * scale;
        }

        @Override
        protected float getHeight(float scale, boolean example) {
            return this.height * scale;
        }
    }
}

