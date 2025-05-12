package org.polyfrost.vanillahud.hud

import dev.deftu.omnicore.client.render.OmniGameRendering
import dev.deftu.omnicore.client.render.OmniMatrixStack
import dev.deftu.textile.minecraft.MCTextFormat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.scoreboard.Score
import net.minecraft.scoreboard.ScorePlayerTeam
import org.polyfrost.oneconfig.api.config.v1.annotations.Color
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import org.polyfrost.polyui.color.rgba
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
        stack.scale(scaleX.toDouble(), scaleY.toDouble(), 1.0)
        stack.translate((x / scaleX).toDouble(), (y / scaleY).toDouble(), 1.0)

        val objective = mc.theWorld.scoreboard.getObjectiveInDisplaySlot(1)

        GlStateManager.enableBlend()

        val scoreboard = objective.scoreboard
        val scores = scoreboard.getSortedScores(objective)
        val showPoints = (scorePoints == 2 || (scorePoints == 1 && isNonConsecutive(scores)))
        val displayName = objective.displayName
        val width = mc.fontRendererObj.getStringWidth(displayName)

        var totalWidth = width

        for (score in scores) {
            val team = scoreboard.getPlayersTeam(score.playerName)
            val totalString = ScorePlayerTeam.formatPlayerName(team, score.playerName) + if (showPoints) ": " + MCTextFormat.RED + score.scorePoints else ""
            totalWidth = max(width, mc.fontRendererObj.getStringWidth(totalString))
        }

        if (scoreboardTitle) {
            OmniGameRendering.drawText(stack, displayName, width / 2.0f - totalWidth / 2.0f, 1f, -1)
        }

        stack.translate(0.0f, height, 0.0f)

        var counter = 0

        for (score in scores) {
            val team = scoreboard.getPlayersTeam(score.playerName)
            val player = ScorePlayerTeam.formatPlayerName(team, score.playerName)
            val yPos = -(++counter * mc.fontRendererObj.FONT_HEIGHT).toFloat()
            OmniGameRendering.drawText(stack, player, 1f, yPos, -1, false)

            if (showPoints) {
                OmniGameRendering.drawText(stack, "$scorePoints", width - mc.fontRendererObj.getStringWidth("$scorePoints") - 1f, yPos, scorePointsColor.rgba, false)
            }
        }

        this.width = totalWidth + 2f
        this.height = scores.size * mc.fontRendererObj.FONT_HEIGHT + (if (scoreboardTitle) 10f else 1f)

        stack.pop()
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