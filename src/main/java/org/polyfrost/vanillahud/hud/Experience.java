package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.data.*;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;

public class Experience extends Config {

    @HUD(
            name = "Experience"
    )
    public static ExperienceHud hud = new ExperienceHud();

    public Experience() {
        super(new Mod("Experience", ModType.HUD), "vanilla-hud/experience.json");
        initialize();
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
