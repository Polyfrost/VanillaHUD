package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Checkbox;
import cc.polyfrost.oneconfig.config.annotations.DualOption;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;

public class Health extends Config {

    @HUD(
            name = "Health"
    )
    public static HealthHud hud = new HealthHud();

    public Health() {
        super(new Mod("Health", ModType.HUD), "vanilla-hud/health.json");
        initialize();
    }

    public static class HealthHud extends HudBar {

        @DualOption(
                name = "Mode",
                left = "Down",
                right = "Up"
        )
        public static boolean mode = true;

        @Checkbox(name = "Link with mount health")
        public static boolean mountLink = false;

        public HealthHud() {
            super(true);
        }
    }
}
