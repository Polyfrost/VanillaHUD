package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.GuiIngameForge;
import org.polyfrost.vanillahud.mixin.*;

public class Hotbar extends Config {

    @HUD(
            name = "HotBar"
    )
    public static HotBarHud hud = new HotBarHud();

    public Hotbar() {
        super(new Mod("HotBar", ModType.HUD), "vanilla-hud/hotbar.json");
        initialize();
    }

    public static class HotBarHud extends Hud {

        public HotBarHud() {
            super(true);
        }

        @Exclude
        private static final Minecraft mc = Minecraft.getMinecraft();

        @Override
        public boolean isEnabled() {
            boolean isEnable = super.isEnabled();
            GuiIngameForge.renderHotbar = isEnable;
            return isEnable;
        }

        @Override
        protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
        }

        @Override
        protected float getWidth(float scale, boolean example) {
            return 182f * scale;
        }

        @Override
        protected float getHeight(float scale, boolean example) {
            return 22f * scale;
        }

        @Override
        protected boolean shouldShow() {
            GuiSpectatorAccessor accessor = (GuiSpectatorAccessor) mc.ingameGUI.getSpectatorGui();
            return super.shouldShow() && (!mc.thePlayer.isSpectator()|| accessor.alpha() > 0f);
        }
    }
}
