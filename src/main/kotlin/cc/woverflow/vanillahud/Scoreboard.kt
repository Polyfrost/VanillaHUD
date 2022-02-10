package cc.woverflow.vanillahud

import cc.woverflow.vanillahud.config.VanillaHUDConfig
import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import gg.essential.universal.*
import net.minecraft.client.gui.Gui
import net.minecraft.scoreboard.ScoreObjective
import net.minecraft.scoreboard.ScorePlayerTeam

object Scoreboard {

    fun renderScoreboard(objective: ScoreObjective) {
        if (VanillaHUDConfig.scoreboard) {
            UGraphics.GL.pushMatrix()

            val scoreboard = objective.scoreboard
            var collection = scoreboard.getSortedScores(objective)
            val list = Lists.newArrayList(Iterables.filter(
                collection
            ) { p_apply_1_ ->
                if (p_apply_1_ != null) {
                    p_apply_1_.playerName != null && !p_apply_1_.playerName.startsWith("#")
                } else {
                    false
                }
            })
            collection = if (list.size > 15) {
                Lists.newArrayList(Iterables.skip(list, collection.size - 15))
            } else {
                list
            }
            var i = UMinecraft.getFontRenderer().getStringWidth(objective.displayName)
            for (score in collection) {
                i = i.coerceAtLeast(
                    UMinecraft.getFontRenderer().getStringWidth(
                        "${
                            ScorePlayerTeam.formatPlayerName(
                                scoreboard.getPlayersTeam(score.playerName), score.playerName
                            )
                        }${if (VanillaHUDConfig.scoreboardScorePoints) ": ${ChatColor.RED}${score.scorePoints}" else ""}"
                    )
                )
            }
            val textHeight = collection.size * UMinecraft.getFontRenderer().FONT_HEIGHT
            val bottom = (UResolution.scaledHeight / 2 + textHeight / 3) + VanillaHUDConfig.scoreboardY
            val left = (UResolution.scaledWidth - i - 3) + VanillaHUDConfig.scoreboardX
            val scorePointRight = (UResolution.scaledWidth - 1) + VanillaHUDConfig.scoreboardX

            UGraphics.GL.translate(
                (-scorePointRight).toDouble() * (VanillaHUDConfig.scoreboardScale - 1f),
                (-(bottom - collection.size * UMinecraft.getFontRenderer().FONT_HEIGHT)).toDouble() * (VanillaHUDConfig.scoreboardScale - 1f), 0.0)
            UGraphics.GL.scale(VanillaHUDConfig.scoreboardScale, VanillaHUDConfig.scoreboardScale, 1.0F)
            collection.forEachIndexed { index, score ->
                val scoreplayerteam = scoreboard.getPlayersTeam(score.playerName)
                val text = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.playerName)
                val scorePoint = "${ChatColor.RED}${score.scorePoints}"
                val currentTextTop = bottom - (index + 1) * UMinecraft.getFontRenderer().FONT_HEIGHT
                if (VanillaHUDConfig.scoreboardBackground) Gui.drawRect(
                    left - 2,
                    currentTextTop,
                    scorePointRight,
                    currentTextTop + UMinecraft.getFontRenderer().FONT_HEIGHT,
                    VanillaHUDConfig.scoreboardBackgroundColor.rgb
                )
                UMinecraft.getFontRenderer().drawString(text, left.toFloat(), currentTextTop.toFloat(), -1, VanillaHUDConfig.scoreboardTextShadow)
                if (VanillaHUDConfig.scoreboardScorePoints) UMinecraft.getFontRenderer().drawString(
                    scorePoint,
                    (scorePointRight - UMinecraft.getFontRenderer().getStringWidth(scorePoint)).toFloat(),
                    currentTextTop.toFloat(),
                    -1, VanillaHUDConfig.scoreboardTextShadow
                )
                if ((index + 1) == collection.size) {
                    val topText = objective.displayName
                    if (VanillaHUDConfig.scoreboardBackground) Gui.drawRect(
                        left - 2,
                        currentTextTop - UMinecraft.getFontRenderer().FONT_HEIGHT - 1,
                        scorePointRight,
                        currentTextTop - 1,
                        VanillaHUDConfig.scoreboardTitleBackgroundColor.rgb
                    )
                    if (VanillaHUDConfig.scoreboardBackground) Gui.drawRect(
                        left - 2,
                        currentTextTop - 1,
                        scorePointRight,
                        currentTextTop,
                        VanillaHUDConfig.scoreboardBackgroundColor.rgb
                    )
                    UMinecraft.getFontRenderer().drawString(
                        topText,
                        (left + i / 2 - UMinecraft.getFontRenderer().getStringWidth(topText) / 2).toFloat(),
                        (currentTextTop - UMinecraft.getFontRenderer().FONT_HEIGHT).toFloat(),
                        -1, VanillaHUDConfig.scoreboardTextShadow
                    )
                }
            }

            UGraphics.GL.popMatrix()
        }
    }
}