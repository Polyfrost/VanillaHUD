package org.polyfrost.vanillahud.mixin.minecraft;

import net.minecraft.client.gui.components.BossHealthOverlay;
import org.polyfrost.vanillahud.config.ModConfig;
import org.polyfrost.vanillahud.render.HudTransforms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else
/*import net.minecraft.client.gui.GuiGraphics;*/

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {
    //? if >=26 {
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void vanillahud$pushBossBar(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (!ModConfig.bossBar.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.bossBar, graphics.guiWidth() / 2.0F, 12.0F);
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void vanillahud$popBossBar(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (ModConfig.bossBar.enabled) {
            HudTransforms.pop(graphics);
        }
    }
    //?} else {
    /*@Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void vanillahud$pushBossBar(GuiGraphics graphics, CallbackInfo ci) {
        if (!ModConfig.bossBar.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.bossBar, graphics.guiWidth() / 2.0F, 12.0F);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void vanillahud$popBossBar(GuiGraphics graphics, CallbackInfo ci) {
        if (ModConfig.bossBar.enabled) {
            HudTransforms.pop(graphics);
        }
    }
    *///?}
}
