package org.polyfrost.vanillahud.hud

//? if > 1.8.9 {
import net.minecraft.client.gui.components.LerpingBossEvent
import net.minecraft.client.multiplayer.PlayerInfo
import net.minecraft.network.chat.Component
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.PlayerScoreEntry
import net.minecraft.world.scores.PlayerTeam
//?} else {
/*import net.minecraft.client.gui.BossBar
import net.minecraft.client.network.PlayerInfo
import net.minecraft.scoreboard.ScoreboardScore
import net.minecraft.scoreboard.team.Team
import net.minecraft.text.Text
*///?}
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.annotations.*
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import org.polyfrost.vanillahud.compat.TabListCompat
import org.polyfrost.vanillahud.mixin.access.IBossHealthOverlay
import org.polyfrost.vanillahud.mixin.access.IPlayerTabOverlay
import org.polyfrost.vanillahud.mixin.access.ISubtitle
import org.polyfrost.vanillahud.mixin.access.ISubtitleOverlay
import org.polyfrost.vanillahud.render.HudTransform
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

    //? if > 1.8.9 {
    override fun hasContent() =
        previewing || hudAccessor?.let { it.overlay != null && it.overlayMessageTime > 0 } ?: true
    //?} else {
    /*override fun hasContent() =
        previewing || hudAccessor?.let { !it.overlayText.isNullOrEmpty() && it.overlayMessageCooldown > 0 } ?: true
    *///?}

    override fun measuredWidth(): Float {
        if (previewing) return super.measuredWidth()
        //? if > 1.8.9 {
        return measureOnce({ mix(0L, hudAccessor?.overlay) }) { textWidth { hudAccessor?.overlay?.string } }
            ?: naturalWidth
        //?} else {
        /*return measureOnce({ mix(0L, hudAccessor?.overlayText) }) { textWidth { hudAccessor?.overlayText } }
            ?: naturalWidth
        *///?}
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

    //? if > 1.8.9 {
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
    //?} else {
    /*// 1.8.9 shows at most one bar, and only for as long as the server keeps refreshing its timer
    private fun bossNames(): List<String> {
        if (previewing) return listOf(DemoData.demoBossName())
        return try {
            val name = BossBar.name
            if (name.isNullOrEmpty() || BossBar.timer <= 0) emptyList() else listOf(name)
        } catch (_: Throwable) {
            emptyList()
        }
    }
    *///?}

    //? if > 1.8.9 {
    override fun hasContent() = previewing || bossEvents().isNotEmpty()
    //?} else
    //override fun hasContent() = previewing || bossNames().isNotEmpty()

    private class Size(val width: Float, val height: Float)

    private fun size(): Size? = measureOnce({
        var h = mix(0L, renderText)
        //? if > 1.8.9 {
        for (e in bossEvents()) h = mix(mix(h, e), e.name)
        //?} else
        //for (n in bossNames()) h = mix(h, n)
        h
    }) { measureSize() }

    //? if > 1.8.9 {
    private fun measureSize(): Size {
        val events = bossEvents()
        val width = if (events.isEmpty() || !renderText) naturalWidth
        else events.fold(naturalWidth) { acc, e -> maxOf(acc, mc.font.width(e.name).toFloat()) }
        val n = events.size
        val height = if (n == 0) naturalHeight else ((n - 1) * 19 + if (renderText) 14 else 5).toFloat()
        return Size(width, height)
    }
    //?} else {
    /*private fun measureSize(): Size {
        val names = bossNames()
        val width = if (names.isEmpty() || !renderText) naturalWidth
        else names.fold(naturalWidth) { acc, n -> maxOf(acc, mc.font.width(n).toFloat()) }
        val height = if (names.isEmpty()) naturalHeight else (if (renderText) 14 else 5).toFloat()
        return Size(width, height)
    }
    *///?}

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

    @Switch(title = "Render Experience Bar")
    var experienceBar = true

    @Switch(title = "Render Experience Level")
    var experienceLevel = true

    //? if >=1.21.6 {
    @Switch(
        title = "Overlay Locator Bar",
        description = "Draw the locator bar's waypoints over the experience / jump bar instead of replacing it."
    )
    var overlayLocatorBar = false
    //?}

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

    //? if > 1.8.9 {
    override fun hasContent() = previewing || hudAccessor?.let {
        !it.lastToolHighlight.isEmpty && (!fadeOut || it.toolHighlightTimer > 0)
    } ?: true
    //?} else {
    /*// 1.8.9 leaves selectedItem null rather than holding an empty stack
    override fun hasContent() = previewing || hudAccessor?.let {
        it.lastToolHighlight != null && (!fadeOut || it.itemSelectedTimer > 0)
    } ?: true
    *///?}

    override fun measuredWidth(): Float {
        if (previewing) return super.measuredWidth()
        //? if > 1.8.9 {
        return measureOnce({ mix(0L, hudAccessor?.lastToolHighlight) }) {
            textWidth { hudAccessor?.lastToolHighlight?.takeUnless { s -> s.isEmpty }?.hoverName?.string }
        } ?: naturalWidth
        //?} else {
        /*return measureOnce({ mix(0L, hudAccessor?.lastToolHighlight) }) {
            textWidth { hudAccessor?.lastToolHighlight?.legacyHoverName }
        } ?: naturalWidth
        *///?}
    }
}

