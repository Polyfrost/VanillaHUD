package org.polyfrost.vanillahud.util

import net.minecraft.ChatFormatting
import net.minecraft.client.gui.components.LerpingBossEvent
import net.minecraft.network.chat.Component
import net.minecraft.world.BossEvent
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.ScoreHolder
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.criteria.ObjectiveCriteria
import java.util.UUID

object DemoData {
    private var scoreboardObjective: Objective? = null

    @JvmStatic
    fun demoScoreboardObjective(): Objective? {
        scoreboardObjective?.let { return it }
        return try {
            val scoreboard = Scoreboard()
            val objective = scoreboard.addObjective(
                "vanillahud_demo",
                ObjectiveCriteria.DUMMY,
                Component.literal("VanillaHUD").withStyle(ChatFormatting.YELLOW),
                ObjectiveCriteria.RenderType.INTEGER,
                false,
                null
            )
            putScore(scoreboard, objective, Component.literal("Kills").withStyle(ChatFormatting.GREEN), 7)
            putScore(scoreboard, objective, Component.literal("Deaths").withStyle(ChatFormatting.RED), 2)
            putScore(scoreboard, objective, Component.literal("K/D").withStyle(ChatFormatting.AQUA), 3)
            putScore(scoreboard, objective, Component.literal("Coins").withStyle(ChatFormatting.GOLD), 1337)
            putScore(scoreboard, objective, Component.literal("Rank").withStyle(ChatFormatting.LIGHT_PURPLE), 1)
            scoreboardObjective = objective
            objective
        } catch (_: Throwable) {
            null
        }
    }

    private fun putScore(scoreboard: Scoreboard, objective: Objective, name: Component, value: Int) {
        val holder = ScoreHolder.forNameOnly(name.string)
        val score = scoreboard.getOrCreatePlayerScore(holder, objective)
        score.set(value)
        score.display(name)
    }

    @JvmStatic
    fun demoBossEvents(): List<LerpingBossEvent> = listOf(
        LerpingBossEvent(
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            Component.literal("Boss Bar"),
            0.67f,
            BossEvent.BossBarColor.PINK,
            BossEvent.BossBarOverlay.PROGRESS,
            false,
            false,
            false
        )
    )
}
