package org.polyfrost.vanillahud.hooks;

import cc.polyfrost.oneconfig.config.core.OneColor;
import org.polyfrost.vanillahud.hud.TabList;

public class TabHook {
    public static boolean gettingSize;
    public static boolean cancelRect;

    public static OneColor getColor(int ping) {
        return ping >= 400 ? TabList.TabHud.pingLevelSix
                : ping >= 300 ? TabList.TabHud.pingLevelFive
                : ping >= 200 ? TabList.TabHud.pingLevelFour
                : ping >= 145 ? TabList.TabHud.pingLevelThree
                : ping >= 75 ? TabList.TabHud.pingLevelTwo
                : TabList.TabHud.pingLevelOne;
    }
}