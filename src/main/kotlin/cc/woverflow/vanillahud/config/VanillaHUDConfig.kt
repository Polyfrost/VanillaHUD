package cc.woverflow.vanillahud.config

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