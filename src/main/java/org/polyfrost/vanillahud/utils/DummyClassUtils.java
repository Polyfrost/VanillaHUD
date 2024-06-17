package org.polyfrost.vanillahud.utils;

import club.sk1er.patcher.config.PatcherConfig;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.boss.BossStatus;
import net.minecraftforge.client.GuiIngameForge;
import org.polyfrost.hytils.handlers.lobby.npc.NPCHandler;
import org.polyfrost.hytils.handlers.lobby.tab.TabChanger;
import org.polyfrost.vanillahud.VanillaHUD;

import java.util.Collection;
import java.util.List;

public class DummyClassUtils {

    public static boolean willPatcherShiftDown() {
        return VanillaHUD.isPatcher && PatcherConfig.tabHeightAllow && BossStatus.bossName != null && BossStatus.statusBarTime > 0 && GuiIngameForge.renderBossHealth;
    }

    public static int patcherTabHeight() {
        return PatcherConfig.tabHeight;
    }

    public static Collection<NetworkPlayerInfo> hytilsHideTabNpcs(Collection<NetworkPlayerInfo> playerInfoCollection) {
        return VanillaHUD.isHytils ? NPCHandler.hideTabNpcs(playerInfoCollection) : playerInfoCollection;
    }

    public static List<String> hytilsModifyHeader(FontRenderer instance, String formattedHeader, int wrapWidth) {
        return VanillaHUD.isHytils ? TabChanger.modifyHeader(instance, formattedHeader, wrapWidth) : instance.listFormattedStringToWidth(formattedHeader, wrapWidth);
    }

    public static List<String> hytilsModifyFooter(FontRenderer instance, String formattedFooter, int wrapWidth) {
        return VanillaHUD.isHytils ? TabChanger.modifyFooter(instance, formattedFooter, wrapWidth) : instance.listFormattedStringToWidth(formattedFooter, wrapWidth);
    }

}
