package org.polyfrost.vanillahud.compat

import net.fabricmc.loader.api.FabricLoader
import org.polyfrost.vanillahud.hud.Huds
import kotlin.math.roundToInt

object CustomScoreboardBridge {
    const val MOD_ID = "customscoreboard"

    const val CONFIG_ID = "customscoreboard/config"

    @JvmStatic
    val present: Boolean by lazy { FabricLoader.getInstance().isModLoaded(MOD_ID) }

    @Volatile
    private var lastUpdateMs = 0L

    @Volatile
    var defaultX = 0
        private set

    @Volatile
    var defaultY = 0
        private set

    @Volatile
    var contentWidth = 0
        private set

    @Volatile
    var contentHeight = 0
        private set

    private var wasActive = false
    private var savedScoreboardHidden: Boolean? = null

    @JvmStatic
    fun isActive(): Boolean = present && (System.currentTimeMillis() - lastUpdateMs) < STALE_MS

    @JvmStatic
    fun onCsPosition(csX: Int, csY: Int, width: Int, height: Int): IntArray? {
        defaultX = csX
        defaultY = csY
        contentWidth = width
        contentHeight = height
        lastUpdateMs = System.currentTimeMillis()

        val hud = Huds.customScoreboard
        val moved = hud.trackExternalDefault(csX.toFloat(), csY.toFloat())
        return if (moved) intArrayOf(hud.x.roundToInt(), hud.y.roundToInt()) else null
    }

    fun syncVisibility() {
        if (!present) return
        val active = isActive()
        Huds.customScoreboard.hidden = !active
        if (active == wasActive) return
        if (active) {
            savedScoreboardHidden = Huds.scoreboard.hidden
            Huds.scoreboard.hidden = true
        } else {
            Huds.scoreboard.hidden = savedScoreboardHidden ?: false
            savedScoreboardHidden = null
        }
        wasActive = active
    }

    private const val STALE_MS = 500L
}
