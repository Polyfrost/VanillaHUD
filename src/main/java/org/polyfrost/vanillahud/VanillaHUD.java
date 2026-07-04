package org.polyfrost.vanillahud;

import net.fabricmc.api.ClientModInitializer;
import org.polyfrost.oneconfig.api.notifications.v1.Notifications;
import org.polyfrost.vanillahud.compat.CompatMods;
import org.polyfrost.vanillahud.config.ModConfig;

public final class VanillaHUD implements ClientModInitializer {
    public static final String MODID = "vanillahud";
    public static final String NAME = "VanillaHUD";
    public static final String VERSION = "2.2.12";

    @Override
    public void onInitializeClient() {
        ModConfig.INSTANCE.preload();
        CompatMods.refresh();

        if (CompatMods.isLoaded("bossbar_customizer")) {
            Notifications.info(NAME, "Bossbar Customizer overlaps with VanillaHUD's boss bar controls.");
        }
        if (CompatMods.isLoaded("sidebarmod")) {
            Notifications.info(NAME, "Sidebar Mod overlaps with VanillaHUD's scoreboard controls.");
        }
    }

    public static boolean isApec() {
        return CompatMods.APEC;
    }

    public static boolean isCompactTab() {
        return CompatMods.SKYBLOCK_ADDONS || CompatMods.SKYHANNI || CompatMods.ANIMATIUM || CompatMods.HYTILS_REBORN;
    }

    public static boolean isForceDisableCompactTab() {
        return isCompactTab();
    }

    public static boolean isSkyHanniScoreboard() {
        return CompatMods.SKYHANNI;
    }

    public static boolean isLegacyTablist() {
        return CompatMods.ANIMATIUM;
    }
}
