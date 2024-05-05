package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.*;
import org.polyfrost.vanillahud.config.HudConfig;
import org.polyfrost.vanillahud.hooks.TooltipHook;

public class ScrollableTooltip extends HudConfig {

    @Switch(
            name = "Start at the Top of Tooltips",
            description = "Changes tooltips to always show the top."
    )
    public static boolean startAtTop = false;

    public ScrollableTooltip() {
        super("Scrollable Tooltip", "vanilla-hud/scrollable-tooltip.json");
        addListener("startAtTop", TooltipHook::resetScrolling);
    }

}