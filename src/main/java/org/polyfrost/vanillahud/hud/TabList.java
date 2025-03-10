package org.polyfrost.vanillahud.hud;

import org.polyfrost.oneconfig.api.config.v1.annotations.*;
import org.polyfrost.polyui.color.PolyColor;
import org.polyfrost.oneconfig.api.config.v1.core.OneColor;
import org.polyfrost.oneconfig.api.config.v1.data.*;
import org.polyfrost.oneconfig.hud.BasicHud;
import org.polyfrost.oneconfig.internal.hud.HudCore;
import org.polyfrost.universal.UMatrixStack;
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
        initialize();
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
            super(true, 1920 / 2f, 10); // the default y is actually 20, see VanillaHUD main class
            ignoreCaching = true;
            showInDebug = true;
        }

        @Switch(
                name = "Animations"
        )
        public static boolean tabAnimation = true;

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
        public static boolean selfAtTop = false;

        @Switch(
                name = "Show Player's Head"
        )
        public static boolean showHead = true;

        @Switch(
                name = "Better Hat Layer"
        )
        public static boolean betterHatLayer = false;

        @Switch(
                name = "Show Player's Ping"
        )
        public static boolean showPing = true;

        @Switch(
                name = "Use Number Ping",
                size = 2
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
                name = "Tab Widget Color", size = 2
        )
        public static OneColor tabWidgetColor = new OneColor(553648127);

        @Info(
                text = "Tablist might goes over screen",
                type = InfoType.WARNING
        )
        private static Runnable info = () -> { }; //runnable so it wont be saved

        @Switch(
                name = "Fix TabList Entry Width"
        )
        public static boolean fixWidth = false;

        public static boolean updatedHeight;

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

        public void doAnimation(boolean toggled) {
            if (toggled != lastToggled) {
                lastToggled = toggled;
                if (tabAnimation) {
                    animation = new EaseOutQuart(tabDuration, position.getHeight(), 0f, toggled);
                }
            }

            if (animation.isFinished()) {
                if (toggled) {
                    animation = new EaseOutQuart(0, 0f, position.getHeight(), false);
                }
            } else {
                if (animation.getEnd() != 0f && toggled && animation.getEnd() != position.getHeight()) {
                    animation = new EaseOutQuart(tabDuration - Minecraft.getSystemTime() + animation.startTime, animation.get(), position.getHeight(), false);
                }
            }
        }

        public void drawBG() {
            if (!background || !shouldRender() || animation.get() == 0f || (animation.isFinished() && !lastToggled)) return;
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

        public OneColor getBackgroundColor() {
            return bgColor;
        }

        @Override
        protected void resetPosition() {
            super.resetPosition();
            TabList.hud.position.setY(20);
        }
    }

}
