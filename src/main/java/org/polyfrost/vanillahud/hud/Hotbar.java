package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.data.*;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.GuiIngameForge;
import org.polyfrost.vanillahud.config.HudConfig;
import org.polyfrost.vanillahud.mixin.*;

public class Hotbar extends HudConfig {

    @HUD(
            name = "HotBar"
    )
    public static HotBarHud hud = new HotBarHud();

    public Hotbar() {
        super("HotBar", "vanilla-hud/hotbar.json");
        initialize();
    }

    public static class HotBarHud extends Hud {

        public HotBarHud() {
            super(true, 1920 / 2f - 182 / 2f, 1080 - 22f);
        }

        @Exclude
        private static final Minecraft mc = Minecraft.getMinecraft();

        @DualOption(
                name = "Mode",
                left = "Vertical",
                right = "Horizontal"
        )
        public static boolean hotbarMode = true;

        @Override
        public boolean isEnabled() {
            boolean isEnable = super.isEnabled();
            GuiIngameForge.renderHotbar = isEnable && shouldShow();
            return isEnable;
        }

        @Override
        protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
            GuiIngameForge.renderHotbar = shouldShow();
        }

        @Override
        protected float getWidth(float scale, boolean example) {
            return (hotbarMode ? 182 : 22) * scale;
        }

        @Override
        protected float getHeight(float scale, boolean example) {
            return (hotbarMode ? 22 : 182) * scale;
        }

        @Override
        protected boolean shouldShow() {
            GuiSpectatorAccessor accessor = (GuiSpectatorAccessor) mc.ingameGUI.getSpectatorGui();
            return super.shouldShow() && (!mc.thePlayer.isSpectator()|| accessor.alpha() > 0f);
        }
    }
}
