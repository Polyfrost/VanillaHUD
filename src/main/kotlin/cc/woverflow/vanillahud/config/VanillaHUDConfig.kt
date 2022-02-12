package cc.woverflow.vanillahud.config

import cc.woverflow.vanillahud.ActionBar
import cc.woverflow.vanillahud.BossBar
import cc.woverflow.vanillahud.Scoreboard
import cc.woverflow.vanillahud.VanillaHUD
import gg.essential.api.EssentialAPI
import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.data.Property
import gg.essential.vigilance.data.PropertyType
import java.awt.Color
import java.io.File

object VanillaHUDConfig : Vigilant(File(VanillaHUD.modDir, "${VanillaHUD.ID}.toml"), VanillaHUD.NAME) {

    @Property(
        type = PropertyType.SWITCH,
        name = "Action Bar",
        description = "Toggle the action bar.",
        category = "Action Bar"
    )
    var actionBar = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Action Bar Shadow",
        description = "Toggle the action bar shadow.",
        category = "Action Bar"
    )
    var actionBarShadow = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Action Bar Background",
        description = "Add a background to the action bar.",
        category = "Action Bar"
    )
    var actionBarBackground = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Round Background",
        description = "Make the background of the action bar round.",
        category = "Action Bar"
    )
    var actionBarRoundBackground = false

    @Property(
        type = PropertyType.NUMBER,
        name = "Rounded Background Radius Amount",
        description = "Change the radius of the rounded background.",
        category = "Action Bar",
        min = 0,
        max = 10
    )
    var actionBarRadius = 6

    @Property(
        type = PropertyType.NUMBER,
        name = "Background Padding Amount",
        description = "Change the amount of padding added to the background.",
        category = "Action Bar",
        min = 0,
        max = 10
    )
    var actionBarPadding = 2

    @Property(
        type = PropertyType.COLOR,
        name = "Background Color",
        description = "Change the text color for the HUD.",
        category = "Action Bar"
    )
    var actionBarBackgroundColor: Color = Color(0, 0, 0, 128)

    @Property(
        type = PropertyType.PERCENT_SLIDER,
        name = "Action Bar Scale",
        description = "Set the scale for the action bar.",
        category = "Action Bar"
    )
    var actionBarScale = 1.0F

    @Property(
        type = PropertyType.BUTTON,
        name = "Action Bar Editor",
        description = "Change the position of the action bar.",
        category = "Action Bar"
    )
    fun openActionBarGui() {
        EssentialAPI.getGuiUtil().openScreen(ActionBar.ActionBarGui())
    }

    @Property(
        type = PropertyType.NUMBER,
        name = "Action Bar X Offset",
        description = "X",
        category = "Action Bar",
        hidden = true
    )
    var actionBarX: Int = 0

    @Property(
        type = PropertyType.NUMBER,
        name = "Action Bar Y Offset",
        description = "Y",
        category = "Action Bar",
        hidden = true
    )
    var actionBarY: Int = 0

    @Property(
        type = PropertyType.SWITCH,
        name = "Toggle Bossbar",
        description = "Toggle the bossbar.",
        category = "Bossbar"
    )
    var bossBar = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Toggle Text",
        description = "Toggle the text for the bossbar.",
        category = "Bossbar"
    )
    var bossBarText = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Toggle Shadow",
        description = "Toggle the text shadow for the bossbar.",
        category = "Bossbar"
    )
    var bossBarShadow = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Toggle Bar",
        description = "Toggle the bar for the bossbar.",
        category = "Bossbar"
    )
    var bossBarBar = true

    @Property(
        type = PropertyType.PERCENT_SLIDER,
        name = "Bossbar Scale",
        description = "Set the scale for the bossbar.",
        category = "Bossbar"
    )
    var bossbarScale = 1.0F

    @Property(
        type = PropertyType.BUTTON,
        name = "Bossbar Editor",
        description = "Change the position of the bossbar.",
        category = "Bossbar"
    )
    fun openBossHealthGui() {
       EssentialAPI.getGuiUtil().openScreen(BossBar.BossBarGui())
    }

    @Property(
        type = PropertyType.NUMBER,
        name = "Bossbar X Offset",
        description = "X",
        category = "Bossbar",
        hidden = true
    )
    var bossBarX: Int = 0

    @Property(
        type = PropertyType.NUMBER,
        name = "Bossbar Y Offset",
        description = "Y",
        category = "Bossbar",
        hidden = true
    )
    var bossBarY: Int = 0

    @Property(
        type = PropertyType.SWITCH,
        name = "Toggle Scoreboard",
        category = "Scoreboard",
        description = "Toggle the scoreboard from rendering."
    )
    var scoreboard = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Toggle Scoreboard Text Shadow",
        category = "Scoreboard",
        description = "Toggle the scoreboard text's shadow from rendering."
    )
    var scoreboardTextShadow = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Toggle Score Points",
        category = "Scoreboard",
        description = "Toggle the scoreboard score points (aka red numbers) from rendering."
    )
    var scoreboardScorePoints = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Toggle Background",
        category = "Scoreboard",
        description = "Toggle the background from rendering."
    )
    var scoreboardBackground = true

    @Property(
        type = PropertyType.COLOR,
        name = "Scoreboard Background Color",
        category = "Scoreboard",
        description = "Change the text color for the scoreboard."
    )
    var scoreboardBackgroundColor: Color = Color(1342177280, true)

    @Property(
        type = PropertyType.COLOR,
        name = "Scoreboard Title Background Color",
        category = "Scoreboard",
        description = "Change the text color for the scoreboard."
    )
    var scoreboardTitleBackgroundColor: Color = Color(1610612736, true)

    @Property(
        type = PropertyType.SWITCH,
        name = "Toggle Scoreboard Border",
        category = "Scoreboard",
        description = "Toggle the background border from rendering."
    )
    var scoreboardBackgroundBorder = false

    @Property(
        type = PropertyType.COLOR,
        name = "Scoreboard Border Color",
        category = "Scoreboard",
        description = "Change the text color for the scoreboard border."
    )
    var scoreboardBorderColor: Color = Color.BLACK

    @Property(
        type = PropertyType.NUMBER,
        name = "Scoreboard Border Width Amount",
        description = "Change the amount of width in the scoreboard border.",
        category = "Scoreboard",
        min = 0,
        max = 10
    )
    var scoreboardBorderWidth = 2

    @Property(
        type = PropertyType.PERCENT_SLIDER,
        name = "Scoreboard Scale",
        description = "Set the scale for the scoreboard.",
        category = "Scoreboard"
    )
    var scoreboardScale = 1.0F

    @Property(
        type = PropertyType.NUMBER,
        name = "Scoreboard X Offset",
        description = "X",
        category = "Scoreboard",
        hidden = true
    )
    var scoreboardX: Int = 0

    @Property(
        type = PropertyType.NUMBER,
        name = "Scoreboard Y Offset",
        description = "Y",
        category = "Scoreboard",
        hidden = true
    )
    var scoreboardY: Int = 0

    @Property(
        type = PropertyType.BUTTON,
        name = "Scoreboard Editor",
        description = "Change the position of the scoreboard.",
        category = "Scoreboard"
    )
    fun openScoreboardGui() {
        EssentialAPI.getGuiUtil().openScreen(Scoreboard.ScoreboardGui())
    }


    init {
        initialize()
    }
}