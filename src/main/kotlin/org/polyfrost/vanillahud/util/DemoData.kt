package org.polyfrost.vanillahud.util

import net.minecraft.ChatFormatting
import net.minecraft.client.gui.components.LerpingBossEvent
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.sounds.WeighedSoundEvents
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.network.chat.Component
import net.minecraft.world.BossEvent
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.ScoreHolder
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.criteria.ObjectiveCriteria
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import java.util.UUID

object DemoData {
    private var scoreboardObjective: Objective? = null

    private val subtitleLines = listOf(
        "Skeleton rattles" to 1,
        "Zombie groans" to -1,
        "Footsteps" to 0,
        "Squid swims" to 1,
        "Splashing" to -1,
    )

    private const val SUBTITLE_ROW = 10

    @JvmStatic
    fun demoSubtitleWidth(): Float = try {
        val font = mc.font
        val arrows = font.width("<") + font.width(" ") + font.width(">") + font.width(" ")
        (subtitleLines.maxOf { font.width(it.first) } + arrows + 2).toFloat()
    } catch (_: Throwable) {
        90f
    }

    @JvmStatic
    fun demoSubtitleHeight(): Float = (subtitleLines.size * SUBTITLE_ROW).toFloat()

    class DemoSubtitle(val sound: SoundInstance, val event: WeighedSoundEvents, val range: Float)

    @JvmStatic
    fun demoSubtitles(pos: Vec3, forward: Vec3, right: Vec3): List<DemoSubtitle> =
        subtitleLines.map { (name, dir) ->
            val location = when (dir) {
                1 -> pos.add(forward.scale(8.0))
                -1 -> pos.subtract(forward.scale(8.0))
                else -> pos.add(right.scale(8.0))
            }
            val sound = SimpleSoundInstance(
                SoundEvents.UI_BUTTON_CLICK.value(),
                SoundSource.MASTER,
                1f,
                1f,
                SoundInstance.createUnseededRandom(),
                location.x,
                location.y,
                location.z,
            )
            val event = WeighedSoundEvents(Identifier.withDefaultNamespace("vanillahud_demo"), name)
            DemoSubtitle(sound, event, Float.POSITIVE_INFINITY)
        }

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

    private var effects: List<MobEffectInstance>? = null

    @JvmStatic
    fun demoEffects(): List<MobEffectInstance> {
        effects?.let { return it }
        return try {
            val holders = listOf(
                MobEffects.REGENERATION,
                MobEffects.FIRE_RESISTANCE,
                MobEffects.NIGHT_VISION,
                MobEffects.WATER_BREATHING,
                MobEffects.POISON,
            )
            val list = holders.map { MobEffectInstance(it, 999999, 0, false, true, true) }
            effects = list
            list
        } catch (_: Throwable) {
            emptyList()
        }
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
