package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;

public class Tablist extends Config {

    @HUD(
            name = "Tablist"
    )
    public static TabHud hud = new TabHud();

    @Exclude
    public static int width;

    @Exclude
    public static int height;

    public Tablist() {
        super(new Mod("Tablist", ModType.HUD), "vanilla-hud/tab.json");
        initialize();
    }

    public static class TabHud extends Hud {

        public TabHud() {
            super(true);
        }

        @Override
        protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
        }

        @Override
        protected float getWidth(float scale, boolean example) {
            return width * scale;
        }

        @Override
        protected float getHeight(float scale, boolean example) {
            return height * scale;
        }
    }

}
