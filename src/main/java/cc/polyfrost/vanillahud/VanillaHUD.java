package cc.polyfrost.vanillahud;

import cc.polyfrost.oneconfig.utils.Notifications;
import cc.polyfrost.vanillahud.hud.ActionBar;
import cc.polyfrost.vanillahud.hud.BossBar;
import cc.polyfrost.vanillahud.hud.Hotbar;
import cc.polyfrost.vanillahud.hud.Scoreboard;
import net.minecraftforge.fml.common.Loader;

@net.minecraftforge.fml.common.Mod(modid = VanillaHUD.MODID, name = VanillaHUD.NAME, version = VanillaHUD.VERSION)
public class VanillaHUD {
    public static final String MODID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";

    public static Hotbar hotbar;

    @net.minecraftforge.fml.common.Mod.EventHandler
    public void onFMLInitialization(net.minecraftforge.fml.common.event.FMLInitializationEvent event) {
        new BossBar();
        new Scoreboard();
        new ActionBar();
        hotbar = new Hotbar();
    }

    @net.minecraftforge.fml.common.Mod.EventHandler
    public void onPostInit(net.minecraftforge.fml.common.event.FMLPostInitializationEvent event) {
        if (Loader.isModLoaded("bossbar_customizer")) {
            Notifications.INSTANCE.send("VanillaHUD", "Bossbar Customizer has been replaced by VanillaHUD and thus can be removed (they will also not work with each other).");
        }

        if (Loader.isModLoaded("sidebarmod")) {
            Notifications.INSTANCE.send("VanillaHUD", "Sidebar Mod Revamp has been replaced by VanillaHUD and thus can be removed (they will also not work with each other).");
        }
    }
}
