package org.polyfrost.vanillahud.hud

import net.minecraft.client.gui.components.LerpingBossEvent
import net.minecraft.client.multiplayer.PlayerInfo
import net.minecraft.network.chat.Component
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.PlayerScoreEntry
import net.minecraft.world.scores.PlayerTeam
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import org.polyfrost.oneconfig.api.config.v1.annotations.*
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.api.hud.v1.HudManager.isEditing
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import org.polyfrost.vanillahud.compat.CustomScoreboardBridge
import org.polyfrost.vanillahud.mixin.access.IBossHealthOverlay
import org.polyfrost.vanillahud.mixin.access.IPlayerTabOverlay
import org.polyfrost.vanillahud.mixin.access.ISubtitle
import org.polyfrost.vanillahud.mixin.access.ISubtitleOverlay
import org.polyfrost.vanillahud.render.ScoreboardBackground
import org.polyfrost.vanillahud.util.DemoData
import org.polyfrost.vanillahud.util.TabListManager

class ActionBarHud : VanillaHud("vanillahud/actionbar.json", "Action Bar", Category.INFO) {
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

    override fun measuredWidth(): Float {
        if (isEditing) return super.measuredWidth()
        return textWidth { hudAccessor?.overlay?.string }
    }
}

class AirHud : VanillaHud("vanillahud/air.json", "Air", Category.PLAYER) {
    @Checkbox(title = "Link with health")
    var healthLink = false

    @Checkbox(title = "Link with mount health")
    var mountLink = true

    override val naturalWidth get() = 81f
    override val naturalHeight get() = 9f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f + 10f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 49f

    override fun linkTarget() = when {
        mountLink -> Huds.mountHealth
        healthLink -> Huds.health
        else -> null
    }
}

class ArmorHud : VanillaHud("vanillahud/armor.json", "Armor", Category.PLAYER) {
    @Checkbox(title = "Link with health")
    var healthLink = true

    @Checkbox(title = "Link with mount health")
    var mountLink = false

    override val naturalWidth get() = 81f
    override val naturalHeight get() = 9f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - 91f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 49f

    override fun linkTarget() = when {
        mountLink -> Huds.mountHealth
        healthLink -> Huds.health
        else -> null
    }
}

class BossBarHud : VanillaHud("vanillahud/bossbar.json", "Boss Bar", Category.COMBAT) {
    @Switch(title = "Render Text")
    var renderText = true

    @Switch(title = "Render Health")
    var renderHealth = true

    override val naturalWidth get() = 182f
    override val naturalHeight get() = 30f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - width / 2f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = if (renderText) 3f else 12f

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
        if (live.isEmpty() && isEditing) return DemoData.demoBossEvents()
        return live
    }

    override fun measuredWidth(): Float = try {
        val events = bossEvents()
        if (events.isEmpty() || !renderText) return naturalWidth
        events.fold(naturalWidth) { acc, e -> maxOf(acc, mc.font.width(e.name).toFloat()) }
    } catch (_: Throwable) {
        naturalWidth
    }

    override fun measuredHeight(): Float = try {
        val n = bossEvents().size
        if (n == 0) naturalHeight else ((n - 1) * 19 + if (renderText) 14 else 5).toFloat()
    } catch (_: Throwable) {
        naturalHeight
    }
}

class ExperienceBarHud : VanillaHud("vanillahud/experience.json", "Experience Bar", Category.PLAYER) {
    override val naturalWidth get() = 182f
    override val naturalHeight get() = 5f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - 91f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 29f
}

class ExperienceLevelHud : VanillaHud("vanillahud/experience-level.json", "Experience Level", Category.PLAYER) {
    override val naturalWidth get() = 16f
    override val naturalHeight get() = 9f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - 8f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 35f
}

class HealthHud : VanillaHud("vanillahud/health.json", "Health", Category.PLAYER) {
    @Checkbox(title = "Link with mount health")
    var mountLink = false

    @Switch(title = "Health Animation", description = "Animate the health bar when taking damage / healing.")
    var animation = true

    override val naturalWidth get() = 81f
    override val naturalHeight get() = 9f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - 91f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 39f

    override fun linkTarget() = if (mountLink) Huds.mountHealth else null
}

class HotbarHud : VanillaHud("vanillahud/hotbar.json", "Hotbar", Category.PLAYER) {
    @Dropdown(title = "Mode", options = ["Horizontal", "Vertical"])
    var hotbarMode = 0

    @Switch(
        title = "Animation",
        description = "Slide the selected-slot highlight between positions instead of snapping."
    )
    var animation = false

    val vertical get() = hotbarMode == 1

    override val naturalWidth get() = if (vertical) 22f else 182f
    override val naturalHeight get() = if (vertical) 182f else 22f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) =
        if (vertical) 4f else screenWidth / 2f - 91f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) =
        if (vertical) screenHeight / 2f - 91f else screenHeight - 22f
}

class HungerHud : VanillaHud("vanillahud/hunger.json", "Hunger", Category.PLAYER) {
    @Checkbox(title = "Link with health")
    var healthLink: Boolean = false

