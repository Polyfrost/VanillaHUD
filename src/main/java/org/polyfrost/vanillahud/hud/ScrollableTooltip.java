package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import org.polyfrost.vanillahud.hooks.TooltipHook;

public class ScrollableTooltip extends Config {

    @Switch(
            name = "Start at the Top of Tooltips",
            description = "Changes tooltips to always show the top."
    )
    public static boolean startAtTop = false;

    public ScrollableTooltip() {
        super(new Mod("Scrollable Tooltip", ModType.HUD, "/vanillahud_dark.svg"), "vanilla-hud/scrollable-tooltip.json");
        initialize();
        addListener("startAtTop", TooltipHook::resetScrolling);
    }

}