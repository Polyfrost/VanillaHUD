package cc.woverflow.vanillahud.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.Button
import cc.polyfrost.oneconfig.config.annotations.Checkbox
import cc.polyfrost.oneconfig.config.annotations.Slider
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.config.migration.VigilanceMigrator
import cc.polyfrost.oneconfig.utils.dsl.openScreen
import cc.woverflow.vanillahud.ActionBar
import cc.woverflow.vanillahud.BossBar
import cc.woverflow.vanillahud.Scoreboard
import cc.woverflow.vanillahud.VanillaHUD
import java.awt.Color
import java.io.File

object VanillaHUDConfig : Config (
    Mod(VanillaHUD.NAME, ModType.HUD, VigilanceMigrator(File(VanillaHUD.modDir, "${VanillaHUD.ID}.toml").path)), VanillaHUD.ID + ".json") {

    @Switch(
        name = "Show Action Bar",
        category = "Action Bar"
    )
    var actionBar = true

    @Checkbox(
        name = "Enable Action Bar Shadow",
        category = "Action Bar"
    )
    var actionBarShadow = true

    @Checkbox(
        name = "Add Action Bar Background",
        category = "Action Bar"
    )
    var actionBarBackground = false

    @Checkbox(
        name = "Round Background",
        category = "Action Bar"
    )
    var actionBarRoundBackground = false

    @Slider(
        name = "Rounded Background Radius",
        category = "Action Bar",
        min = 0F,
        max = 12F
    )
    var actionBarRadius = 6

    @Slider(
        name = "Background Padding (px)",
        category = "Action Bar",
        min = 0F,
        max = 10F
    )
    var actionBarPadding = 2

    @cc.polyfrost.oneconfig.config.annotations.Color(
        name = "Background Color",
        category = "Action Bar"
    )
    var actionBarBackgroundColor: OneColor = OneColor(0, 0, 0, 128)

    @Slider(
        name = "Action Bar Scale",
        category = "Action Bar", min = 0F, max = 2F
    )
    var actionBarScale = 1.0F

    @Button(
        name = "Open Action Bar Editor",
        category = "Action Bar", text = "Open"
    )
    var openActionBarGui = Runnable {
        ActionBar.ActionBarGui().openScreen()
    }


    var actionBarX: Int = 0

    var actionBarY: Int = 0

    @Switch(
        name = "Toggle Bossbar",
        category = "Bossbar"
    )
    var bossBar = true

    @Checkbox(
        name = "Enable Text",
        category = "Bossbar"
    )
    var bossBarText = true

    @Checkbox(
        name = "Text Shadow",
        category = "Bossbar"
    )
    var bossBarShadow = true

    @Checkbox(
        name = "Enable Health Bar",
        category = "Bossbar"
    )
    var bossBarBar = true

    @Slider(
        name = "Bossbar Scale",
        category = "Bossbar", min = 0F, max = 2F
    )
    var bossbarScale = 1.0F

    @Button(
        name = "Open Bossbar Editor",
        category = "Bossbar", text = "Open"
    )
    var openBossbarEditor = Runnable {
        BossBar.BossBarGui().openScreen()
    }


    var bossBarX: Int = 0

    var bossBarY: Int = 0

    @Switch(
        name = "Toggle Scoreboard",
        category = "Scoreboard",
    )
    var scoreboard = true

    @Checkbox(
        name = "Enable Text Shadow",
        category = "Scoreboard"
    )
    var scoreboardTextShadow = true

    @Checkbox(
        name = "Enable Score Points (red numbers)",
        category = "Scoreboard",
    )
    var scoreboardScorePoints = false

    @Switch(
        name = "Enable Background",
        category = "Scoreboard",
    )
    var scoreboardBackground = true

    @cc.polyfrost.oneconfig.config.annotations.Color(
        name = "Scoreboard Background Color",
        category = "Scoreboard",
    )
    var scoreboardBackgroundColor: OneColor = OneColor(1342177280)

    @cc.polyfrost.oneconfig.config.annotations.Color(
        name = "Scoreboard Title Background Color",
        category = "Scoreboard",
    )
    var scoreboardTitleBackgroundColor: OneColor = OneColor(1610612736)

    @Checkbox(
        name = "Enable Scoreboard Border",
        category = "Scoreboard",
    )
    var scoreboardBackgroundBorder = false

    @cc.polyfrost.oneconfig.config.annotations.Color(
        name = "Scoreboard Border Color",
        category = "Scoreboard",
    )
    var scoreboardBorderColor: OneColor = OneColor(Color.BLACK)

    @Slider(
        name = "Scoreboard Border Width",
        category = "Scoreboard",
        min = 0F,
        max = 10F
    )
    var scoreboardBorderWidth = 2

    @Slider(
        name = "Scoreboard Scale",
        category = "Scoreboard", min = 0F, max = 2F
    )
    var scoreboardScale = 1.0F


    var scoreboardX: Int = 0

    var scoreboardY: Int = 0

    @Button(
        name = "Open Scoreboard Editor",
        category = "Scoreboard", text = "Open"
    )
    var openScoreboardGui = Runnable {
        Scoreboard.ScoreboardGui().openScreen()
    }

    init {
        initialize()
        listOf(
            "actionBarRoundBackground",
            "actionBarRadius",
            "actionBarPadding",
            "actionBarBackgroundColor"
        ).forEach { addDependency(it, "actionBarBackground") }
        addDependency("bossBarShadow", "bossBarText")
        listOf(
            "scoreboardBorderColor",
            "scoreboardBorderWidth"
        ).forEach { addDependency(it, "scoreboardBackgroundBorder") }
        listOf(
            "scoreboardBackgroundColor",
            "scoreboardTitleBackgroundColor"
        ).forEach { addDependency(it, "scoreboardBackground") }
    }
}