package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;

public class ScrollableTooltip extends Config {

    public ScrollableTooltip() {
        super(new Mod("Scrollable Tooltip", ModType.HUD, "/vanillahud_dark.svg"), "vanilla-hud/scrollable-tooltip.json");
        initialize();
    }

    @Switch(
            name = "Start at the Top of Tooltips",
            description = "Changes tooltips to always show the top."
    )
    public static boolean startAtTop = false;

}