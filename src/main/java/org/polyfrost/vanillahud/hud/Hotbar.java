package org.polyfrost.vanillahud.hud;

import org.polyfrost.oneconfig.api.config.v1.annotations.*;
import org.polyfrost.oneconfig.hud.Hud;
import org.polyfrost.universal.UMatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.GuiIngameForge;
import org.polyfrost.vanillahud.config.HudConfig;
import org.polyfrost.vanillahud.mixin.minecraft.GuiSpectatorAccessor;

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
            showInDebug = true;
        }

        @Exclude
        private static final Minecraft mc = Minecraft.getMinecraft();

        @DualOption(
                name = "Mode",
                left = "Vertical",
                right = "Horizontal"
        )
        public static boolean hotbarMode = true;

        @Switch(name = "Animation")
        public static boolean animation = true;

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
            if (mc.thePlayer == null || mc.ingameGUI == null) return false;
            GuiSpectatorAccessor accessor = (GuiSpectatorAccessor) mc.ingameGUI.getSpectatorGui();
            return super.shouldShow() && (!mc.thePlayer.isSpectator()|| accessor.alpha() > 0f);
        }
    }
}
