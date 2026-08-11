package org.polyfrost.vanillahud.compat

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import java.lang.reflect.Field
import java.lang.reflect.Method

object TabListCompat {
    class Bounds(val top: Float, val height: Float)

    private val providers: List<Provider> by lazy {
        buildList {
            // SkyHanni cancels the whole tab list layer so it wins over SkyCubed when both are on
            if (isLoaded("skyhanni")) add(SkyHanni)
            if (isLoaded("skycubed")) add(SkyCubed)
        }
    }

    private fun isLoaded(id: String) = try {
        FabricLoader.getInstance().isModLoaded(id)
    } catch (_: Throwable) {
        false
    }

    fun bounds(header: Component?, footer: Component?): Bounds? {
        for (provider in providers) {
            provider.bounds(header, footer)?.let { return it }
        }
        return null
    }

    private interface Provider {
        fun bounds(header: Component?, footer: Component?): Bounds?
    }

    private fun lookup(name: String): Class<*>? = try {
        Class.forName(name)
    } catch (_: Throwable) {
        null
    }

    private object SkyCubed : Provider {
        private const val DRAW_Y = 3f

        // Olympus reserves extra pixels for the border
        private const val BORDER_RESERVE = 4f

        private var resolved = false
        private var instance: Any? = null
        private var isEnabled: Method? = null
        private var display: Field? = null
        private var getHeight: Method? = null

        private fun resolve(): Boolean {
            if (resolved) return instance != null
            resolved = true
            try {
                val clazz = lookup("tech.thatgravyboat.skycubed.features.tablist.CompactTablist") ?: return false
                isEnabled = clazz.getDeclaredMethod("isEnabled").apply { isAccessible = true }
                display = clazz.getDeclaredField("display").apply { isAccessible = true }
                getHeight = lookup("me.owdding.lib.displays.Display")?.getMethod("getHeight") ?: return false
                instance = clazz.getField("INSTANCE").get(null)
            } catch (_: Throwable) {
                instance = null
            }
            return instance != null
        }

        override fun bounds(header: Component?, footer: Component?): Bounds? {
            if (!resolve()) return null
            return try {
                if (isEnabled?.invoke(instance) != true) return null
                val shown = display?.get(null) ?: return null
                val height = getHeight?.invoke(shown) as? Int ?: return null
                if (height <= 0) null else Bounds(DRAW_Y, height + BORDER_RESERVE)
            } catch (_: Throwable) {
                instance = null
                null
            }
        }
    }

    private object SkyHanni : Provider {
        private const val LINE_HEIGHT = 8 + 1
        private const val TAB_PADDING = 3
        private const val DRAW_Y = 10

        private var resolved = false
        private var reader: Any? = null
        private var getRenderColumns: Method? = null
        private var getAdvertString: Method? = null
        private var columnSize: Method? = null
        private var config: Method? = null
        private var getEnabled: Method? = null
        private var propertyGet: Method? = null
        private var getToggleTab: Method? = null
        private var getHideAdverts: Method? = null

        private fun resolve(): Boolean {
            if (resolved) return reader != null
            resolved = true
            try {
                val readerClass =
                    lookup("at.hannibal2.skyhanni.features.misc.compacttablist.TabListReader") ?: return false
                getRenderColumns = readerClass.getMethod("getRenderColumns")
                getAdvertString = readerClass.getMethod("getHypixelAdvertisingString")
                columnSize = lookup("at.hannibal2.skyhanni.features.misc.compacttablist.RenderColumn")
                    ?.getMethod("size") ?: return false

                val mod = lookup("at.hannibal2.skyhanni.SkyHanniMod") ?: return false
                val feature = mod.getField("feature")
                val gui = feature.type.getField("gui")
                config = gui.type.getMethod("getCompactTabList")
                val configClass = config?.returnType ?: return false
                getEnabled = configClass.getMethod("getEnabled")
                propertyGet = getEnabled?.returnType?.getMethod("get") ?: return false
                getToggleTab = configClass.getMethod("getToggleTab")
                getHideAdverts = configClass.getMethod("getHideAdverts")

                configOf = { feature.get(null)?.let { gui.get(it) }?.let { config?.invoke(it) } }
                reader = readerClass.getField("INSTANCE").get(null)
            } catch (_: Throwable) {
                reader = null
            }
            return reader != null
        }

        private var configOf: (() -> Any?)? = null

        override fun bounds(header: Component?, footer: Component?): Bounds? {
            if (!resolve()) return null
            return try {
                val config = configOf?.invoke() ?: return null
                if (propertyGet?.invoke(getEnabled?.invoke(config)) != true) return null
                if (getToggleTab?.invoke(config) == true) return null

                val columns = getRenderColumns?.invoke(reader) as? List<*> ?: return null
                var maxLines = 0
                for (column in columns) {
                    val size = columnSize?.invoke(column) as? Int ?: continue
                    if (size > maxLines) maxLines = size
                }
                if (maxLines <= 0) return null

                var total = maxLines * LINE_HEIGHT
                if (getHideAdverts?.invoke(config) != true) {
                    val marker = getAdvertString?.invoke(reader) as? String ?: return null
                    total += advertLines(header, marker) * LINE_HEIGHT + TAB_PADDING
                    total += advertLines(footer, marker) * LINE_HEIGHT + TAB_PADDING
                }
                Bounds((DRAW_Y - TAB_PADDING).toFloat(), (total + TAB_PADDING * 2).toFloat())
            } catch (_: Throwable) {
                reader = null
                null
            }
        }

        private fun advertLines(component: Component?, marker: String): Int {
            val text = component?.string ?: return 0
            return text.split("\n").count { it.contains(marker) }
        }
    }
}
