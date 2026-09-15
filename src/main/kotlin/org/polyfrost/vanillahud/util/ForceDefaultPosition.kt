package org.polyfrost.vanillahud.util

import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.vanillahud.hud.VanillaHud
import java.nio.file.Files
import java.nio.file.Path

object ForceDefaultPosition {
    private const val FILE = "vanillahud-customized"

    private const val RETRY_AFTER_NANOS = 1_000_000_000L

    @Volatile
    internal var nanos: () -> Long = System::nanoTime

    private val file: Path get() = ConfigManager.active().folder.resolve(FILE)

    private data class Customized(val file: Path, val ids: MutableSet<String>)

    private var cache: Customized? = null

    private var failedAt: Long? = null

    /** ids queued this launch so each HUD is only forced once per profile */
    private val forced = HashSet<String>()

    private val unwritten = LinkedHashSet<String>()

    private var unwrittenFile: Path? = null

    private var retriedAt: Long? = null

    private var flushedAt: Long? = null

    private val customized: Customized?
        get() {
            cache?.let { return it }
            val failed = failedAt
            if (failed != null && nanos() - failed < RETRY_AFTER_NANOS) return null
            val read = read()
            if (read == null) {
                failedAt = nanos()
            } else {
                read.ids.addAll(unwritten)
                cache = read
            }
            return read
        }

    @Synchronized
    fun invalidate() {
        cache = null
        failedAt = null
        flushedAt = null
        forced.clear()
    }

    @Synchronized
    fun customized(hud: VanillaHud): Boolean? = customized?.ids?.contains(hud.hudId)

    private fun markCustomized(hud: VanillaHud) {
        hud.cancelForceDefault()
        val state = customized ?: run {
            unwritten.add(hud.hudId)
            return
        }
        val known = state.ids
        if (!known.add(hud.hudId)) {
            if (unwritten.isEmpty()) return
            val last = retriedAt
            if (last != null && nanos() - last < RETRY_AFTER_NANOS) return
            retriedAt = nanos()
        }
        if (write(state)) {
            unwritten.clear()
            retriedAt = null
        } else {
            unwritten.add(hud.hudId)
        }
    }

    private fun flushBacklog() {
        if (unwritten.isEmpty()) return
        val last = flushedAt
        if (last != null && nanos() - last < RETRY_AFTER_NANOS) return
        val state = customized ?: return
        if (unwritten.isEmpty()) return
        flushedAt = nanos()
        if (write(state)) {
            unwritten.clear()
            flushedAt = null
        }
    }

    @Synchronized
    fun tick() {
        flushBacklog()
        for (hud in HudManager.activeInstances.filterIsInstance<VanillaHud>()) {
            if (!hud.locked || hud.isAnchored) {
                markCustomized(hud)
                continue
            }
            if (customized(hud) != false) continue
            if (forced.add(hud.hudId)) hud.queueForceDefault()
        }
    }

    private fun read(): Customized? = try {
        val p = file
        if (unwrittenFile != p) {
            unwrittenFile = p
            unwritten.clear()
        }
        val ids = if (Files.isRegularFile(p)) {
            Files.readAllLines(p).mapNotNullTo(LinkedHashSet()) { it.trim().ifEmpty { null } }
        } else {
            LinkedHashSet()
        }
        Customized(p, ids)
    } catch (_: Throwable) {
        null
    }

    private fun write(state: Customized): Boolean {
        return try {
            if (file != state.file) {
                cache = null
                failedAt = null
                false
            } else {
                Files.createDirectories(state.file.parent)
                Files.write(state.file, state.ids.sorted())
                true
            }
        } catch (_: Throwable) {
            false
        }
    }
}