    @Checkbox(title = "Link with mount health")
    var mountLink: Boolean = false

    @Switch(title = "Hunger Animation", description = "Animate the hunger bar when it shakes.")
    var animation = true

    override val naturalWidth get() = 81f
    override val naturalHeight get() = 9f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f + 10f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 39f

    override fun linkTarget() = when {
        mountLink -> Huds.mountHealth
        healthLink -> Huds.health
        else -> null
    }
}

class MountHealthHud : VanillaHud("vanillahud/mount.json", "Mount Health", Category.PLAYER) {
    @Checkbox(title = "Link with health")
    var healthLink: Boolean = false

    override val naturalWidth get() = 81f
    override val naturalHeight get() = 9f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f + 10f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 39f

    override fun linkTarget() = if (healthLink) Huds.health else null
}

class HeldItemTooltipHud : VanillaHud("vanillahud/itemtooltip.json", "Held Item Tooltip", Category.INFO) {
    @Switch(title = "Fade Out")
    var fadeOut: Boolean = true

    @Switch(title = "Instant Fade")
    var instantFade: Boolean = false

    override val exampleText get() = "Diamond Sword"
    override val naturalWidth get() = 70f
    override val naturalHeight get() = 11f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - width / 2f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 59f

    override fun measuredWidth(): Float {
        if (isEditing) return super.measuredWidth()
        return textWidth { hudAccessor?.lastToolHighlight?.takeUnless { s -> s.isEmpty }?.hoverName?.string }
    }
}

class ScoreboardHud : VanillaHud("vanillahud/scoreboard.json", "Scoreboard", Category.INFO) {
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
        title = "Custom Background Image",
        category = "Background Image",
        description = "Render an image behind the scoreboard instead of the solid background colour."
    )
    var customBackground: Boolean = false

    @Button(
        title = "Choose Image",
        text = "Browse…",
        category = "Background Image",
        description = "Pick an image file to use as the scoreboard background."
    )
    fun chooseImage() {
        ScoreboardBackground.chooseFile()?.let { backgroundImagePath = it }
    }

    @Text(
        title = "Image Path",
        category = "Background Image",
        placeholder = "No image selected"
    )
    var backgroundImagePath: String = ""

    @Dropdown(title = "Text Type", options = ["No Shadow", "Shadow"])
    var textType: Int = 0

    val titleBgColor: Int get() = titleColor.argb

    val bodyBgColor: Int get() = backgroundColor.argb

    val hasCustomBackground: Boolean get() = customBackground && backgroundImagePath.isNotBlank()

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

    private class Size(val width: Float, val scores: Int, val title: Boolean)

    private fun size(): Size? {
        val objective = (mc.level?.scoreboard?.getDisplayObjective(DisplaySlot.SIDEBAR)
        ?: if (isEditing) DemoData.demoScoreboardObjective() else null) ?: return null
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

/**
 * For meowdding custom scoreboard compatibility.
 */
class CustomScoreboardHud : VanillaHud("vanillahud/customscoreboard.json", "Custom Scoreboard", Category.INFO) {
    private var options = false

    fun stealOptions() {
        if (options) return
        val target = tree ?: return
        val csTree = try {
            ConfigManager.active().get(CustomScoreboardBridge.CONFIG_ID)
        } catch (_: Throwable) {
            null
        } ?: return
        target.put(csTree)
        options = true
    }

    override val naturalWidth get() = 90f
    override val naturalHeight get() = 90f

    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int): Float {
        val d = CustomScoreboardBridge.defaultX
        return if (d != 0) d.toFloat() else screenWidth - width - 1f
    }

    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int): Float {
        val d = CustomScoreboardBridge.defaultY
        return if (d != 0) d.toFloat() else screenHeight / 2f - naturalHeight / 2f
    }

    override fun measuredWidth(): Float {
        val w = CustomScoreboardBridge.contentWidth
        return if (w > 0) w.toFloat() else naturalWidth
    }

    override fun measuredHeight(): Float {
        val h = CustomScoreboardBridge.contentHeight
        return if (h > 0) h.toFloat() else naturalHeight
    }
}

class TabListHud : VanillaHud("vanillahud/tab.json", "Tab List", Category.INFO) {
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

    @Dropdown(title = "Text Type", options = ["No Shadow", "Shadow"])
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

