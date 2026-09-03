package org.polyfrost.vanillahud.hud

import net.minecraft.client.gui.components.LerpingBossEvent
import net.minecraft.client.multiplayer.PlayerInfo
import net.minecraft.network.chat.Component
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.PlayerScoreEntry
import net.minecraft.world.scores.PlayerTeam
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.annotations.*
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import org.polyfrost.vanillahud.compat.TabListCompat
import org.polyfrost.vanillahud.mixin.access.IBossHealthOverlay
import org.polyfrost.vanillahud.mixin.access.IPlayerTabOverlay
import org.polyfrost.vanillahud.mixin.access.ISubtitle
import org.polyfrost.vanillahud.mixin.access.ISubtitleOverlay
import org.polyfrost.vanillahud.util.DemoData
import org.polyfrost.vanillahud.util.TabListManager

class ActionBarHud : VanillaHud("vanillahud-actionbar.json", "Action Bar", Category.INFO) {
    @Switch(
        title = "Use Jukebox Rainbow Timer Color",
        description = "Use the rainbow timer color when a jukebox begins playing."
    )
    var rainbowTimer = true

    override val exampleText get() = "Action Bar"
    override val naturalWidth get() = 60f
    override val naturalHeight get() = 11f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - width / 2f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 72f
    override val anchorX get() = 0.5f
    override val anchorY get() = 1f

    override fun measuredWidth(): Float {
        if (previewing) return super.measuredWidth()
        return textWidth { hudAccessor?.overlay?.string }
    }
}

class BossBarHud : VanillaHud("vanillahud-bossbar.json", "Boss Bar", Category.COMBAT) {
    @Switch(title = "Render Text")
    var renderText = true

    @Switch(title = "Render Health")
    var renderHealth = true

    override val naturalWidth get() = 182f
    override val naturalHeight get() = 30f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - width / 2f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = if (renderText) 3f else 12f
    override val anchorX get() = 0.5f
    override val anchorY get() = 0f

    private fun bossEvents(): Collection<LerpingBossEvent> {
        val live = try {
            //? if >=26.2 {
            (mc.gui.hud.bossOverlay as IBossHealthOverlay).events.values
            //?} else {
            /*(mc.gui.bossOverlay as IBossHealthOverlay).events.values
            *///?}
        } catch (_: Throwable) {
            emptyList()
        }
        if (previewing) return DemoData.demoBossEvents()
        return live
    }

    private class Size(val width: Float, val height: Float)

    private fun size(): Size? = measureOnce { measureSize() }

    private fun measureSize(): Size {
        val events = bossEvents()
        val width = if (events.isEmpty() || !renderText) naturalWidth
        else events.fold(naturalWidth) { acc, e -> maxOf(acc, mc.font.width(e.name).toFloat()) }
        val n = events.size
        val height = if (n == 0) naturalHeight else ((n - 1) * 19 + if (renderText) 14 else 5).toFloat()
        return Size(width, height)
    }

    override fun measuredWidth(): Float = try {
        size()?.width ?: naturalWidth
    } catch (_: Throwable) {
        naturalWidth
    }

    override fun measuredHeight(): Float = try {
        size()?.height ?: naturalHeight
    } catch (_: Throwable) {
        naturalHeight
    }
}

/** the whole bottom cluster as one element so it keeps its vanilla internal layout and moves as a unit */
class HotbarHud : VanillaHud("vanillahud-hotbar.json", "Hotbar", Category.PLAYER) {
    @Dropdown(
        title = "Side",
        description = "Which screen edge the cluster docks against. Left and Right rotate it a quarter turn.",
        options = ["Bottom", "Left", "Top", "Right"]
    )
    var side = BOTTOM

    @Switch(
        title = "Animation",
        description = "Slide the selected-slot highlight between positions instead of snapping."
    )
    var animation = false

    @Switch(title = "Health Animation", description = "Animate the health bar when taking damage / healing.")
    var healthAnimation = true

    @RadioButton(title = "Hardcore Hearts", description = "When to render hardcore hearts.", options = arrayOf("Default", "Always Hardcore", "Always Regular"))
    var hardcoreHearts = 0

    @Switch(title = "Hunger Animation", description = "Animate the hunger bar when it shakes.")
    var hungerAnimation = true

    override val quarterTurns get() = side.coerceIn(BOTTOM, RIGHT)

