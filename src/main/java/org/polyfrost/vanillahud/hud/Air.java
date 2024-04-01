package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.data.*;
import org.polyfrost.vanillahud.config.HudConfig;

public class Air extends HudConfig {

    @HUD(
            name = "Air"
    )
    public static AirHud hud = new AirHud();

    public Air() {
        super(new Mod("Air", ModType.HUD), "vanilla-hud/air.json");
        initialize();
    }

    public static class AirHud extends HudBar {

        @Checkbox(name = "Link with health")
        public static boolean healthLink = false;

        @Checkbox(name = "Link with mount health")
        public static boolean mountLink = true;

        public AirHud() {
            super(true, 1920 / 2f + 182 / 2f - 81, 1080 - 49, true);
        }
    }

}
