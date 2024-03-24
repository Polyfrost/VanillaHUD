package org.polyfrost.vanillahud.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.data.Mod;
import org.polyfrost.vanillahud.VanillaHUD;

public class HudConfig extends Config {

    public HudConfig(Mod mod, String file, boolean enabled) {
        super(mod, file, enabled);
    }

    public HudConfig(Mod mod, String file) {
        this(mod, file, true);
    }

    @Override
    public void initialize() {
        super.initialize();
        VanillaHUD.mods.add(mod);
    }
}