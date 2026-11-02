package org.polyfrost.vanillahud.util

/**
 * HUD data read once when the editor or settings opens and held until it closes, so previews don't
 * change while you edit. A null value means nothing was worth showing, and the caller uses its demo data.
 */
object EditorSnapshot {
    private val held = HashMap<String, Any?>()

    fun clear() = held.clear()

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T> hold(key: String, live: () -> T?): T? {
        if (key !in held) held[key] = try {
            live()
        } catch (_: Throwable) {
            null
        }
        return held[key] as T?
    }

    @JvmStatic
    fun <T> hold(key: String, live: T, valid: Boolean, demo: T): T = hold(key) { live.takeIf { valid } } ?: demo
}
