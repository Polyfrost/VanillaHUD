package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.categories.SpectatorDetails;
import net.minecraftforge.client.GuiIngameForge;
import org.polyfrost.vanillahud.mixin.*;

public class Hotbar extends Config {

    @HUD(
            name = "HotBar"
    )
    public static HotBarHud hud = new HotBarHud();

    public Hotbar() {
        super(new Mod("HotBar", ModType.HUD, "/vanillahud_dark.svg"), "hotbar.json");
        initialize();
    }

    public static class HotBarHud extends Hud {

        @Exclude
        private static final Minecraft mc = Minecraft.getMinecraft();

        @Override
        public boolean isEnabled() {
            boolean en = super.isEnabled();
            GuiIngameForge.renderHotbar = !en;
            return en;
        }

        @Override
        protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
            ScaledResolution scaleResolution = new ScaledResolution(mc);
            if (mc.playerController.isSpectator()) {
                GuiSpectatorAccessor accessor = (GuiSpectatorAccessor) mc.ingameGUI.getSpectatorGui();
                GuiAccessor guiAccessor = (GuiAccessor) mc.ingameGUI.getSpectatorGui();
                drawSpec(x, y, accessor.getField_175271_i(), accessor, guiAccessor, scaleResolution);
            } else {
                GuiIngameAccessor accessor = (GuiIngameAccessor) mc.ingameGUI;
                accessor.renderHotBar(scaleResolution, this.deltaTicks);
            }
        }

        private void drawSpec(float x, float y, SpectatorMenu menu, GuiSpectatorAccessor specAccessor, GuiAccessor guiAccessor, ScaledResolution scaledResolution) {
            if (menu != null) {
                float g = specAccessor.alpha();
                if (g <= 0.0f) {
                    menu.func_178641_d();
                } else {
                    float h = guiAccessor.getZLevel();
                    guiAccessor.setZLevel(0f);
                    SpectatorDetails spectatorDetails = menu.func_178646_f();
                    specAccessor.draw(scaledResolution, g, scaledResolution.getScaledWidth() / 2, scaledResolution.getScaledHeight() - 22, spectatorDetails);
                    guiAccessor.setZLevel(h);
                }
            }
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