    override val naturalWidth get() = 182f

    override val naturalHeight get() = 49f

    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - naturalWidth / 2f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - naturalHeight

    override fun defaultOriginX(screenWidth: Int, screenHeight: Int) = when (quarterTurns) {
        LEFT -> 0f
        RIGHT -> screenWidth - naturalHeight
        else -> screenWidth / 2f - naturalWidth / 2f
    }

    override fun defaultOriginY(screenWidth: Int, screenHeight: Int) = when (quarterTurns) {
        TOP -> 0f
        BOTTOM -> screenHeight - naturalHeight
        else -> screenHeight / 2f - naturalWidth / 2f
    }

    // pinned against the docked edge and centred along the free axis
    override val anchorX get() = when (quarterTurns) {
        LEFT -> 0f
        RIGHT -> 1f
        else -> 0.5f
    }

    override val anchorY get() = when (quarterTurns) {
        TOP -> 0f
        BOTTOM -> 1f
        else -> 0.5f
    }

    companion object {
        const val BOTTOM = 0
        const val LEFT = 1
        const val TOP = 2
        const val RIGHT = 3

        /** vanilla centres the experience level this far above the bottom of the screen */
        const val LEVEL_CENTER_Y = 30.5f
    }
}

class HeldItemTooltipHud : VanillaHud("vanillahud-itemtooltip.json", "Held Item Tooltip", Category.INFO) {
    @Switch(title = "Fade Out")
    var fadeOut: Boolean = true

    @Switch(title = "Instant Fade")
    var instantFade: Boolean = false

    override val exampleText get() = "Diamond Sword"
    override val naturalWidth get() = 70f
    override val naturalHeight get() = 11f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - width / 2f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 59f
    override val anchorX get() = 0.5f
    override val anchorY get() = 1f

    override fun measuredWidth(): Float {
        if (previewing) return super.measuredWidth()
        return textWidth { hudAccessor?.lastToolHighlight?.takeUnless { s -> s.isEmpty }?.hoverName?.string }
    }
}

class ScoreboardHud : VanillaHud("vanillahud-scoreboard.json", "Scoreboard", Category.INFO) {
    @Dropdown(
        title = "Show Score Points",
        category = "Score Points",
        options = ["Hide", "Hide Only if Consecutive", "Show Always"]
    )
    var scoreboardPoints: Int = 1

    @Switch(
        title = "Hide Repeating Scores",
        category = "Score Points",
        description = "Hide score points when every visible score shows the same number."
    )
    var hideRepeatingScores: Boolean = true

    @Color(title = "Score Points Color", category = "Score Points")
    var scorePointsColor = PolyColor(0xFFFF5555.toInt())

    @Switch(title = "Scoreboard Title")
    var scoreboardTitle: Boolean = true

    @Switch(
        title = "Persistent Scoreboard Title",
        description = "Keep rendering the scoreboard title even when there are no score lines."
    )
    var persistentTitle: Boolean = false

    @Color(title = "Title Background Color")
    var titleColor = PolyColor(0x66000000)

    @Color(title = "Background Color")
    var backgroundColor = PolyColor(0x4C000000)

    @Switch(
        title = "Keep Background Colour",
        category = "Background Image",
        description = "Draw the solid background colours on top of the image instead of replacing them."
    )
    var keepBackgroundColor: Boolean = false

    @File(
        title = "Image",
        category = "Background Image",
        description = "The PNG image file to use as the scoreboard background.",
        types = ["png"],
        filterName = "PNG Images",
        placeholder = "No image selected"
    )
    var backgroundImagePath: String = ""

    @Dropdown(title = "Text Shadow", options = ["No Shadow", "Shadow"])
    var textType: Int = 0

    val titleBgColor: Int get() = titleColor.argb

    val bodyBgColor: Int get() = backgroundColor.argb

    val hasCustomBackground: Boolean get() = backgroundImagePath.isNotBlank()

    val textShadow: Boolean get() = textType == 1

    fun showScorePoints(scores: Collection<PlayerScoreEntry>): Boolean {
        if (hideRepeatingScores && areScoresRepeating(scores)) return false
        return scoreboardPoints == 2 || (scoreboardPoints == 1 && !areScoresConsecutive(scores))
    }

