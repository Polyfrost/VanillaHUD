package org.polyfrost.vanillahud;

import Apec.Components.Gui.GuiIngame.ApecGuiIngameForge;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.events.event.Stage;
import cc.polyfrost.oneconfig.events.event.TickEvent;
import cc.polyfrost.oneconfig.libs.eventbus.Subscribe;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.utils.Notifications;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.Loader;
import org.polyfrost.vanillahud.config.ModConfig;
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
    private static boolean isSBA = false;
    private static boolean inSkyblock = false;
    public static ArrayList<Mod> mods = new ArrayList<>();
    private int tickAmount = 0;

    @net.minecraftforge.fml.common.Mod.EventHandler
    public void onFMLInitialization(net.minecraftforge.fml.common.event.FMLInitializationEvent event) {
        modConfig = new ModConfig();
        TabListManager.asyncUpdateList();
        EventManager.INSTANCE.register(this);
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
        isSBA = Loader.isModLoaded("skyblockaddons") || Loader.isModLoaded("sbaunofficial");
    }

    @Subscribe
    private void onTick(TickEvent event) {
        if (event.stage == Stage.START) {
            tickAmount++;
            if (tickAmount % 20 == 0) {
                if (UMinecraft.getPlayer() != null) {
                    Minecraft mc = Minecraft.getMinecraft();
                    if (mc != null && mc.theWorld != null && !mc.isSingleplayer()) {
                        ScoreObjective scoreboardObj = mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
                        if (scoreboardObj != null) {
                            String scObjName = cleanSB(scoreboardObj.getDisplayName());
                            if (scObjName.contains("SKYBLOCK")) {
                                inSkyblock = true;
                                return;
                            }
                        }
                    }
                    inSkyblock = false;
                }

                tickAmount = 0;
            }
        }
    }

    private static String cleanSB(String scoreboard) {
        char[] nvString = StringUtils.stripControlCodes(scoreboard).toCharArray();
        StringBuilder cleaned = new StringBuilder();

        for (char c : nvString) {
            if ((int) c > 20 && (int) c < 127) {
                cleaned.append(c);
            }
        }

        return cleaned.toString();
    }

    public static boolean isApec() {
        return apec && Minecraft.getMinecraft().ingameGUI instanceof ApecGuiIngameForge;
    }

    public static boolean inSBASkyblock() {
        return inSkyblock && isSBA;
    }
}
