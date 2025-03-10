package org.polyfrost.vanillahud.hud;

import org.polyfrost.oneconfig.api.config.v1.annotations.*;
import org.polyfrost.vanillahud.config.HudConfig;

public class Air extends HudConfig {

    @HUD(
            name = "Air"
    )
    public static AirHud hud = new AirHud();

    public Air() {
        super("Air", "vanilla-hud/air.json");
        initialize();
    }

    public static class AirHud extends HudBar {

        @Checkbox(name = "Link with health")
        public static boolean healthLink = false;

        @Checkbox(name = "Link with mount health")
        public static boolean mountLink = true;

        public AirHud() {
            super(true, 1920 / 2f + 182 / 2f - 81, 1080 - 49, true);
            showInDebug = true;
        }
    }

}
