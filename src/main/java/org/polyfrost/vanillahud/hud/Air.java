package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Checkbox;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;

public class Air extends Config {

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
        public static boolean mountLink = false;

        public AirHud() {
            super(true);
        }
    }

}
