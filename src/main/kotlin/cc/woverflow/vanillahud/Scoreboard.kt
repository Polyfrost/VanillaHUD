//package cc.woverflow.vanillahud
//
//import cc.polyfrost.oneconfig.libs.universal.ChatColor
//import cc.polyfrost.oneconfig.libs.universal.UGraphics
//import cc.polyfrost.oneconfig.libs.universal.UMinecraft
//import cc.polyfrost.oneconfig.libs.universal.UResolution
//import cc.woverflow.vanillahud.config.VanillaHUDConfig
//import com.google.common.collect.Iterables
//import net.minecraft.client.gui.Gui
//import net.minecraft.scoreboard.ScoreObjective
//import net.minecraft.scoreboard.ScorePlayerTeam
//import org.lwjgl.input.Keyboard
//
//
//object Scoreboard {
/*
@cc.polyfrost.oneconfig.config.annotations.Color(
        name = "Scoreboard Title Background Color",
        category = "Scoreboard",
    )
    var scoreboardTitleBackgroundColor: OneColor = OneColor(1610612736)
    @Checkbox(
        name = "Enable Text Shadow",
        category = "Scoreboard"
    )
    var scoreboardTextShadow = true

    @Checkbox(
        name = "Enable Score Points (red numbers)",
        category = "Scoreboard",
    )
    var scoreboardScorePoints = false
 */
//
//    fun renderScoreboard(objective: ScoreObjective) {
//        if (VanillaHUDConfig.scoreboard) {
//            UGraphics.GL.pushMatrix()
//
//            val scoreboard = objective.scoreboard
//            val sortedScores = scoreboard.getSortedScores(objective)
//            val list = sortedScores.filter {
//                (it != null) && (it.playerName != null) && !it.playerName.startsWith("#")
//            }.toMutableList()
//            val collection = if (list.size > 15) {
//                Iterables.skip(list, sortedScores.size - 15).toMutableList()
//            } else {
//                list
//            }
//            var i = UMinecraft.getFontRenderer().getStringWidth(objective.displayName)
//            for (score in collection) {
//                i = i.coerceAtLeast(
//                    UMinecraft.getFontRenderer().getStringWidth(
//                        "${
//                            ScorePlayerTeam.formatPlayerName(
//                                scoreboard.getPlayersTeam(score.playerName), score.playerName
//                            )
//                        }${if (VanillaHUDConfig.scoreboardScorePoints) ": ${ChatColor.RED}${score.scorePoints}" else ""}"
//                    )
//                )
//            }
//            val textHeight = collection.size * UMinecraft.getFontRenderer().FONT_HEIGHT
//            val bottom = (UResolution.scaledHeight / 2 + textHeight / 3) + VanillaHUDConfig.scoreboardY
//            val left = (UResolution.scaledWidth - i - 3) + VanillaHUDConfig.scoreboardX
//            val scorePointRight = (UResolution.scaledWidth - 1) + VanillaHUDConfig.scoreboardX
//            UGraphics.GL.translate(
//                (-scorePointRight).toDouble() * (VanillaHUDConfig.scoreboardScale - 1f),
//                (-(bottom - collection.size * UMinecraft.getFontRenderer().FONT_HEIGHT)).toDouble() * (VanillaHUDConfig.scoreboardScale - 1f),
//                0.0
//            )
//            UGraphics.GL.scale(VanillaHUDConfig.scoreboardScale, VanillaHUDConfig.scoreboardScale, 1.0F)
//            if (VanillaHUDConfig.scoreboardBackgroundBorder) {
//                drawHollowRect(
//                    left - 2 - VanillaHUDConfig.scoreboardBorderWidth,
//                    bottom - (collection.size + 1) * UMinecraft.getFontRenderer().FONT_HEIGHT - VanillaHUDConfig.scoreboardBorderWidth,
//                    scorePointRight,
//                    bottom,
//                    VanillaHUDConfig.scoreboardBorderWidth,
//                    VanillaHUDConfig.scoreboardBorderColor.rgb
//                )
//            }
//            collection.forEachIndexed { index, score ->
//                val scoreplayerteam = scoreboard.getPlayersTeam(score.playerName)
//                val text = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.playerName)
//                val scorePoint = "${ChatColor.RED}${score.scorePoints}"
//                val currentTextTop = bottom - (index + 1) * UMinecraft.getFontRenderer().FONT_HEIGHT
//                if (VanillaHUDConfig.scoreboardBackground) Gui.drawRect(
//                    left - 2,
//                    currentTextTop,
//                    scorePointRight,
//                    currentTextTop + UMinecraft.getFontRenderer().FONT_HEIGHT,
//                    VanillaHUDConfig.scoreboardBackgroundColor.rgb
//                )
//                UMinecraft.getFontRenderer().drawString(
//                    text, left.toFloat(), currentTextTop.toFloat(), -1, VanillaHUDConfig.scoreboardTextShadow
//                )
//                if (VanillaHUDConfig.scoreboardScorePoints) UMinecraft.getFontRenderer().drawString(
//                    scorePoint,
//                    (scorePointRight - UMinecraft.getFontRenderer().getStringWidth(scorePoint)).toFloat(),
//                    currentTextTop.toFloat(),
//                    -1,
//                    VanillaHUDConfig.scoreboardTextShadow
//                )
//                if ((index + 1) == collection.size) {
//                    val topText = objective.displayName
//                    if (VanillaHUDConfig.scoreboardBackground) Gui.drawRect(
//                        left - 2,
//                        currentTextTop - UMinecraft.getFontRenderer().FONT_HEIGHT - 1,
//                        scorePointRight,
//                        currentTextTop - 1,
//                        VanillaHUDConfig.scoreboardTitleBackgroundColor.rgb
//                    )
//                    if (VanillaHUDConfig.scoreboardBackground) Gui.drawRect(
//                        left - 2,
//                        currentTextTop - 1,
//                        scorePointRight,
//                        currentTextTop,
//                        VanillaHUDConfig.scoreboardBackgroundColor.rgb
//                    )
//                    UMinecraft.getFontRenderer().drawString(
//                        topText,
//                        (left + i / 2 - UMinecraft.getFontRenderer().getStringWidth(topText) / 2).toFloat(),
//                        (currentTextTop - UMinecraft.getFontRenderer().FONT_HEIGHT).toFloat(),
//                        -1,
//                        VanillaHUDConfig.scoreboardTextShadow
//                    )
//                }
//            }
//
//            UGraphics.GL.popMatrix()
//        }
//    }
//
//}