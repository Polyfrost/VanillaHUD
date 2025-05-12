package org.polyfrost.vanillahud.hooks

import net.minecraft.entity.boss.BossStatus
import org.polyfrost.vanillahud.hud.BossBar

object BossStatusHook {
    private var lerpedBossHealth = 0f
    private var percentSetTime: Long = 0

    @JvmStatic
    fun onStatusSet() {
        lerpedBossHealth = percent
        percentSetTime = System.currentTimeMillis()
    }

    @JvmStatic
    val percent: Float
        get() {
            val l = System.currentTimeMillis() - percentSetTime

            val f = (l / BossBar.hud.lerpSpeed).coerceIn(0f, 1f)
            return lerp(f, lerpedBossHealth, BossStatus.healthScale)
        }

    private fun lerp(pct: Float, start: Float, end: Float): Float {
        return start + pct * (end - start)
    }
}