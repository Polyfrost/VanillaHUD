package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.data.*;

public class Armor extends Config {

    @HUD(
            name = "Armor"
    )
    public static ArmorHud hud = new ArmorHud();

    public Armor() {
        super(new Mod("Armor", ModType.HUD), "vanilla-hud/armor.json");
        initialize();
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
