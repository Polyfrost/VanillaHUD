package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.data.*;
import org.polyfrost.vanillahud.config.HudConfig;

public class Armor extends HudConfig {

    @HUD(
            name = "Armor"
    )
    public static ArmorHud hud = new ArmorHud();

    public Armor() {
        super("Armor", "vanilla-hud/armor.json");
    }

    public static class ArmorHud extends HudBar {

        @Checkbox(name = "Link with health")
        public static boolean healthLink = true;

        @Checkbox(name = "Link with mount health")
        public static boolean mountLink = false;

        public ArmorHud() {
            super(true, 1920 / 2f - 182 / 2f, 1080 - 49, false);
        }
    }

}
