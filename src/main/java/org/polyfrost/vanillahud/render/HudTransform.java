package org.polyfrost.vanillahud.render;

import net.minecraft.client.gui.GuiGraphics;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.vanillahud.hud.VanillaHud;

public final class HudTransform {
    private HudTransform() {}

    private static VanillaHud resolve(VanillaHud provider) {
        VanillaHud fallback = provider;
        for (Hud h : HudManager.INSTANCE.getActiveInstances()) {
            if (provider.getClass().isInstance(h)) {
                if (h != provider) {
                    return (VanillaHud) h;
                }
                fallback = (VanillaHud) h;
            }
        }
        return fallback;
    }

    public static void begin(GuiGraphics graphics, VanillaHud provider) {
        VanillaHud hud = resolve(provider);
        boolean placed = hud != provider;
        int w = graphics.guiWidth();
        int h = graphics.guiHeight();
        float ox = provider.vanillaOriginX(w, h);
        float oy = provider.vanillaOriginY(w, h);
        float gx = placed ? hud.getX() : ox;
        float gy = placed ? hud.getY() : oy;
        float s = placed ? hud.getEffectiveScale() : 1f;
        var pose = graphics.pose();
        pose.pushMatrix();
        //? if <= 1.21.4 {
        // pose.translate(gx, gy, 0f);
        // pose.scale(s, s, 1f);
        // pose.translate(-ox, -oy, 0f);
        //?} else {
        pose.translate(gx, gy);
        pose.scale(s, s);
        pose.translate(-ox, -oy);
        //?}
    }

    public static void end(GuiGraphics graphics) {
        graphics.pose().popMatrix();
    }
}