    fun areScoresRepeating(scores: Collection<PlayerScoreEntry>): Boolean {
        val values = scores
            .filter { !it.isHidden }
            .map { it.value }

        if (values.size < 2) return false
        return values.all { it == values[0] }
    }

    fun areScoresConsecutive(scores: Collection<PlayerScoreEntry>): Boolean {
        val values = scores
            .filter { !it.isHidden }
            .map { it.value }
            .sorted()

        if (values.isEmpty()) return false

        for (i in 0 until values.size - 1) {
            if (values[i] + 1 != values[i + 1]) {
                return false
            }
        }
        return true
    }

    override val naturalWidth get() = 90f
    override val naturalHeight get() = 90f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth - width - 1f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int): Float {
        val s = size() ?: return screenHeight / 2f - naturalHeight / 2f
        return screenHeight / 2f - s.scores * 6f - if (s.title) 10f else 1f
    }
    override val anchorX get() = 1f
    override val anchorY get() = 0.5f

    private class Size(val width: Float, val scores: Int, val title: Boolean)

    private fun size(): Size? = measureOnce { measureSize() }

    private fun measureSize(): Size? {
        val objective = (if (previewing) DemoData.demoScoreboardObjective()
        else mc.level?.scoreboard?.getDisplayObjective(DisplaySlot.SIDEBAR)) ?: return null
        val font = mc.font
        val scoreboard = objective.scoreboard
        val scores = scoreboard.listPlayerScores(objective)
            .filter { !it.isHidden }
            .sortedByDescending { it.value }
            .take(15)
        val showTitle = scoreboardTitle
        if (scores.isEmpty() && !(persistentTitle && showTitle)) return null

        val spaceWidth = font.width(": ")
        val showPoints = showScorePoints(scores)
        var maxWidth = font.width(objective.displayName)
        for (s in scores) {
            val name = PlayerTeam.formatNameForTeam(scoreboard.getPlayersTeam(s.owner()), s.ownerName())
            var line = font.width(name)
            if (showPoints) {
                val scoreWidth = font.width(s.value.toString())
                if (scoreWidth > 0) line += spaceWidth + scoreWidth
            }
            maxWidth = maxOf(maxWidth, line)
        }

        return Size((maxWidth + 4).toFloat(), scores.size, showTitle)
    }

    override fun measuredWidth(): Float = try {
        size()?.width ?: naturalWidth
    } catch (_: Throwable) {
        naturalWidth
    }

    override fun measuredHeight(): Float = try {
        val s = size() ?: return naturalHeight
        (s.scores * 9 + if (s.title) 10 else 1).toFloat()
    } catch (_: Throwable) {
        naturalHeight
    }
}

class TabListHud : VanillaHud("vanillahud-tab.json", "Tab List", Category.INFO) {
    init {
        TabListManager.ensureLoaded()
    }

    @Slider(
        title = "Tab Player Limit",
        description = "How many players can display on the tab list.",
        min = 10f,
        max = 120f
    )
    var playerLimit = 80

    @Dropdown(title = "Mode", options = ["Held", "Toggle"])
    var displayMode = 0

    @Switch(title = "Animation", description = "Slide the tab list open and closed instead of snapping.")
    var animation = true

    @Slider(
        title = "Animation Duration",
        description = "How long the open / close animation takes, in milliseconds.",
        min = 50f,
        max = 1000f
    )
    var animationDuration = 400f

    @Dropdown(title = "Text Shadow", options = ["No Shadow", "Shadow"])
    var textType: Int = 1

    @Switch(title = "Show Header")
    var showHeader: Boolean = true

    @Switch(title = "Show Footer")
    var showFooter: Boolean = true

    @Switch(title = "Show Self At Top")
    var selfAtTop: Boolean = false

    @Switch(title = "Show Player's Head")
    var showHead: Boolean = true

    @Switch(title = "Better Hat Layer")
    var betterHatLayer: Boolean = true

    @Switch(title = "Show Player's Ping")
    var showPing: Boolean = true

    @Switch(title = "Use Number Ping")
    var numberPing: Boolean = true

    @Dropdown(title = "Ping Text", options = ["Small", "Full"])
    var pingType = 1

    @Switch(
        title = "Hide False Ping",
        description = "Hides falsified ping numbers such as a ping of 0 or 1 when on Hypixel"
    )
    var hideFalsePing: Boolean = true

