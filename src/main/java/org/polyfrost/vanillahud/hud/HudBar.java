package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import net.minecraft.client.Minecraft;

public class HudBar extends Hud {

    public HudBar(boolean enable) {
        super(enable);
    }

    @Exclude
    public boolean isExample;

    @DualOption(
            name = "Icon Alignment",
            left = "Left",
            right = "Right"
    )
    public boolean alignment = true;

    @Exclude
    private static final Minecraft mc = Minecraft.getMinecraft();

    @Override
    protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
        isExample = example;
    }

    @Override
    protected float getWidth(float scale, boolean example) {
        return 81f * scale;
    }

    @Override
    protected float getHeight(float scale, boolean example) {
        return 9f * scale;
    }

}
