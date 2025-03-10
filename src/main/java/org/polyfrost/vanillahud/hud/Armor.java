package org.polyfrost.vanillahud.hud;

import org.polyfrost.oneconfig.api.config.v1.annotations.*;
import org.polyfrost.oneconfig.api.config.v1.data.*;
import org.polyfrost.vanillahud.config.HudConfig;

public class Armor extends HudConfig {

    @HUD(
            name = "Armor"
    )
    public static ArmorHud hud = new ArmorHud();

    public Armor() {
        super("Armor", "vanilla-hud/armor.json");
        initialize();
    }

    public static class ArmorHud extends HudBar {

        @Checkbox(name = "Link with health")
        public static boolean healthLink = true;

        @Checkbox(name = "Link with mount health")
        public static boolean mountLink = false;

        public ArmorHud() {
            super(true, 1920 / 2f - 182 / 2f, 1080 - 49, false);
            showInDebug = true;
        }
    }

}
