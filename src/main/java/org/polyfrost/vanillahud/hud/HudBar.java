package org.polyfrost.vanillahud.hud;

import org.polyfrost.oneconfig.api.config.v1.annotations.*;
import org.polyfrost.oneconfig.hud.Hud;
import org.polyfrost.universal.UMatrixStack;

public class HudBar extends Hud {

    public HudBar(boolean enable, float x, float y, boolean alignment) {
        super(enable, x, y);
        showInDebug = true;
        this.alignment = alignment;
    }

    @DualOption(
            name = "Icon Alignment",
            left = "Left",
            right = "Right"
    )
    public boolean alignment;

    @Override
    protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
    }

    @Override
    protected float getWidth(float scale, boolean example) {
        return 81f * scale;
    }

    @Override
    protected float getHeight(float scale, boolean example) {
        return 9f * scale;
    }

    public boolean shouldRender() {
        return isEnabled() && shouldShow();
    }

}
