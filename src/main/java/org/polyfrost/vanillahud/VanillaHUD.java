package org.polyfrost.vanillahud;

import Apec.Components.Gui.GuiIngame.ApecGuiIngameForge;
import cc.polyfrost.oneconfig.utils.Notifications;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import org.polyfrost.vanillahud.hud.*;

@net.minecraftforge.fml.common.Mod(modid = VanillaHUD.MODID, name = VanillaHUD.NAME, version = VanillaHUD.VERSION)
public class VanillaHUD {
    public static final String MODID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";

    public static ActionBar actionBar;
    public static BossBar bossBar;
    public static Hotbar hotBar;
    public static ItemTooltip itemTooltip;
    public static Scoreboard scoreboard;
    public static Title title;
    private static boolean apec = false;

    @net.minecraftforge.fml.common.Mod.EventHandler
    public void onFMLInitialization(net.minecraftforge.fml.common.event.FMLInitializationEvent event) {
        actionBar = new ActionBar();
        bossBar = new BossBar();
        hotBar = new Hotbar();
        itemTooltip = new ItemTooltip();
        scoreboard = new Scoreboard();
        title = new Title();
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
    }

    public static boolean isApec() {
        return apec && Minecraft.getMinecraft().ingameGUI instanceof ApecGuiIngameForge;
    }
}
