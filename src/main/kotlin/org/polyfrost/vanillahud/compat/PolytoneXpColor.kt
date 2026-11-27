package org.polyfrost.vanillahud.compat

import com.google.gson.JsonParser
import net.fabricmc.loader.api.FabricLoader
//? if > 1.8.9 {
import net.minecraft.client.Minecraft
import org.polyfrost.compose.minecraft.platform.HostEvents
//?}
import java.io.Reader
import java.util.Properties

/** Whether a resource pack sets the experience level colour through Polytone, which beats the colour picked in this mod. */
object PolytoneXpColor {
    private val FOLDERS = listOf("polytone", "colormatic", "vanadium", "optifine")
    private val FILE_ENDINGS = listOf("color.properties", "colors.json")
    private const val PROPERTIES_KEY = "text.xpbar"

    private val polytoneLoaded = FabricLoader.getInstance().isModLoaded("polytone")

    var packSetsColor = false
        private set

    //? if > 1.8.9 {
    init {
        HostEvents.onResourcesReloaded(::refresh)
        refresh()
    }

    private fun refresh() {
        packSetsColor = polytoneLoaded && anyFileDefinesXpColor()
    }

    private fun anyFileDefinesXpColor(): Boolean {
        val resourceManager = Minecraft.getInstance().resourceManager ?: return false
        return FOLDERS.any { folder ->
            resourceManager.listResourceStacks(folder) { id -> FILE_ENDINGS.any(id.path::endsWith) }.any { (id, stack) ->
                stack.any { resource -> runCatching { resource.openAsReader().use { definesXpColor(id.path, it) } }.getOrDefault(false) }
            }
        }
    }
    //?}

    fun definesXpColor(fileName: String, content: Reader): Boolean =
        if (fileName.endsWith(".json")) jsonDefinesXpColor(content) else propertiesDefineXpColor(content)

    private fun propertiesDefineXpColor(content: Reader) =
        Properties().apply { load(content) }.containsKey(PROPERTIES_KEY)

    private fun jsonDefinesXpColor(content: Reader) =
        JsonParser.parseReader(content).asJsonObject.getAsJsonObject("text")?.has("xpbar") == true
}
