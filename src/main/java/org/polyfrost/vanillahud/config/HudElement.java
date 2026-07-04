package org.polyfrost.vanillahud.config;

import org.polyfrost.oneconfig.api.config.v1.annotations.Checkbox;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;

public class HudElement {
    private final float defaultX;
    private final float defaultY;

    @Switch(title = "Enabled")
    public boolean enabled;

    @Slider(title = "X", min = 0.0F, max = 1920.0F, step = 1.0F)
    public float x;

    @Slider(title = "Y", min = 0.0F, max = 1080.0F, step = 1.0F)
    public float y;

    @Slider(title = "Scale", min = 0.25F, max = 4.0F, step = 0.05F)
    public float scale = 1.0F;

    @Checkbox(title = "Right aligned")
    public boolean rightAligned;

    public HudElement(boolean enabled, float x, float y, boolean rightAligned) {
        this.enabled = enabled;
        this.x = x;
        this.y = y;
        this.defaultX = x;
        this.defaultY = y;
        this.rightAligned = rightAligned;
    }

    public float scaledX(int screenWidth) {
        return x * screenWidth / 1920.0F;
    }

    public float scaledY(int screenHeight) {
        return y * screenHeight / 1080.0F;
    }

    public float defaultScaledX(int screenWidth) {
        return defaultX * screenWidth / 1920.0F;
    }

    public float defaultScaledY(int screenHeight) {
        return defaultY * screenHeight / 1080.0F;
    }
}
