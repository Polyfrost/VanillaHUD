package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Checkbox;
import cc.polyfrost.oneconfig.config.annotations.DualOption;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;

public class Hunger extends Config {

    @HUD(
            name = "Hunger"
    )
    public static HungerHud hud = new HungerHud();

    @DualOption(
            name = "Mode",
            left = "Down",
            right = "Up",
            category = "Mount Health"
    )
    public static boolean mode = true;

    @HUD(
            name = "Mount Health",
            category = "Mount Health"
    )
    public static MountHud mountHud = new MountHud();

    public static HudBar getMountHud() {
        return Hunger.mountHud.isEnabled() ? Hunger.mountHud : Hunger.hud;
    }

    public Hunger() {
        super(new Mod("Hunger", ModType.HUD), "vanilla-hud/hunger.json");
        initialize();
    }

    public static class HungerHud extends HudBar {

        @Checkbox(name = "Link with health")
        public static boolean healthLink = false;

        @Checkbox(name = "Link with mount health")
        public static boolean mountLink = false;

        public HungerHud() {
            super(true);
        }
    }

    public static class MountHud extends HudBar {

        @Checkbox(name = "Link with health")
        public static boolean healthLink = false;

        public MountHud() {
            super(false);
        }
    }
}
