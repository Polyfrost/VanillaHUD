package org.polyfrost.vanillahud.compat;

import net.fabricmc.loader.api.FabricLoader;

public final class CompatMods {
    public static boolean APEC;
    public static boolean SKYBLOCK_ADDONS;
    public static boolean SKYHANNI;
    public static boolean APPLESKIN;
    public static boolean ANIMATIUM;
    public static boolean HYTILS_REBORN;

    private CompatMods() {
    }

    public static void refresh() {
        APEC = isLoaded("apec");
        SKYBLOCK_ADDONS = isLoaded("skyblockaddons");
        SKYHANNI = isLoaded("skyhanni");
        APPLESKIN = isLoaded("appleskin");
        ANIMATIUM = isLoaded("animatium");
        HYTILS_REBORN = isLoaded("hytils-reborn");
    }

    public static boolean isLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
