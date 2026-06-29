package org.polyfrost.vanillahud.config

import org.polyfrost.oneconfig.api.config.v1.Config
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch

object VanillaHUDConfig : Config(
    "vanillahud.json",
    "/vanillahud_dark.svg",
    "VanillaHUD",
    Category.QOL,
) {
    @Switch(
        title = "Start at the Top of Tooltips",
        category = "Tooltips",
        description = "Changes scrollable tooltips to always show the top.",
    )
    var tooltipStartAtTop = false
}