    @Color(title = "Ping Between 0 and 75")
    var pingLevelOne = PolyColor(0xFF55FF55.toInt())

    @Color(title = "Ping Between 75 and 145")
    var pingLevelTwo = PolyColor(0xFF00AA00.toInt())

    @Color(title = "Ping Between 145 and 200")
    var pingLevelThree = PolyColor(0xFFFFFF55.toInt())

    @Color(title = "Ping Between 200 and 300")
    var pingLevelFour = PolyColor(0xFFFFAA00.toInt())

    @Color(title = "Ping Between 300 and 400")
    var pingLevelFive = PolyColor(0xFFFF5555.toInt())

    @Color(title = "Ping Above 400")
    var pingLevelSix = PolyColor(0xFFAA0000.toInt())

    @Color(title = "Tab Widget Color")
    var tabWidgetColor = PolyColor(0x20FFFFFF.toInt())

    @Color(title = "Header Background Color")
    var headerBgColor = PolyColor(0x80000000.toInt())

    @Color(title = "Body Background Color")
    var bodyBgColor = PolyColor(0x80000000.toInt())

    @Color(title = "Footer Background Color")
    var footerBgColor = PolyColor(0x80000000.toInt())

    val tabWidgetArgb: Int get() = tabWidgetColor.argb
    val headerBgArgb: Int get() = headerBgColor.argb
    val bodyBgArgb: Int get() = bodyBgColor.argb
    val footerBgArgb: Int get() = footerBgColor.argb

    fun pingColor(ping: Int): Int = when {
        ping >= 400 -> pingLevelSix
        ping >= 300 -> pingLevelFive
        ping >= 200 -> pingLevelFour
        ping >= 145 -> pingLevelThree
        ping >= 75 -> pingLevelTwo
        else -> pingLevelOne
    }.argb

    override val naturalWidth get() = 200f
    override val naturalHeight get() = 100f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - width / 2f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = 10f
    override val anchorX get() = 0.5f
    override val anchorY get() = 0f

    val backgroundTop get() = 1f

    private var animOpen = false
    private var animStart = 0L
    private var animFrom = 0f
    private var animTo = 0f

    private fun easeOutQuart(x: Float): Float {
        val t = 1f - x
        return 1f - t * t * t * t
    }

    fun updateOpen(open: Boolean) {
        if (open == animOpen) return
        animOpen = open
        animFrom = clipFraction()
        animTo = if (open) 1f else 0f
        animStart = System.currentTimeMillis()
    }

    fun clipFraction(): Float {
        if (!animation) return if (animOpen) 1f else 0f
        val dur = animationDuration.coerceAtLeast(1f)
        val t = ((System.currentTimeMillis() - animStart).toFloat() / dur).coerceIn(0f, 1f)
        return animFrom + (animTo - animFrom) * easeOutQuart(t)
    }

    fun isRendering(): Boolean = animOpen || clipFraction() > 0.001f

    fun foreignBounds(): TabListCompat.Bounds? {
        if (previewing) return null
        return try {
            val overlay = tabOverlay()
            TabListCompat.bounds(overlay?.header, overlay?.footer)
        } catch (_: Throwable) {
            null
        }
    }

    private fun tabOverlay(): IPlayerTabOverlay? = try {
        //? if >=26.2 {
        mc.gui.hud.tabList as IPlayerTabOverlay
        //?} else {
        /*mc.gui.tabList as IPlayerTabOverlay
        *///?}
    } catch (_: Throwable) {
        null
    }

    private fun players(): List<PlayerInfo> = try {
        val real = mc.connection?.listedOnlinePlayers?.take(playerLimit) ?: emptyList()
        if (previewing) TabListManager.devInfo.take(playerLimit) else real
    } catch (_: Throwable) {
        emptyList()
    }

    private fun displayName(info: PlayerInfo): Component {
        info.tabListDisplayName?.let { return it }
        val name = try {
            //? if >=1.21.9 {
            info.profile.name() ?: ""
            //?} else {
            /*info.profile.name ?: ""
            *///?}
        } catch (_: Throwable) {
            ""
        }
        return PlayerTeam.formatNameForTeam(info.team, Component.literal(name))
    }

    private fun tabText(editing: Component, live: () -> Component?, show: Boolean): Component? {
        if (!show) return null
        if (previewing) return editing
        return try { live() } catch (_: Throwable) { null }
    }