    private fun players(): List<PlayerInfo> = try {
        val real = mc.connection?.listedOnlinePlayers?.take(playerLimit) ?: emptyList()
        val useDemo = isEditing && (real.isEmpty() || mc.hasSingleplayerServer())
        if (useDemo) TabListManager.devInfo.take(playerLimit) else real
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

    private fun tabText(editing: String, live: () -> Component?, show: Boolean): Component? {
        if (!show) return null
        val real = try { live() } catch (_: Throwable) { null }
        if (real != null) return real
        return if (isEditing) Component.literal(editing) else null
    }

    private fun size(): Pair<Float, Float>? {
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

        val cellWidth = 9 + maxName + 13
        val slotWidth = minOf(columns * cellWidth, screenWidth - 50) / columns
        var width = slotWidth * columns + (columns - 1) * 5
        var height = rows * line

        val overlay: IPlayerTabOverlay? = try {
            //? if >=26.2 {
            mc.gui.hud.tabList as IPlayerTabOverlay
            //?} else {
            /*mc.gui.tabList as IPlayerTabOverlay
            *///?}
        } catch (_: Throwable) {
            null
        }
        val header = tabText("Tab List", { overlay?.header }, showHeader)
        val footer = tabText("VanillaHUD", { overlay?.footer }, showFooter)
        if (header != null) {
            val lines = font.split(header, screenWidth - 50)
            for (l in lines) width = maxOf(width, font.width(l))
            height += lines.size * line + 1
        }
        if (footer != null) {
            val lines = font.split(footer, screenWidth - 50)
            for (l in lines) width = maxOf(width, font.width(l))
            height += lines.size * line + 1
        }

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
}

class TitleHud : VanillaHud("vanillahud/title.json", "Title & Subtitle", Category.INFO) {
    @Switch(
        title = "Auto Scale",
        description = "Shrink the title and subtitle so they always fit within the screen width."
    )
    var autoTitleScale = false

    override val naturalWidth get() = 120f
    override val naturalHeight get() = 68f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - width / 2f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight / 2f - 40f

    override fun measuredWidth(): Float {
        val gui = if (isEditing) null else hudAccessor
        val title = gui?.title?.string ?: "Title"
        val subtitle = gui?.subtitle?.string ?: "Subtitle"
        return try {
            maxOf(mc.font.width(title) * 4, mc.font.width(subtitle) * 2).toFloat()
        } catch (_: Throwable) {
            naturalWidth
        }
    }

    override fun measuredHeight(): Float {
        val gui = if (isEditing) null else hudAccessor
        val subtitle = gui?.subtitle?.string ?: "Subtitle"
        return try {
            val line = mc.font.lineHeight
            if (subtitle.isNotBlank()) (line * 4 + 14 + line * 2).toFloat() else (line * 4).toFloat()
        } catch (_: Throwable) {
            naturalHeight
        }
    }
}

class StatusEffectsHud : VanillaHud("vanillahud/statuseffects.json", "Status Effects", Category.PLAYER) {
    override val naturalWidth get() = 50f
    override val naturalHeight get() = 50f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth - width
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = 1f

    private class Counts(val beneficial: Int, val harmful: Int)

    private fun counts(): Counts? {
        val real = mc.player?.activeEffects ?: emptyList()
        val effects = if (real.isEmpty() && isEditing) DemoData.demoEffects() else real
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

class SubtitlesHud : VanillaHud("vanillahud/subtitles.json", "Closed Captions", Category.INFO) {
    @Color(title = "Text Color")
    var captionTextColor = PolyColor(0xFFFFFFFF.toInt())

    @Color(
        title = "Background Color",
        description = "Overrides the vanilla text background opacity setting for closed captions."
    )
    var captionBgColor = PolyColor(0xCC000000.toInt())

    val captionBgArgb: Int get() = captionBgColor.argb

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

    private fun texts(): List<Component> {
        val overlay = hudAccessor?.subtitleOverlay as? ISubtitleOverlay ?: return emptyList()
        return overlay.audibleSubtitles.mapNotNull { (it as? ISubtitle)?.subtitleText }
    }

    override fun measuredWidth(): Float = try {
        val texts = texts()
        if (texts.isEmpty()) naturalWidth else {
            val font = mc.font
            val row = texts.maxOf { font.width(it) } +
                font.width("<") + font.width(" ") + font.width(">") + font.width(" ")
            (row / 2 * 2 + 2).toFloat()
        }
    } catch (_: Throwable) {
        naturalWidth
    }

    override fun measuredHeight(): Float = try {
        val lines = texts().size
        if (lines == 0) naturalHeight else (lines * SUBTITLE_ROW).toFloat()
    } catch (_: Throwable) {
        naturalHeight
    }

    private companion object {
        const val SUBTITLE_ROW = 10
    }
}

object Huds {
    val hotbar = HotbarHud()
    val health = HealthHud()
    val armor = ArmorHud()
    val hunger = HungerHud()
    val air = AirHud()
    val mountHealth = MountHealthHud()
    val experienceBar = ExperienceBarHud()
    val experienceLevel = ExperienceLevelHud()
    val actionBar = ActionBarHud()
    val heldItemTooltip = HeldItemTooltipHud()
    val title = TitleHud()
    val scoreboard = ScoreboardHud()

    val customScoreboard = CustomScoreboardHud()

    val tabList = TabListHud()
    val bossBar = BossBarHud()
    val statusEffects = StatusEffectsHud()
    val subtitles = SubtitlesHud()

    val all: Array<VanillaHud>
        get() = arrayOf(
            hotbar, health, armor, hunger, air, mountHealth,
            experienceBar, experienceLevel, actionBar, heldItemTooltip,
            title, scoreboard, tabList, bossBar, statusEffects, subtitles,
        )
}
