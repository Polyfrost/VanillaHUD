package org.polyfrost.vanillahud.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayMixin {
    @WrapMethod(
            //? if >=26 {
            method = "extractRenderState"
            //?} else {
            /*method = "render"
            *///?}
    )
    private void vanillahud$boss(GuiGraphicsExtractor graphics, Operation<Void> original) {
        if (!Huds.INSTANCE.getBossBar().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getBossBar());
        original.call(graphics);
        HudTransform.end(graphics);
    }
}
