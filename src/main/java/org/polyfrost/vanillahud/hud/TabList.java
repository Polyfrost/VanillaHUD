package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Dropdown;
import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.hud.BasicHud;
import cc.polyfrost.oneconfig.libs.universal.UChat;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import org.polyfrost.vanillahud.utils.TabListManager;

public class TabList extends Config {

    @HUD(
            name = "TabList"
    )
    public static TabHud hud = new TabHud();

    @Exclude
    public static int width;

    @Exclude
    public static int height;

    public TabList() {
        super(new Mod("TabList", ModType.HUD), "vanilla-hud/tab.json");
        initialize();
    }

    public static class TabHud extends BasicHud {

        public TabHud() {
            super(true, 1920 / 2f, 10);
        }

        @Dropdown(
                name = "Text Type",
                options = {"No Shadow", "Shadow", "Full Shadow"}
        )
        public static int textType = 1;

        @Exclude
        private static boolean isExample;

        @Override
        protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
            if (isExample != example) {
                if (example) {
                    TabListManager.launchUpdateDevList();
                }
                isExample = example;
            }
        }

        @Override
        protected void drawBackground(float x, float y, float width, float height, float scale) {
            super.drawBackground(x, y, width, height, scale);
        }

        public void drawBG() {
            if (!background) return;
            this.drawBackground(position.getX(), position.getY(), position.getWidth(), position.getHeight(), scale);
        }

        public float getPaddingY() {
            return paddingY * scale;
        }

        @Override
        protected boolean shouldDrawBackground() {
            return false;
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
