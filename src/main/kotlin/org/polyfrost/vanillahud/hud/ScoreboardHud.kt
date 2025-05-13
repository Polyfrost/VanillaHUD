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
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import org.polyfrost.polyui.color.rgba
import org.polyfrost.vanillahud.utils.drawScaledString
import kotlin.math.max

class ScoreboardHud(override var width: Float, override var height: Float) : LegacyHud() {
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

    override fun render(
        stack: OmniMatrixStack,
        x: Float,
        y: Float,
        scaleX: Float,
        scaleY: Float
    ) {
        stack.push()
        stack.scale(scaleX, scaleY, 1f)
        stack.translate(x / scaleX, y / scaleY, 1f)

        val objective = mc.theWorld.scoreboard.getObjectiveInDisplaySlot(1)

        renderObjective(stack, objective)

        stack.pop()
    }

    fun renderObjective(stack: OmniMatrixStack, objective: ScoreObjective) {
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
            stack.drawScaledString(displayName, width / 2f - displayNameWidth / 2f, 1f, -1, textShadow, 1f)
        }

        GlStateManager.translate(0f, height, 0f)
        var counter = 0f
        for (score in sortedScores) {
            val team = scoreboard.getPlayersTeam(score.playerName)
            val playerName = ScorePlayerTeam.formatPlayerName(team, score.playerName)
            val yPos = -++counter * OmniClient.fontRenderer.FONT_HEIGHT

            stack.drawScaledString(playerName, 1f, yPos, -1, textShadow, 1f)

            if (showScores) {
                val scorePoints = "${score.scorePoints}"
                stack.drawScaledString(scorePoints, width / fontRenderer.getStringWidth(scorePoints) - 1, yPos, scorePointsColor.rgba, textShadow, 1f)
            }
        }

        width = displayNameWidth + 2f
        this.height = sortedScores.size * fontRenderer.FONT_HEIGHT + (if (scoreboardTitle) 10f else 1f)
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

    override fun update(): Boolean {
        return true
        // TODO: Actually implement this
    }
}