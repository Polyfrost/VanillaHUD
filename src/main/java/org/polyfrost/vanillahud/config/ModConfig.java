package org.polyfrost.vanillahud.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.config.data.*;
import cc.polyfrost.oneconfig.gui.pages.ModsPage;
import org.polyfrost.vanillahud.VanillaHUD;

public class ModConfig extends Config {

    public ModConfig() {
        super(new Mod(VanillaHUD.NAME, ModType.HUD, "/vanillahud_dark.svg"), VanillaHUD.MODID + ".json");
        initialize();
    }

    @Exclude
    public static ModsPage page = null;
}