    private var hfHeader: Component? = null
    private var hfFooter: Component? = null
    private var hfScreenWidth = -1
    private var hfWidth = 0
    private var hfHeight = 0

    private fun measureHeaderFooter(header: Component?, footer: Component?, screenWidth: Int) {
        if (header == hfHeader && footer == hfFooter && screenWidth == hfScreenWidth) return
        val font = mc.font
        var width = 0
        var height = 0
        for (text in arrayOf(header, footer)) {
            if (text == null) continue
            val lines = font.split(text, screenWidth - 50)
            for (l in lines) width = maxOf(width, font.width(l))
            height += lines.size * font.lineHeight + 1
        }
        hfHeader = header
        hfFooter = footer
        hfScreenWidth = screenWidth
        hfWidth = width
        hfHeight = height
    }

    private fun size(): Pair<Float, Float>? = measureOnce { measureSize() }

    private fun measureSize(): Pair<Float, Float>? {
        val list = players()
        if (list.isEmpty()) return null
        val font = mc.font
        val line = font.lineHeight
        val screenWidth = HudManager.guiScreenWidth.toInt().coerceAtLeast(1)

        var maxName = 0
        for (p in list) maxName = maxOf(maxName, font.width(displayName(p)))

        val count = list.size
        var rows = count
        var columns = 1
        while (rows > 20) {
            columns++
            rows = (count + columns - 1) / columns
        }

        val headWidth = if (showHead) 9 else 0
        val pingReserve = if (showPing && numberPing && pingType == 1) font.width("999") + 3 else 0
        val cellWidth = headWidth + maxName + maxOf(13, pingReserve)
        val slotWidth = minOf(columns * cellWidth, screenWidth - 50) / columns
        var width = slotWidth * columns + (columns - 1) * 5
        var height = rows * line

        val overlay = tabOverlay()
        val header = tabText(PREVIEW_HEADER, { overlay?.header }, showHeader)
        val footer = tabText(PREVIEW_FOOTER, { overlay?.footer }, showFooter)
        measureHeaderFooter(header, footer, screenWidth)
        width = maxOf(width, hfWidth)
        height += hfHeight

        return (width + 2).toFloat() to (height + 2).toFloat()
    }

    override fun measuredWidth(): Float = try {
        size()?.first ?: naturalWidth
    } catch (_: Throwable) {
        naturalWidth
    }

    override fun measuredHeight(): Float = try {
        size()?.second ?: naturalHeight
    } catch (_: Throwable) {
        naturalHeight
    }

    private companion object {
        // Stable instances so the header/footer measure cache also hits while previewing.
        val PREVIEW_HEADER: Component = Component.literal("Tab List")
        val PREVIEW_FOOTER: Component = Component.literal("VanillaHUD")
    }
}

class TitleHud : VanillaHud("vanillahud-title.json", "Title & Subtitle", Category.INFO) {
    @Switch(
        title = "Auto Scale",
        description = "Shrink the title and subtitle so they always fit within the screen width."
    )
    var autoTitleScale = false

    override val naturalWidth get() = 120f
    override val naturalHeight get() = 68f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - width / 2f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight / 2f - 40f
    override val anchorX get() = 0.5f
    override val anchorY get() = 0.5f

    // title top edge sits a fixed distance above screen centre and the subtitle only grows downwards
    override val positionAnchorY get() = 0f

    private class Size(val width: Float, val height: Float)

    private fun size(): Size? = measureOnce { measureSize() }

    private fun measureSize(): Size {
        val gui = if (previewing) null else hudAccessor
        val title = gui?.title?.string ?: "Title"
        val subtitle = gui?.subtitle?.string ?: "Subtitle"
        val font = mc.font
        val line = font.lineHeight
        return Size(
            maxOf(font.width(title) * 4, font.width(subtitle) * 2).toFloat(),
            if (subtitle.isNotBlank()) (line * 4 + 14 + line * 2).toFloat() else (line * 4).toFloat(),
        )
    }

    override fun measuredWidth(): Float = try {
        size()?.width ?: naturalWidth
    } catch (_: Throwable) {
        naturalWidth
    }

    override fun measuredHeight(): Float = try {
        size()?.height ?: naturalHeight
    } catch (_: Throwable) {
        naturalHeight
    }
}