class ScoreboardHud : VanillaHud("vanillahud-scoreboard.json", "Scoreboard", Category.INFO) {
    @Dropdown(
        title = "Show Score Points",
        subcategory = "Score Points",
        options = ["Hide", "Hide Only if Consecutive", "Show Always"]
    )
    var scoreboardPoints: Int = 1

    @Switch(
        title = "Hide Repeating Scores",
        subcategory = "Score Points",
        description = "Hide score points when every visible score shows the same number."
    )
    var hideRepeatingScores: Boolean = true

    @Color(title = "Score Points Color", subcategory = "Score Points")
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
        subcategory = "Background Image",
        description = "Draw the solid background colours on top of the image instead of replacing them."
    )
    var keepBackgroundColor: Boolean = false

    @File(
        title = "Image",
        subcategory = "Background Image",
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

    //? if > 1.8.9 {
    fun showScorePoints(scores: Collection<PlayerScoreEntry>): Boolean {
    //?} else
    //fun showScorePoints(scores: Collection<ScoreboardScore>): Boolean {
        if (previewing) return scoreboardPoints == 2
        if (hideRepeatingScores && areScoresRepeating(scores)) return false
        return scoreboardPoints == 2 || (scoreboardPoints == 1 && !areScoresConsecutive(scores))
    }

    //? if > 1.8.9 {
    fun areScoresRepeating(scores: Collection<PlayerScoreEntry>): Boolean {
        val values = scores
            .filter { !it.isHidden }
            .map { it.value }
    //?} else {
    /*fun areScoresRepeating(scores: Collection<ScoreboardScore>): Boolean {
        val values = scores.map { it.get() }
    *///?}

        if (values.size < 2) return false
        return values.all { it == values[0] }
    }

    //? if > 1.8.9 {
    fun areScoresConsecutive(scores: Collection<PlayerScoreEntry>): Boolean {
        val values = scores
            .filter { !it.isHidden }
            .map { it.value }
            .sorted()
    //?} else {
    /*fun areScoresConsecutive(scores: Collection<ScoreboardScore>): Boolean {
        val values = scores.map { it.get() }.sorted()
    *///?}

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
        val s = try {
            size()
        } catch (_: Throwable) {
            null
        } ?: return screenHeight / 2f - naturalHeight / 2f
        return screenHeight / 2f - s.scores * 6f - if (s.title) 10f else 1f
    }
    override val anchorX get() = 1f
    override val anchorY get() = 0.5f

    //? if > 1.8.9 {
    override fun hasContent(): Boolean {
        if (previewing) return true
        val scoreboard = mc.level?.scoreboard ?: return true
        val player = mc.player ?: return true
        return scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) != null ||
            scoreboard.getPlayersTeam(player.scoreboardName) != null
    }
    //?} else {
    /*override fun hasContent(): Boolean {
        if (previewing) return true
        val scoreboard = mc.level?.scoreboard ?: return true
        val player = mc.player ?: return true
        return scoreboard.getDisplayObjective(SIDEBAR_SLOT) != null ||
            scoreboard.getTeamOfMember(player.name) != null
    }
    *///?}

    private class Size(val width: Float, val scores: Int, val title: Boolean)

    private fun size(): Size? = measureOnce({
        var h = HudInternals.scoreboardRevision.toLong()
        //? if > 1.8.9 {
        h = mix(h, mc.level?.scoreboard?.getDisplayObjective(DisplaySlot.SIDEBAR))
        //?} else
        //h = mix(h, mc.level?.scoreboard?.getDisplayObjective(SIDEBAR_SLOT))
        h = mix(h, scoreboardPoints)
        h = mix(h, hideRepeatingScores)
        h = mix(h, scoreboardTitle)
        mix(h, persistentTitle)
    }) { measureSize() }

    //? if > 1.8.9 {
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
    //?} else {
    /*private fun measureSize(): Size? {
        val objective = (if (previewing) DemoData.demoScoreboardObjective()
        else mc.level?.scoreboard?.getDisplayObjective(SIDEBAR_SLOT)) ?: return null
        val font = mc.font
        val scoreboard = objective.scoreboard
        val scores = scoreboard.getScores(objective)
            .sortedByDescending { it.get() }
            .take(15)
        val showTitle = scoreboardTitle
        if (scores.isEmpty() && !(persistentTitle && showTitle)) return null

        val spaceWidth = font.width(": ")
        val showPoints = showScorePoints(scores)
        var maxWidth = font.width(objective.displayName)
        for (s in scores) {
            val name = Team.getMemberDisplayName(scoreboard.getTeamOfMember(s.owner), s.owner)
            var line = font.width(name)
            if (showPoints) {
                val scoreWidth = font.width(s.get().toString())
                if (scoreWidth > 0) line += spaceWidth + scoreWidth
            }
            maxWidth = maxOf(maxWidth, line)
        }

        return Size((maxWidth + 4).toFloat(), scores.size, showTitle)
    }

    private companion object {
        // 1.8.9 addresses display slots by index; 1 is the sidebar
        const val SIDEBAR_SLOT = 1
    }
    *///?}

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

    fun hidesPing(ping: Int): Boolean {
        if (previewing) return false
        return hideFalsePing && (ping <= 1 || ping >= 999)
    }

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

    fun resetOpen() {
        animOpen = false
        animFrom = 0f
        animTo = 0f
        animStart = 0L
    }

    fun clipFraction(): Float {
        if (!animation) return if (animOpen) 1f else 0f
        val dur = animationDuration.coerceAtLeast(1f)
        val t = ((System.currentTimeMillis() - animStart).toFloat() / dur).coerceIn(0f, 1f)
        return animFrom + (animTo - animFrom) * easeOutQuart(t)
    }

    fun isRendering(): Boolean = animOpen || clipFraction() > 0.001f

    // the open state only updates partway through vanilla's render, after the transform has already measured,
    // so the held key covers the frame the list opens on
    //? if > 1.8.9 {
    override fun shouldShow() = previewing || isRendering() || mc.options.keyPlayerList.isDown
    //?} else
    //override fun shouldShow() = previewing || isRendering() || mc.options.playerListKey.isPressed

    override fun hasContent() = shouldShow()

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
        //?} elif > 1.8.9 {
        /*mc.gui.tabList as IPlayerTabOverlay
        *///?} else {
        /*mc.gui.playerTabOverlay as IPlayerTabOverlay
        *///?}
    } catch (_: Throwable) {
        null
    }

    private fun players(): List<PlayerInfo> = try {
        //? if > 1.8.9 {
        val real = mc.connection?.listedOnlinePlayers?.take(playerLimit) ?: emptyList()
        //?} else
        //val real = mc.networkHandler?.onlinePlayers?.take(playerLimit) ?: emptyList()
        if (previewing) TabListManager.devInfo.take(playerLimit) else real
    } catch (_: Throwable) {
        emptyList()
    }

    //? if > 1.8.9 {
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
    //?} else {
    /*// 1.8.9 formats tab list names as legacy strings rather than components
    private fun displayName(info: PlayerInfo): String {
        info.displayName?.let { return it.formattedString }
        val name = try {
            info.profile.name ?: ""
        } catch (_: Throwable) {
            ""
        }
        return Team.getMemberDisplayName(info.team, name)
    }
    *///?}

    //? if > 1.8.9 {
    private fun tabText(editing: Component, live: () -> Component?, show: Boolean): Component? {
    //?} else
    //private fun tabText(editing: Text, live: () -> Text?, show: Boolean): Text? {
        if (!show) return null
        if (previewing) return editing
        return try { live() } catch (_: Throwable) { null }
    }

    //? if > 1.8.9 {
    private var hfHeader: Component? = null
    private var hfFooter: Component? = null
    //?} else {
    /*private var hfHeader: Text? = null
    private var hfFooter: Text? = null
    *///?}
    private var hfScreenWidth = -1
    private var hfWidth = 0
    private var hfHeight = 0

    //? if > 1.8.9 {
    private fun measureHeaderFooter(header: Component?, footer: Component?, screenWidth: Int) {
    //?} else
    //private fun measureHeaderFooter(header: Text?, footer: Text?, screenWidth: Int) {
        if (header == hfHeader && footer == hfFooter && screenWidth == hfScreenWidth) return
        val font = mc.font
        var width = 0
        var height = 0
        for (text in arrayOf(header, footer)) {
            if (text == null) continue
            //? if > 1.8.9 {
            val lines = font.split(text, screenWidth - 50)
            //?} else
            //val lines = font.split(text.formattedString, screenWidth - 50)
            for (l in lines) width = maxOf(width, font.width(l))
            height += lines.size * font.lineHeight + 1
        }
        hfHeader = header
        hfFooter = footer
        hfScreenWidth = screenWidth
        hfWidth = width
        hfHeight = height
    }

    private fun size(): Pair<Float, Float>? = measureOnce({ sizeKey() }) { if (shouldShow()) measureSize() else null }

    private fun sizeKey(): Long {
        if (!shouldShow()) return Long.MIN_VALUE
        var h = HudInternals.scoreboardRevision.toLong()
        val overlay = tabOverlay()
        h = mix(h, overlay?.header)
        h = mix(h, overlay?.footer)
        var n = 0
        //? if > 1.8.9 {
        for (p in mc.connection?.listedOnlinePlayers ?: emptyList()) {
            if (n++ >= playerLimit) break
            h = mix(mix(h, p), p.tabListDisplayName)
        }
        //?} else {
        /*for (p in mc.networkHandler?.onlinePlayers ?: emptyList()) {
            if (n++ >= playerLimit) break
            h = mix(mix(h, p), p.displayName)
        }
        *///?}
        h = mix(h, n)
        h = mix(h, playerLimit)
        h = mix(h, showHead)
        h = mix(h, showPing)
        h = mix(h, numberPing)
        h = mix(h, pingType)
        h = mix(h, showHeader)
        return mix(h, showFooter)
    }

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
        //? if > 1.8.9 {
        val PREVIEW_HEADER: Component = Component.literal("Tab List")
        val PREVIEW_FOOTER: Component = Component.literal("VanillaHUD")
        //?} else {
        /*val PREVIEW_HEADER: Text = DemoData.demoTabHeader()
        val PREVIEW_FOOTER: Text = DemoData.demoTabFooter()
        *///?}
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

    override val sectionAnchorY get() = 0.5f

    //? if > 1.8.9 {
    override fun shouldShow() = previewing || hudAccessor?.let { it.title != null && it.titleTime > 0 } == true
    //?} else
    //override fun shouldShow() = previewing || hudAccessor?.let { it.titleText != null && it.titleTime > 0 } == true

    override fun hasContent() = shouldShow()

    private class Size(val width: Float, val height: Float)

    private fun size(): Size? = measureOnce({
        if (!shouldShow()) Long.MIN_VALUE
        //? if > 1.8.9 {
        else hudAccessor.let { mix(mix(0L, it?.title), it?.subtitle) }
        //?} else
        //else hudAccessor.let { mix(mix(0L, it?.titleText), it?.subtitleText) }
    }) { if (shouldShow()) measureSize() else null }

    private fun measureSize(): Size {
        val gui = if (previewing) null else hudAccessor
        //? if > 1.8.9 {
        val title = gui?.title?.string ?: "Title"
        val subtitle = gui?.subtitle?.string ?: "Subtitle"
        //?} else {
        /*val title = gui?.titleText ?: "Title"
        val subtitle = gui?.subtitleText ?: "Subtitle"
        *///?}
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

//? if > 1.8.9 {
class StatusEffectsHud : VanillaHud("vanillahud-statuseffects.json", "Status Effects", Category.PLAYER) {
    override val naturalWidth get() = 50f
    override val naturalHeight get() = 50f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth - width
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = 1f
    override val anchorX get() = 1f
    override val anchorY get() = 0f

    override fun hasContent() = previewing || mc.player?.activeEffects?.isNotEmpty() ?: true

    private class Counts(val beneficial: Int, val harmful: Int)

    private fun counts(): Counts? = measureOnce({ frame }) { measureCounts() }

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

    private fun size(): Size? = measureOnce({
        var h = 0L
        val overlay = hudAccessor?.subtitleOverlay as? ISubtitleOverlay
        for (s in overlay?.audibleSubtitles ?: emptyList()) h = mix(h, (s as? ISubtitle)?.subtitleText)
        mix(h, overlay?.audibleSubtitles?.size ?: 0)
    }) { measureSize() }

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
//?}

object Huds {
    private val hotbarProvider = HotbarHud()
    private val actionBarProvider = ActionBarHud()
    private val heldItemTooltipProvider = HeldItemTooltipHud()
    private val titleProvider = TitleHud()
    private val scoreboardProvider = ScoreboardHud()
    private val tabListProvider = TabListHud()
    private val bossBarProvider = BossBarHud()
    //? if > 1.8.9 {
    private val statusEffectsProvider = StatusEffectsHud()
    private val closedCaptionsProvider = ClosedCaptionsHud()
    //?}

    val hotbar: HotbarHud get() = HudTransform.live(hotbarProvider)
    val actionBar: ActionBarHud get() = HudTransform.live(actionBarProvider)
    val heldItemTooltip: HeldItemTooltipHud get() = HudTransform.live(heldItemTooltipProvider)
    val title: TitleHud get() = HudTransform.live(titleProvider)
    val scoreboard: ScoreboardHud get() = HudTransform.live(scoreboardProvider)
    val tabList: TabListHud get() = HudTransform.live(tabListProvider)
    val bossBar: BossBarHud get() = HudTransform.live(bossBarProvider)
    //? if > 1.8.9 {
    val statusEffects: StatusEffectsHud get() = HudTransform.live(statusEffectsProvider)
    val closedCaptions: ClosedCaptionsHud get() = HudTransform.live(closedCaptionsProvider)
    //?}

    val all: Array<VanillaHud>
        //? if > 1.8.9 {
        get() = arrayOf(
            hotbarProvider, actionBarProvider, heldItemTooltipProvider, titleProvider,
            scoreboardProvider, tabListProvider, bossBarProvider, statusEffectsProvider,
            closedCaptionsProvider,
        )
        //?} else {
        /*get() = arrayOf(
            hotbarProvider, actionBarProvider, heldItemTooltipProvider,
            titleProvider, scoreboardProvider, tabListProvider, bossBarProvider,
        )
        *///?}
}
