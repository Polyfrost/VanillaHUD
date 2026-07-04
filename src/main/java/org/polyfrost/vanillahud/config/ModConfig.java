package org.polyfrost.vanillahud.config;

import org.polyfrost.compose.render.PolyColor;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.annotations.Checkbox;
import org.polyfrost.oneconfig.api.config.v1.annotations.Color;
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown;
import org.polyfrost.oneconfig.api.config.v1.annotations.Include;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.vanillahud.VanillaHUD;

public final class ModConfig extends Config {
    public static final ModConfig INSTANCE = new ModConfig();

    @Include
    public static HudElement hotbar = new HudElement(true, 869.0F, 1058.0F, false);

    @Include
    public static HudElement health = new HudElement(true, 869.0F, 1041.0F, false);

    @Include
    public static HudElement armor = new HudElement(true, 869.0F, 1031.0F, false);

    @Include
    public static HudElement hunger = new HudElement(true, 970.0F, 1041.0F, true);

    @Include
    public static HudElement air = new HudElement(true, 970.0F, 1031.0F, true);

    @Include
    public static HudElement mountHealth = new HudElement(false, 970.0F, 1021.0F, true);

    @Include
    public static HudElement experience = new HudElement(true, 869.0F, 1051.0F, false);

    @Include
    public static HudElement itemTooltip = new HudElement(true, 960.0F, 1043.0F, false);

    @Include
    public static HudElement actionBar = new HudElement(true, 960.0F, 1018.0F, false);

    @Include
    public static HudElement title = new HudElement(true, 960.0F, 540.0F, false);

    @Include
    public static HudElement scoreboard = new HudElement(true, 1919.0F, 540.0F, true);

    @Include
    public static HudElement bossBar = new HudElement(true, 960.0F, 12.0F, false);

    @Include
    public static HudElement tabList = new HudElement(true, 960.0F, 20.0F, false);

    @Switch(title = "Hotbar vertical mode", category = "Hotbar")
    public static boolean hotbarVertical = false;

    @Switch(title = "Disable hotbar selection animation", category = "Hotbar")
    public static boolean disableHotbarAnimation = false;

    @Switch(title = "Disable health animation", category = "Health")
    public static boolean disableHealthAnimation = false;

    @Switch(title = "Link armor to health height", category = "Armor")
    public static boolean armorHealthLink = true;

    @Switch(title = "Link hunger to health height", category = "Hunger")
    public static boolean hungerHealthLink = false;

    @Switch(title = "Link air to health height", category = "Air")
    public static boolean airHealthLink = false;

    @Slider(title = "Level text offset", min = -10.0F, max = 10.0F, step = 1.0F, category = "Experience")
    public static float experienceLevelOffset = 4.0F;

    @Dropdown(title = "Score points", options = {"Hide", "Hide consecutive", "Show"}, category = "Scoreboard")
    public static int scoreboardPoints = 1;

    @Switch(title = "Scoreboard title", category = "Scoreboard")
    public static boolean scoreboardTitle = true;

    @Color(title = "Score points color", category = "Scoreboard")
    public static PolyColor scorePointsColor = new PolyColor(0xFFFF5555);

    @Switch(title = "Tab toggle mode", category = "Tab List")
    public static boolean tabToggleMode = false;

    @Slider(title = "Tab player limit", min = 10.0F, max = 120.0F, step = 1.0F, category = "Tab List")
    public static int tabPlayerLimit = 80;

    @Checkbox(title = "Show tab header", category = "Tab List")
    public static boolean tabHeader = true;

    @Checkbox(title = "Show tab footer", category = "Tab List")
    public static boolean tabFooter = true;

    @Checkbox(title = "Show player heads", category = "Tab List")
    public static boolean tabHeads = true;

    @Checkbox(title = "Show ping", category = "Tab List")
    public static boolean tabPing = true;

    @Switch(title = "Scrollable tooltips", category = "Tooltips")
    public static boolean scrollableTooltips = true;

    @Switch(title = "Start large tooltips at top", category = "Tooltips")
    public static boolean tooltipStartAtTop = false;

    private ModConfig() {
        super("vanillahud.json", "/assets/vanillahud/vanillahud_dark.svg", VanillaHUD.NAME, Category.HUD);
        save();
    }
}
