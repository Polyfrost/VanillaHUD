package org.polyfrost.vanillahud.hud;

import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.annotations.*;
import org.polyfrost.polyui.color.PolyColor;
import org.polyfrost.oneconfig.api.config.v1.core.OneColor;
import org.polyfrost.oneconfig.api.config.v1.data.*;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.hud.BasicHud;
import org.polyfrost.universal.UGraphics;
import org.polyfrost.universal.UMatrixStack;
import org.polyfrost.universal.UMinecraft;
import org.polyfrost.oneconfig.renderer.NanoVGHelper;
import org.polyfrost.oneconfig.renderer.TextRenderer;
import org.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.scoreboard.*;
import net.minecraft.util.EnumChatFormatting;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.config.HudConfig;
import org.polyfrost.vanillahud.hooks.ScoreboardHook;

import java.util.Collection;

public class Scoreboard extends HudConfig {

    @HUD(
            name = "Scoreboard"
    )
    public ScoreboardHUD hud = new ScoreboardHUD();

    public Scoreboard() {
        super("Scoreboard", "scoreboard.json");
        initialize();
    }

    public static class ScoreboardHUD extends BasicHud {

        @Dropdown(
                name = "Show Score Points",
                category = "Score Points",
                options = {"Hide", "Hide Only if Consecutive", "Show Always"}
        )
        public static int scoreboardPoints = 1;

        @Color(
                name = "Score Points Color"
        )
        public OneColor scorePointsColor = new OneColor(255, 85, 85);

        @Switch(
                name = "Scoreboard Title"
        )
        public static boolean scoreboardTitle = true;

        @Switch(
                name = "Persistent Scoreboard Title"
        )
        public boolean persistentTitle = false;

        @Color(
                name = "Title Background Color"
        )
        public OneColor titleColor = new OneColor(0, 0, 0, 96);

        @Dropdown(
                name = "Text Type",
                options = {"No Shadow", "Shadow", "Full Shadow"}
        )
        public int textType = 0;

        @Exclude
        private static final Minecraft mc = UMinecraft.getMinecraft();

        @Exclude public static final FontRenderer fontRenderer = UMinecraft.getFontRenderer();

        @Exclude public float width = 0f;
        @Exclude public float height = 0f;

        public ScoreboardHUD() {
            super(true, 1919, 1080f / 2f, 1, true, false, 0, 1, 0, new OneColor(0, 0, 0, 80), false, 2, new OneColor(0, 0, 0));
            EventManager.INSTANCE.register(this);
            showInDebug = true;
        }

        @Override
        public void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
            UGraphics.GL.pushMatrix();
            UGraphics.GL.scale(scale, scale, 1);
            UGraphics.GL.translate(x / scale, y / scale, 1);

            ScoreObjective objective = mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);

            if (!this.shouldShow()) {
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
            if (VanillaHUD.isApec() || !ScoreboardHook.canDraw || VanillaHUD.isSkyHanniScoreboard() || mc.theWorld == null) { // I love Apec Mod Minecraft
                return false;
            }
            ScoreObjective objective = mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
            boolean showRealScoreboard = objective != null;
            if (showRealScoreboard) {
                Collection<Score> sortedScores = objective.getScoreboard().getSortedScores(objective);
                showRealScoreboard = sortedScores.size() <= 15 && (sortedScores.size() > 0 || (this.persistentTitle && this.scoreboardTitle));
            }
            return showRealScoreboard && super.shouldShow();
        }

        private void renderObjective(ScoreObjective scoreObjective) {
            UGraphics.enableBlend();
            net.minecraft.scoreboard.Scoreboard scoreboard = scoreObjective.getScoreboard();
            Collection<Score> sortedScores = scoreboard.getSortedScores(scoreObjective);
            boolean showScorePoints = (this.scoreboardPoints == 2 || (this.scoreboardPoints == 1 && isNonConsecutive(scoreObjective)));
            String displayName = scoreObjective.getDisplayName();
            int displayNameStringWidth = fontRenderer.getStringWidth(displayName);

            for (Score score : sortedScores) {
                ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
                String totalString = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()) + (showScorePoints ? ": " + EnumChatFormatting.RED + score.getScorePoints() : "");
                displayNameStringWidth = Math.max(displayNameStringWidth, fontRenderer.getStringWidth(totalString));
            }

