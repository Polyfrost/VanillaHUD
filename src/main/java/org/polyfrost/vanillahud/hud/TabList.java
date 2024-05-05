package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.*;
import cc.polyfrost.oneconfig.hud.BasicHud;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.polyfrost.vanillahud.config.HudConfig;
import org.polyfrost.vanillahud.utils.*;

public class TabList extends HudConfig {

    @HUD(
            name = "TabList"
    )
    public static TabHud hud = new TabHud();

    @Exclude
    public static int width, height;

    @Exclude
    public static boolean isGuiIngame = false;

    @Exclude
    public static EaseOutQuart animation = new EaseOutQuart(0, 0, 0, false);

    public TabList() {
        super("TabList", "vanilla-hud/tab.json");
        TabListManager.asyncFetchFallbackList();
        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onScreenOpened(GuiScreenEvent.InitGuiEvent.Post event) {
                if (HudCore.editing) {
                    TabListManager.asyncUpdateList();
                }
            }
        });
    }

    public static class TabHud extends BasicHud {

        public TabHud() {
            super(true, 1920 / 2f, 10);
        }

        @Switch(
                name = "Animations"
        )
        public static boolean tabAnimation = false;

        @Slider(
                name = "Duration",
                min = 50f, max = 1000f
        )
        public static float tabDuration = 400f;

        @Slider(
                name = "Tab Player Limit",
                description = "Change how many players can display on tab.",
                min = 10, max = 120
        )
        public static int tabPlayerLimit = 80;

        @DualOption(
                name = "Mode",
                left = "Held",
                right = "Toggle"
        )
        public static boolean displayMode = false;

        @Dropdown(
                name = "Text Type",
                options = {"No Shadow", "Shadow", "Full Shadow"}
        )
        public static int textType = 1;

        @Switch(
                name = "Show Header"
        )
        public static boolean showHeader = true;

        @Switch(
                name = "Show Footer"
        )
        public static boolean showFooter = true;

        @Switch(
                name = "Show Self At Top"
        )
        public static boolean selfAtTop = true;

        @Switch(
                name = "Show Player's Head"
        )
        public static boolean showHead = true;

        @Switch(
                name = "Show Player's Ping"
        )
        public static boolean showPing = true;

        @Switch(
                name = "Use Number Ping"
        )
        public static boolean numberPing = true;

        @DualOption(
                name = "Ping Text",
                left = "Small",
                right = "Full"
        )
        public static boolean pingType = false;

        @Switch(
                name = "Hide False Ping",
                description = "Hides falsified ping numbers such as a ping of 0 or 1 when on Hypixel"
        )
        public static boolean hideFalsePing = true;

        @Color(
                name = "Ping Between 0 and 75"
        )
        public static OneColor pingLevelOne = new OneColor("55FF55FF");

        @Color(
                name = "Ping Between 75 and 145"
        )
        public static OneColor pingLevelTwo = new OneColor("00AA00FF");

        @Color(
                name = "Ping Between 145 and 200"
        )
        public static OneColor pingLevelThree = new OneColor("FFFF55FF");

        @Color(
                name = "Ping Between 200 and 300"
        )
        public static OneColor pingLevelFour = new OneColor("FFAA00FF");

        @Color(
                name = "Ping Between 300 and 400"
        )
        public static OneColor pingLevelFive = new OneColor("FF5555FF");

        @Color(
                name = "Ping Above 400"
        )
        public static OneColor pingLevelSix = new OneColor("AA0000FF");

        @Color(
                name = "Tab Widget Color"
        )
        public static OneColor tabWidgetColor = new OneColor(553648127);

        public static int getTabPlayerLimit() {
            return MathHelper.clamp_int(tabPlayerLimit, 10, 120);
        }

        @Override
        protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
        }

        @Override
        protected void drawBackground(float x, float y, float width, float height, float scale) {
            super.drawBackground(x, y, width, height, scale);
        }

        public boolean shouldRender() {
            return isEnabled() && shouldShow() && (isGuiIngame ^ isCachingIgnored());
        }

        @Exclude
        private static boolean lastToggled;

        public void drawBG(boolean toggled) {
            if (toggled != lastToggled) {
                lastToggled = toggled;

                if (tabAnimation) {
                    if (toggled) {
                        animation = new EaseOutQuart(tabDuration, 0f, position.getHeight(), false);
                    } else {
                        animation = new EaseOutQuart(tabDuration, position.getHeight(), 0f, false);
                    }
                }
            }

            if (animation.isFinished()) {
                if (toggled) {
                    animation = new EaseOutQuart(0, 0, position.getHeight(), false);
                } else {
                    return;
                }
            } else {
                if (animation.getEnd() != 0f && toggled && animation.getEnd() != position.getHeight()) {
                    animation = new EaseOutQuart(tabDuration - Minecraft.getSystemTime() + animation.startTime, animation.get(), position.getHeight(), false);
                }
            }

            if (!background || !shouldRender()) return;
            this.drawBackground(position.getX(), position.getY(), position.getWidth(), animation.get(), scale);
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
