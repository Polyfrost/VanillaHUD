package org.polyfrost.vanillahud.hooks;

import cc.polyfrost.oneconfig.utils.MathUtils;
import net.minecraft.entity.boss.BossStatus;
import org.polyfrost.vanillahud.hud.BossBar;

public class BossStatusHook {
    private static float lerpedBossHealth;
    private static long percentSetTime;

    public static void onStatusSet() {
        lerpedBossHealth = getPercent();
        percentSetTime = System.currentTimeMillis();
    }

    public static float getPercent() {
        long l = System.currentTimeMillis() - percentSetTime;
        float f = MathUtils.clamp((float)l / BossBar.hud.lerpSpeed, 0.0f, 1.0f);
        return lerp(f, lerpedBossHealth, BossStatus.healthScale);
    }

    private static float lerp(float pct, float start, float end) {
        return start + pct * (end - start);
    }
}
