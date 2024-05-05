package org.polyfrost.vanillahud;

import Apec.Components.Gui.GuiIngame.ApecGuiIngameForge;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.utils.Notifications;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import org.polyfrost.vanillahud.config.ModConfig;
import org.polyfrost.vanillahud.hud.*;
import org.polyfrost.vanillahud.utils.TabListManager;

import java.util.ArrayList;

@net.minecraftforge.fml.common.Mod(modid = VanillaHUD.MODID, name = VanillaHUD.NAME, version = VanillaHUD.VERSION)
public class VanillaHUD {
    public static final String MODID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";

    public static ModConfig modConfig;
    private static boolean apec = false;
    public static boolean isPatcher = false;
    public static ArrayList<Mod> mods = new ArrayList<>();

    @net.minecraftforge.fml.common.Mod.EventHandler
    public void onFMLInitialization(net.minecraftforge.fml.common.event.FMLInitializationEvent event) {
        modConfig = new ModConfig();
        TabListManager.asyncUpdateList();
    }

    @net.minecraftforge.fml.common.Mod.EventHandler
    public void onPostInit(net.minecraftforge.fml.common.event.FMLPostInitializationEvent event) {
        if (Loader.isModLoaded("bossbar_customizer")) {
            Notifications.INSTANCE.send("VanillaHUD", "Bossbar Customizer has been replaced by VanillaHUD and thus can be removed (they will also not work with each other).");
        }

        if (Loader.isModLoaded("sidebarmod")) {
            Notifications.INSTANCE.send("VanillaHUD", "Sidebar Mod Revamp has been replaced by VanillaHUD and thus can be removed (they will also not work with each other).");
        }
        apec = Loader.isModLoaded("apec");
        isPatcher = Loader.isModLoaded("patcher");
    }

    public static boolean isApec() {
        return apec && Minecraft.getMinecraft().ingameGUI instanceof ApecGuiIngameForge;
    }
}
