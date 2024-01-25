package org.polyfrost.vanillahud.hooks;

import club.sk1er.patcher.config.PatcherConfig;
import org.polyfrost.vanillahud.VanillaHUD;

public class PatcherCompatHook {
    public static boolean isTabHeightAllow() {
        return VanillaHUD.isPatcher && PatcherConfig.tabHeightAllow;
    }

    public static int getTabHeight() {
        return VanillaHUD.isPatcher ? PatcherConfig.tabHeight : 0;
    }
}
