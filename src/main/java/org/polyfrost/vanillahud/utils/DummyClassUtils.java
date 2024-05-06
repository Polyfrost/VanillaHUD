package org.polyfrost.vanillahud.utils;

import club.sk1er.patcher.config.PatcherConfig;
import net.minecraft.entity.boss.BossStatus;
import net.minecraftforge.client.GuiIngameForge;
import org.polyfrost.vanillahud.VanillaHUD;

public class DummyClassUtils {

    public static boolean willPatcherShiftDown() {
        return VanillaHUD.isPatcher && PatcherConfig.tabHeightAllow && BossStatus.bossName != null && BossStatus.statusBarTime > 0 && GuiIngameForge.renderBossHealth;
    }

    public static int patcherTabHeight() {
        return PatcherConfig.tabHeight;
    }
}
