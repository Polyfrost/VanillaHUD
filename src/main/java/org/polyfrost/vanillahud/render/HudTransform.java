package org.polyfrost.vanillahud.render;

//? if >=26 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;*/
//?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}
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

    //? if >=26 {
    /*public static void begin(GuiGraphicsExtractor graphics, VanillaHud provider) {}

    public static void end(GuiGraphicsExtractor graphics) {}*/
    //?} else {
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
        pose.pushPose();
        pose.translate(gx, gy, 0f);
        pose.scale(s, s, 1f);
        pose.translate(-ox, -oy, 0f);
    }

    public static void end(GuiGraphics graphics) {
        graphics.pose().popPose();
    }
    //?}
}
