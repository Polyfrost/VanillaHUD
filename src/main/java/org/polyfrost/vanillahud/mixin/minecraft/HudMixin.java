package org.polyfrost.vanillahud.mixin.minecraft;

//? if >=26.2 {
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.config.ModConfig;
import org.polyfrost.vanillahud.render.HudTransforms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {
    @Inject(method = "extractItemHotbar", at = @At("HEAD"), cancellable = true)
    private void vanillahud$pushHotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (VanillaHUD.isApec() || !ModConfig.hotbar.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.hotbar, graphics.guiWidth() / 2.0F - 91.0F, graphics.guiHeight() - 22.0F);
    }

    @Inject(method = "extractItemHotbar", at = @At("RETURN"))
    private void vanillahud$popHotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!VanillaHUD.isApec() && ModConfig.hotbar.enabled) {
            HudTransforms.pop(graphics);
        }
    }

    @Inject(method = "extractSelectedItemName", at = @At("HEAD"), cancellable = true)
    private void vanillahud$pushSelectedItemName(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (VanillaHUD.isApec() || !ModConfig.itemTooltip.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.itemTooltip, graphics.guiWidth() / 2.0F, graphics.guiHeight() - 59.0F);
    }

    @Inject(method = "extractSelectedItemName", at = @At("RETURN"))
    private void vanillahud$popSelectedItemName(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (!VanillaHUD.isApec() && ModConfig.itemTooltip.enabled) {
            HudTransforms.pop(graphics);
        }
    }

    @Inject(method = "extractOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void vanillahud$pushActionBar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (VanillaHUD.isApec() || !ModConfig.actionBar.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.actionBar, graphics.guiWidth() / 2.0F, graphics.guiHeight() - 68.0F);
    }

    @Inject(method = "extractOverlayMessage", at = @At("RETURN"))
    private void vanillahud$popActionBar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!VanillaHUD.isApec() && ModConfig.actionBar.enabled) {
            HudTransforms.pop(graphics);
        }
    }

    @Inject(method = "extractTitle", at = @At("HEAD"), cancellable = true)
    private void vanillahud$pushTitle(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!ModConfig.title.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.title, graphics.guiWidth() / 2.0F, graphics.guiHeight() / 2.0F);
    }

    @Inject(method = "extractTitle", at = @At("RETURN"))
    private void vanillahud$popTitle(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ModConfig.title.enabled) {
            HudTransforms.pop(graphics);
        }
    }

    @Inject(method = "extractScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void vanillahud$pushScoreboard(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (VanillaHUD.isApec() || VanillaHUD.isSkyHanniScoreboard() || !ModConfig.scoreboard.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.scoreboard, graphics.guiWidth(), graphics.guiHeight() / 2.0F);
    }

    @Inject(method = "extractScoreboardSidebar", at = @At("RETURN"))
    private void vanillahud$popScoreboard(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!VanillaHUD.isApec() && !VanillaHUD.isSkyHanniScoreboard() && ModConfig.scoreboard.enabled) {
            HudTransforms.pop(graphics);
        }
    }

    @Inject(method = "extractTabList", at = @At("HEAD"), cancellable = true)
    private void vanillahud$pushTabList(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (VanillaHUD.isForceDisableCompactTab() || !ModConfig.tabList.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.tabList, graphics.guiWidth() / 2.0F, 10.0F);
    }

    @Inject(method = "extractTabList", at = @At("RETURN"))
    private void vanillahud$popTabList(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!VanillaHUD.isForceDisableCompactTab() && ModConfig.tabList.enabled) {
            HudTransforms.pop(graphics);
        }
    }

    @Inject(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"), require = 0)
    private void vanillahud$pushContextualBackground(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        HudTransforms.push(graphics, ModConfig.experience, graphics.guiWidth() / 2.0F - 91.0F, graphics.guiHeight() - 29.0F);
    }

    @Inject(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.AFTER), require = 0)
    private void vanillahud$popContextualBackground(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        HudTransforms.pop(graphics);
    }

    @Inject(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"), require = 0)
    private void vanillahud$pushContextualLevel(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        HudTransforms.push(graphics, ModConfig.experience, graphics.guiWidth() / 2.0F - 91.0F, graphics.guiHeight() - 29.0F);
    }

    @Inject(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V", shift = At.Shift.AFTER), require = 0)
    private void vanillahud$popContextualLevel(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        HudTransforms.pop(graphics);
    }

    @Inject(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"), require = 0)
    private void vanillahud$pushContextualBar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        HudTransforms.push(graphics, ModConfig.experience, graphics.guiWidth() / 2.0F - 91.0F, graphics.guiHeight() - 29.0F);
    }

    @Inject(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.AFTER), require = 0)
    private void vanillahud$popContextualBar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        HudTransforms.pop(graphics);
    }

    @Inject(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractArmor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIII)V"))
    private void vanillahud$pushArmor(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        HudTransforms.push(graphics, ModConfig.armor, graphics.guiWidth() / 2.0F - 91.0F, graphics.guiHeight() - 49.0F);
    }

    @Inject(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractArmor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIII)V", shift = At.Shift.AFTER))
    private void vanillahud$popArmor(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        HudTransforms.pop(graphics);
    }

    @Inject(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractHearts(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V"))
    private void vanillahud$pushHealth(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        HudTransforms.push(graphics, ModConfig.health, graphics.guiWidth() / 2.0F - 91.0F, graphics.guiHeight() - 39.0F);
    }

    @Inject(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractHearts(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V", shift = At.Shift.AFTER))
    private void vanillahud$popHealth(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        HudTransforms.pop(graphics);
    }

    @Inject(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractFood(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;II)V"))
    private void vanillahud$pushFood(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        HudTransforms.push(graphics, ModConfig.hunger, graphics.guiWidth() / 2.0F + 10.0F, graphics.guiHeight() - 39.0F);
    }

    @Inject(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractFood(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;II)V", shift = At.Shift.AFTER))
    private void vanillahud$popFood(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        HudTransforms.pop(graphics);
    }

    @Inject(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractAirBubbles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;III)V"))
    private void vanillahud$pushAir(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        HudTransforms.push(graphics, ModConfig.air, graphics.guiWidth() / 2.0F + 10.0F, graphics.guiHeight() - 59.0F);
    }

    @Inject(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractAirBubbles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;III)V", shift = At.Shift.AFTER))
    private void vanillahud$popAir(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        HudTransforms.pop(graphics);
    }

    @Inject(method = "extractVehicleHealth", at = @At("HEAD"), cancellable = true)
    private void vanillahud$pushMountHealth(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (!ModConfig.mountHealth.enabled && !ModConfig.hunger.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.mountHealth.enabled ? ModConfig.mountHealth : ModConfig.hunger, graphics.guiWidth() / 2.0F + 10.0F, graphics.guiHeight() - 39.0F);
    }

    @Inject(method = "extractVehicleHealth", at = @At("RETURN"))
    private void vanillahud$popMountHealth(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (ModConfig.mountHealth.enabled || ModConfig.hunger.enabled) {
            HudTransforms.pop(graphics);
        }
    }
}
//?} else {
/*public final class HudMixin {
}
*///?}