            if (this.scoreboardTitle) {
                TextRenderer.drawScaledString(displayName, this.width / 2.0f - fontRenderer.getStringWidth(displayName) / 2.0f, 1, -1, TextRenderer.TextType.toType(textType), 1);
            }

            UGraphics.GL.translate(0.0f, this.height, 0.0f);
            int counter = 0;
            for (Score score : sortedScores) {
                ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
                String playerName = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName());
                float yPos = -++counter * fontRenderer.FONT_HEIGHT;
                TextRenderer.drawScaledString(playerName, 1, yPos, -1, TextRenderer.TextType.toType(textType), 1);

                if (showScorePoints) {
                    String scorePoints = "" + score.getScorePoints();
                    TextRenderer.drawScaledString(scorePoints, this.width - fontRenderer.getStringWidth(scorePoints) - 1, yPos, this.scorePointsColor.getRGB(), TextRenderer.TextType.toType(textType), 1);
                }
            }

            this.width = displayNameStringWidth + 2;
            this.height = sortedScores.size() * fontRenderer.FONT_HEIGHT + (this.scoreboardTitle ? 10 : 1);
        }

        /*
            The following code is taken from Ugly Scoreboard Fix under the Apache-2.0 License
            https://github.com/Lortseam/ugly-scoreboard-fix/blob/master/LICENSE
         */
        private boolean isNonConsecutive(ScoreObjective scoreObjective) {
            int[] scorePoints = scoreObjective.getScoreboard().getSortedScores(scoreObjective).stream().mapToInt(Score::getScorePoints).toArray();
            if (scorePoints.length > 1) {
                for (int line = 1; line < scorePoints.length; line++) {
                    if (scorePoints[line] != scorePoints[line - 1] + 1) { // check if the score is just 1 higher than previous
                        return true;
                    }
                }
            }
            return false;
        }

        protected void drawBackground(float x, float y, float width, float height, float scale) {
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            nanoVGHelper.setupAndDraw(true, vg -> {
                if (this.rounded) {
                    if (this.scoreboardTitle) {
                        ScissorHelper helper = ScissorHelper.INSTANCE;

                        helper.scissor(vg, x, y, width, fontRenderer.FONT_HEIGHT + paddingY);
                        nanoVGHelper.drawRoundedRectVaried(vg, x, y, width, height, this.titleColor.getRGB(), this.cornerRadius * scale, this.cornerRadius * scale, 0, 0);
                        helper.clearScissors(vg);

                        helper.scissor(vg, x, y + fontRenderer.FONT_HEIGHT + paddingY, width, height - fontRenderer.FONT_HEIGHT - paddingY);
                        nanoVGHelper.drawRoundedRectVaried(vg, x, y, width, height, this.bgColor.getRGB(), 0, 0, this.cornerRadius * scale, this.cornerRadius * scale);
                        helper.clearScissors(vg);
                    } else {
                        nanoVGHelper.drawRoundedRect(vg, x, y, width, height, bgColor.getRGB(), this.cornerRadius * scale);
                    }
                    if (this.border) {
                        nanoVGHelper.drawHollowRoundRect(vg, x - borderSize * scale, y - borderSize * scale, width + borderSize * scale, height + borderSize * scale, borderColor.getRGB(), this.cornerRadius * scale, borderSize * scale);
                    }
                } else {
                    if (this.scoreboardTitle) {
                        nanoVGHelper.drawRect(vg, x, y, width, fontRenderer.FONT_HEIGHT + paddingY, this.titleColor.getRGB());
                        nanoVGHelper.drawRect(vg, x, y + fontRenderer.FONT_HEIGHT + paddingY, width, height - fontRenderer.FONT_HEIGHT - paddingY, bgColor.getRGB());
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