class StatusEffectsHud : VanillaHud("vanillahud-statuseffects.json", "Status Effects", Category.PLAYER) {
    override val naturalWidth get() = 50f
    override val naturalHeight get() = 50f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth - width
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = 1f
    override val anchorX get() = 1f
    override val anchorY get() = 0f

    private class Counts(val beneficial: Int, val harmful: Int)

    private fun counts(): Counts? = measureOnce { measureCounts() }

    private fun measureCounts(): Counts? {
        val real = mc.player?.activeEffects ?: emptyList()
        val effects = if (previewing) DemoData.demoEffects() else real
        var beneficial = 0
        var harmful = 0
        for (effect in effects) {
            if (!effect.showIcon()) continue
            if (effect.effect.value().isBeneficial) beneficial++ else harmful++
        }
        if (beneficial == 0 && harmful == 0) return null
        return Counts(beneficial, harmful)
    }

    override fun measuredWidth(): Float = try {
        val c = counts() ?: return naturalWidth
        (25 * maxOf(c.beneficial, c.harmful)).toFloat()
    } catch (_: Throwable) {
        naturalWidth
    }

    override fun measuredHeight(): Float = try {
        val c = counts() ?: return naturalHeight
        if (c.harmful > 0) 50f else 24f
    } catch (_: Throwable) {
        naturalHeight
    }
}

class ClosedCaptionsHud : VanillaHud("vanillahud-closedcaptions.json", "Closed Captioning", Category.INFO) {
    @Color(title = "Text Color")
    var captionTextColor = PolyColor(0xFFFFFFFF.toInt())

    @Color(
        title = "Background Color",
        description = "Overrides the vanilla text background opacity setting for closed captions."
    )
    var captionBgColor = PolyColor(0xCC000000.toInt())

    @Dropdown(title = "Text Shadow", options = ["No Shadow", "Shadow"])
    var textType: Int = 1

    val captionBgArgb: Int get() = captionBgColor.argb

    val textShadow: Boolean get() = textType == 1

    fun captionTextArgb(vanilla: Int): Int {
        val fade = vanilla and 0xFF
        val color = captionTextColor.argb
        val a = color ushr 24 and 0xFF
        val r = (color ushr 16 and 0xFF) * fade / 255
        val g = (color ushr 8 and 0xFF) * fade / 255
        val b = (color and 0xFF) * fade / 255
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    override val naturalWidth get() = 90f
    override val naturalHeight get() = 50f

    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth - width - 1f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 30f - height
    override val anchorX get() = 1f
    override val anchorY get() = 1f

    private fun texts(): List<Component> {
        val overlay = hudAccessor?.subtitleOverlay as? ISubtitleOverlay ?: return emptyList()
        return overlay.audibleSubtitles.mapNotNull { (it as? ISubtitle)?.subtitleText }
    }

    private class Size(val width: Float, val height: Float)

    private fun size(): Size? = measureOnce { measureSize() }

    private fun measureSize(): Size {
        val texts = texts()
        if (texts.isEmpty()) return Size(naturalWidth, naturalHeight)
        val font = mc.font
        val row = texts.maxOf { font.width(it) } +
            font.width("<") + font.width(" ") + font.width(">") + font.width(" ")
        return Size((row / 2 * 2 + 2).toFloat(), (texts.size * CAPTION_ROW).toFloat())
    }

    override fun measuredWidth(): Float = try {
        size()?.width ?: naturalWidth
    } catch (_: Throwable) {
        naturalWidth
    }

    override fun measuredHeight(): Float = try {
        size()?.height ?: naturalHeight
    } catch (_: Throwable) {
        naturalHeight
    }

    private companion object {
        const val CAPTION_ROW = 10
    }
}

object Huds {
    val hotbar = HotbarHud()
    val actionBar = ActionBarHud()
    val heldItemTooltip = HeldItemTooltipHud()
    val title = TitleHud()
    val scoreboard = ScoreboardHud()
    val tabList = TabListHud()
    val bossBar = BossBarHud()
    val statusEffects = StatusEffectsHud()
    val closedCaptions = ClosedCaptionsHud()

    val all: Array<VanillaHud>
        get() = arrayOf(
            hotbar, actionBar, heldItemTooltip,
            title, scoreboard, tabList, bossBar, statusEffects, closedCaptions,
        )
}
