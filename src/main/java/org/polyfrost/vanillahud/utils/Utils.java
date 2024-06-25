package org.polyfrost.vanillahud.utils;

import cc.polyfrost.oneconfig.events.event.Stage;
import cc.polyfrost.oneconfig.events.event.TickEvent;
import cc.polyfrost.oneconfig.libs.eventbus.Subscribe;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.StringUtils;

public class Utils {

    public static boolean inSkyblock = false;
    private int tickAmount = 0;

    @Subscribe
    private void onTick(TickEvent event) {
        if (event.stage == Stage.START) {
            tickAmount++;
            if (tickAmount % 20 == 0) {
                if (UMinecraft.getPlayer() != null) {
                    Minecraft mc = UMinecraft.getMinecraft();
                    if (mc.theWorld != null && !mc.isSingleplayer()) {
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
}
