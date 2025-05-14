package org.polyfrost.vanillahud.hud

import dev.deftu.omnicore.client.OmniClient
import dev.deftu.omnicore.client.render.OmniMatrixStack
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.scoreboard.Score
import net.minecraft.scoreboard.ScoreObjective
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.util.EnumChatFormatting
import org.polyfrost.oneconfig.api.config.v1.annotations.Color
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.hud.v1.Hud
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import org.polyfrost.polyui.color.rgba
import org.polyfrost.polyui.component.Drawable
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.vanillahud.utils.drawScaledString
import kotlin.math.max

class ScoreboardHud() : Hud<Drawable>() {
    @Dropdown(
        title = "Show Score Points",
        category = "Score Points",
        options = ["Never", "If Consecutive", "Always"]
    )
    var scorePoints = 1

    @Color(
        title = "Score Points Color"
    )
    var scorePointsColor = rgba(255, 85, 85, 1f)

    @Switch(
        title = "Scoreboard Title"
    )
    var scoreboardTitle = true

    @Switch(
        title = "Persistent Scoreboard Title"
    )
    var persistentScoreboardTitle = false

    @Color(
        title = "Title Background Color"
    )
    var titleColor = rgba(0, 0, 0, (96/255).toFloat()) // this api sucks /lh

    @Dropdown(
        title = "Text Shadow",
        options = ["No Shadow", "Shadow", "Full Shadow"]
    )
    var textShadow = 0

    fun renderObjective(objective: ScoreObjective, height: Float, width: Float): Vec2 {
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend() // TODO: Probably use OmniCore, I just didn't feel like reading code for 3 hours

        val fontRenderer = OmniClient.fontRenderer

        val scoreboard = objective.scoreboard
        val sortedScores = scoreboard.getSortedScores(objective)
        val showScores = (this.scorePoints == 2 || (this.scorePoints == 1 && isNonConsecutive(sortedScores)))

        val displayName = objective.displayName
        var displayNameWidth = OmniClient.fontRenderer.getStringWidth(displayName)

        for (score in sortedScores) {
            val team = scoreboard.getPlayersTeam(score.playerName)
            val totalString = ScorePlayerTeam.formatPlayerName(team, score.playerName) + (if (showScores) ": " + EnumChatFormatting.RED + score.scorePoints else "")
            displayNameWidth = max(displayNameWidth, OmniClient.fontRenderer.getStringWidth(totalString))
        }

        if (scoreboardTitle) {
            drawScaledString(displayName, width / 2f - displayNameWidth / 2f, 1f, -1, textShadow, 1f)
        }

        GlStateManager.translate(0f, height, 0f)
        var counter = 0f
        for (score in sortedScores) {
            val team = scoreboard.getPlayersTeam(score.playerName)
            val playerName = ScorePlayerTeam.formatPlayerName(team, score.playerName)
            val yPos = -++counter * OmniClient.fontRenderer.FONT_HEIGHT

            drawScaledString(playerName, 1f, yPos, -1, textShadow, 1f)

            if (showScores) {
                val scorePoints = "${score.scorePoints}"
                drawScaledString(scorePoints, width / fontRenderer.getStringWidth(scorePoints) - 1, yPos, scorePointsColor.rgba, textShadow, 1f)
            }
        }

        GlStateManager.popMatrix()

        val h = sortedScores.size * fontRenderer.FONT_HEIGHT + (if (scoreboardTitle) 10f else 1f)
        val w = displayNameWidth + 2f
        return Vec2(h, w)
    }

    fun isNonConsecutive(scores: Collection<Score>): Boolean {
        val points = scores.map { it.scorePoints }

        if (points.size <= 1) return false

        return points.zipWithNext().any { (a, b) -> b != a + 1 }
    }

    override fun title(): String {
        return "Scoreboard"
    }

    override fun category(): Category {
        return Category.INFO
    }

    override fun create(): Drawable {
        return object : Drawable() {
            override fun render() {
                val objective = mc.theWorld.scoreboard.getObjectiveInDisplaySlot(1)
                val size = renderObjective(objective, height, width)
                height = size.x
                width = size.y
            }

        }
    }

    override fun update(): Boolean {
        // TODO: Properly implement
        return true
    }
}