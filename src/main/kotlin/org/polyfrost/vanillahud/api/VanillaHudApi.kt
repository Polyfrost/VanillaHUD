package org.polyfrost.vanillahud.api

import org.polyfrost.vanillahud.hud.Huds
import java.util.concurrent.ConcurrentHashMap

object VanillaHudApi {
    private val vanilla: MutableSet<String> = ConcurrentHashMap.newKeySet()

    @JvmStatic
    fun elements(): List<String> = Huds.all.map { normalise(it.hudId) }

    @JvmStatic
    fun setDrawnAsVanilla(hudId: String, drawnAsVanilla: Boolean) {
        val id = normalise(hudId)
        val keys = listOf(id, "$id.json")
        if (drawnAsVanilla) vanilla.addAll(keys) else vanilla.removeAll(keys)
    }

    @JvmStatic
    fun isDrawnAsVanilla(hudId: String): Boolean = vanilla.isNotEmpty() && hudId in vanilla

    @JvmStatic
    fun clear() = vanilla.clear()

    private fun normalise(id: String) = id.trim().removeSuffix(".json")
}
