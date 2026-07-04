package org.polyfrost.vanillahud.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//? if >=26 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayMixin {
    @WrapMethod(
            //? if >=26 {
            /*method = "extractRenderState"
            *///?} else {
            method = "render"
            //?}
    )
    private void vanillahud$boss(GuiGraphics guiGraphics, Operation<Void> original) {
        if (!Huds.INSTANCE.getBossBar().shouldRender()) return;

        HudTransform.begin(guiGraphics, Huds.INSTANCE.getBossBar());
        original.call(guiGraphics);
        HudTransform.end(guiGraphics);
    }
}
