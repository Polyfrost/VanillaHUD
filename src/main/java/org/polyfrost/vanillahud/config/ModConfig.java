package org.polyfrost.vanillahud.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.SubConfig;
import cc.polyfrost.oneconfig.config.data.*;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.hud.*;

public class ModConfig extends Config {

    public ModConfig() {
        super(new Mod(VanillaHUD.NAME, ModType.HUD, "/vanillahud_dark.svg"), VanillaHUD.MODID + ".json");
        initialize();
    }

    @SubConfig
    public static ActionBar actionBar = new ActionBar();
    @SubConfig
    public static Air air = new Air();
    @SubConfig
    public static Armor armor = new Armor();
    @SubConfig
    public static BossBar bossBar = new BossBar();
    @SubConfig
    public static Experience experience = new Experience();
    @SubConfig
    public static Health health = new Health();
    @SubConfig
    public static Hotbar hotBar = new Hotbar();
    @SubConfig
    public static Hunger hunger = new Hunger();
    @SubConfig
    public static ItemTooltip itemTooltip = new ItemTooltip();
    @SubConfig
    public static Scoreboard scoreboard = new Scoreboard();
    @SubConfig
    public static ScrollableTooltip scrollableTooltip = new ScrollableTooltip();
    @SubConfig
    public static TabList tab = new TabList();
    @SubConfig
    public static Title title = new Title();
    public static boolean hasMigratedPatcher = false;

}