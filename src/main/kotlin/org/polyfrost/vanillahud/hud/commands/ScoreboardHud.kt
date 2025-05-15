package org.polyfrost.vanillahud.hud.commands

import dev.deftu.omnicore.client.OmniClient
import dev.deftu.omnicore.client.OmniClient.fontRenderer
import dev.deftu.omnicore.client.render.OmniMatrixStack
import dev.deftu.textile.minecraft.MCTextFormat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.scoreboard.*
import net.minecraft.util.EnumChatFormatting
import org.polyfrost.oneconfig.api.config.v1.annotations.Color
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud
import org.polyfrost.polyui.color.rgba
import org.polyfrost.vanillahud.utils.drawScaledString

class ScoreboardHud() : LegacyHud() {
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

    override fun title() = "Scoreboard"
    override fun category() = Category.INFO

    override var width: Float = 0f

    override var height: Float = 0f

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

        var objective = OmniClient.world?.scoreboard?.getObjectiveInDisplaySlot(1)

        if (objective == null) return

        if (!isReal) {
            val scoreboard = Scoreboard()
            objective = ScoreObjective(scoreboard, "OneConfig", IScoreObjectiveCriteria.DUMMY)
            objective.displayName = "${MCTextFormat.AQUA + MCTextFormat.BOLD}Scoreboard"
            scoreboard.getValueFromObjective("Drag me around!", objective)
            scoreboard.getValueFromObjective("Click to drag", objective)
        }

        renderObjective(objective)
        stack.pop()
    }

    private fun renderObjective(scoreObjective: ScoreObjective) {
        GlStateManager.enableBlend()

        val scoreboard = scoreObjective.scoreboard
        val sortedScores = scoreboard.getSortedScores(scoreObjective)
        val showScorePoints = (this.scorePoints == 2 || (this.scorePoints == 1 && sortedScores.isNonConsecutive()))
        val displayName = scoreObjective.displayName
        var displayNameStringWidth = fontRenderer.getStringWidth(displayName)

        for (score in sortedScores) {
            val team = scoreboard.getPlayersTeam(score.playerName)
            val totalString = ScorePlayerTeam.formatPlayerName(team, score.playerName) + (if (showScorePoints) ": ${EnumChatFormatting.RED}${score.scorePoints}" else "")
            displayNameStringWidth = displayNameStringWidth.coerceAtLeast(fontRenderer.getStringWidth(totalString))
        }

        if (this.scoreboardTitle)
            drawScaledString(displayName, this.width / 2.0f - fontRenderer.getStringWidth(displayName) / 2.0f, 1f, -1, textShadow, 1f)


        GlStateManager.translate(0.0f, this.height, 0.0f)

        var counter = 0f
        for (score in sortedScores) {
            val team = scoreboard.getPlayersTeam(score.playerName)
            val playerName = ScorePlayerTeam.formatPlayerName(team, score.playerName)
            val yPos = -++counter * fontRenderer.FONT_HEIGHT
            drawScaledString(playerName, 1f, yPos, -1, textShadow, 1f)

            if (showScorePoints) {
                val scorePoints = "${score.scorePoints}"

                drawScaledString(scorePoints, this.width - fontRenderer.getStringWidth(scorePoints) - 1, yPos, this.scorePointsColor.rgba, textShadow, 1f)
            }
        }

        this.width = displayNameStringWidth + 2f
        this.height = sortedScores.size * fontRenderer.FONT_HEIGHT + (if (this.scoreboardTitle) 10f else 1f)
    }

    override fun update() = true

    private fun Collection<Score>.isNonConsecutive() = this.map { it.scorePoints }.zipWithNext().any { (a, b) -> b != a + 1 }
}