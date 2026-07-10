package org.polyfrost.vanillahud.mixin.elements;

import net.minecraft.client.gui.components.SubtitleOverlay;
import org.spongepowered.asm.mixin.Mixin;

//? if <1.21.4 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.hud.SubtitlesHud;
import org.polyfrost.vanillahud.render.HudTransform;
*///?}

@Mixin(SubtitleOverlay.class)
public class GuiMixinSubtitles {
    //? if <1.21.4 {
    /*@WrapMethod(method = "render")
    private void vanillahud$subtitles(GuiGraphicsExtractor graphics, Operation<Void> original) {
        SubtitlesHud hud = Huds.INSTANCE.getSubtitles();
        if (!hud.shouldRender()) return;

        HudTransform.begin(graphics, hud);
        original.call(graphics);
        HudTransform.end(graphics);
    }
    *///?}
}
