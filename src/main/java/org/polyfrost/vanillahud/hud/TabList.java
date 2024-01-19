package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Dropdown;
import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.hud.BasicHud;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
        TabListManager.asyncFetchFallbackList();
        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onScreenOpened(GuiScreenEvent.InitGuiEvent.Post event) {
                if (HudCore.editing) {
                    TabListManager.asyncUpdateList();
                }
            }
        });
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

        @Switch(
                name = "Show Player's Head"
        )
        public static boolean showHead = true;

        @Switch(
                name = "Show Player's Ping"
        )
        public static boolean showPing = true;

        @Override
        protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {}

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
