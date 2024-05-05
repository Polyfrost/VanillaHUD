package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.data.*;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import org.polyfrost.vanillahud.config.HudConfig;

public class Experience extends HudConfig {

    @HUD(
            name = "Experience"
    )
    public static ExperienceHud hud = new ExperienceHud();

    public Experience() {
        super("Experience", "vanilla-hud/experience.json");
    }

    public static class ExperienceHud extends Hud {

        public ExperienceHud() {
            super(true, 1920 / 2f - 182 / 2f, 1080 - 29);
        }

        @Slider(
                name = "Level Text Height",
                min = -10f, max = 10f
        )
        public static float expHeight = 4;

        public boolean shouldRender() {
            return isEnabled() && shouldShow();
        }

        @Override
        protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
        }

        @Override
        protected float getWidth(float scale, boolean example) {
            return 182 * scale;
        }

        @Override
        protected float getHeight(float scale, boolean example) {
            return 5 * scale;
        }

    }

}
