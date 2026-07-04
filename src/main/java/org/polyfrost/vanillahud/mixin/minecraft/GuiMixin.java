package org.polyfrost.vanillahud.mixin.minecraft;

//? if <26 {
/*import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.PlayerRideableJumping;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.config.ModConfig;
import org.polyfrost.vanillahud.render.HudTransforms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "renderItemHotbar", at = @At("HEAD"), cancellable = true)
    private void vanillahud$pushHotbar(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (VanillaHUD.isApec() || !ModConfig.hotbar.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.hotbar, graphics.guiWidth() / 2.0F - 91.0F, graphics.guiHeight() - 22.0F);
    }

    @Inject(method = "renderItemHotbar", at = @At("RETURN"))
    private void vanillahud$popHotbar(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!VanillaHUD.isApec() && ModConfig.hotbar.enabled) {
            HudTransforms.pop(graphics);
        }
    }

    @Inject(method = "renderSelectedItemName", at = @At("HEAD"), cancellable = true)
    private void vanillahud$pushSelectedItemName(GuiGraphics graphics, CallbackInfo ci) {
        if (VanillaHUD.isApec() || !ModConfig.itemTooltip.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.itemTooltip, graphics.guiWidth() / 2.0F, graphics.guiHeight() - 59.0F);
    }

    @Inject(method = "renderSelectedItemName", at = @At("RETURN"))
    private void vanillahud$popSelectedItemName(GuiGraphics graphics, CallbackInfo ci) {
        if (!VanillaHUD.isApec() && ModConfig.itemTooltip.enabled) {
            HudTransforms.pop(graphics);
        }
    }

    @Inject(method = "renderOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void vanillahud$pushActionBar(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (VanillaHUD.isApec() || !ModConfig.actionBar.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.actionBar, graphics.guiWidth() / 2.0F, graphics.guiHeight() - 68.0F);
    }

    @Inject(method = "renderOverlayMessage", at = @At("RETURN"))
    private void vanillahud$popActionBar(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!VanillaHUD.isApec() && ModConfig.actionBar.enabled) {
            HudTransforms.pop(graphics);
        }
    }

    @Inject(method = "renderTitle", at = @At("HEAD"), cancellable = true)
    private void vanillahud$pushTitle(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!ModConfig.title.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.title, graphics.guiWidth() / 2.0F, graphics.guiHeight() / 2.0F);
    }

    @Inject(method = "renderTitle", at = @At("RETURN"))
    private void vanillahud$popTitle(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ModConfig.title.enabled) {
            HudTransforms.pop(graphics);
        }
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void vanillahud$pushScoreboard(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (VanillaHUD.isApec() || VanillaHUD.isSkyHanniScoreboard() || !ModConfig.scoreboard.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.scoreboard, graphics.guiWidth(), graphics.guiHeight() / 2.0F);
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("RETURN"))
    private void vanillahud$popScoreboard(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!VanillaHUD.isApec() && !VanillaHUD.isSkyHanniScoreboard() && ModConfig.scoreboard.enabled) {
            HudTransforms.pop(graphics);
        }
    }

    @Inject(method = "renderTabList", at = @At("HEAD"), cancellable = true)
    private void vanillahud$pushTabList(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (VanillaHUD.isForceDisableCompactTab() || !ModConfig.tabList.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.tabList, graphics.guiWidth() / 2.0F, 10.0F);
    }

    @Inject(method = "renderTabList", at = @At("RETURN"))
    private void vanillahud$popTabList(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!VanillaHUD.isForceDisableCompactTab() && ModConfig.tabList.enabled) {
            HudTransforms.pop(graphics);
        }
    }

    //? if <1.21.8 {
    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true, require = 0)
    private void vanillahud$pushExperienceBar(GuiGraphics graphics, int x, CallbackInfo ci) {
        if (!ModConfig.experience.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.experience, graphics.guiWidth() / 2.0F - 91.0F, graphics.guiHeight() - 29.0F);
    }

    @Inject(method = "renderExperienceBar", at = @At("RETURN"), require = 0)
    private void vanillahud$popExperienceBar(GuiGraphics graphics, int x, CallbackInfo ci) {
        if (ModConfig.experience.enabled) {
            HudTransforms.pop(graphics);
        }
    }

    @Inject(method = "renderJumpMeter", at = @At("HEAD"), cancellable = true, require = 0)
    private void vanillahud$pushJumpMeter(PlayerRideableJumping vehicle, GuiGraphics graphics, int x, CallbackInfo ci) {
        if (!ModConfig.experience.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.experience, graphics.guiWidth() / 2.0F - 91.0F, graphics.guiHeight() - 29.0F);
    }

    @Inject(method = "renderJumpMeter", at = @At("RETURN"), require = 0)
    private void vanillahud$popJumpMeter(PlayerRideableJumping vehicle, GuiGraphics graphics, int x, CallbackInfo ci) {
        if (ModConfig.experience.enabled) {
            HudTransforms.pop(graphics);
        }
    }

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true, require = 0)
    private void vanillahud$pushExperienceLevel(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!ModConfig.experience.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.experience, graphics.guiWidth() / 2.0F - 91.0F, graphics.guiHeight() - 29.0F);
    }

    @Inject(method = "renderExperienceLevel", at = @At("RETURN"), require = 0)
    private void vanillahud$popExperienceLevel(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ModConfig.experience.enabled) {
            HudTransforms.pop(graphics);
        }
    }
    //?}

    @Inject(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"), require = 0)
    private void vanillahud$pushContextualBackground(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        HudTransforms.push(graphics, ModConfig.experience, graphics.guiWidth() / 2.0F - 91.0F, graphics.guiHeight() - 29.0F);
    }

    @Inject(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.AFTER), require = 0)
    private void vanillahud$popContextualBackground(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        HudTransforms.pop(graphics);
    }

    @Inject(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"), require = 0)
    private void vanillahud$pushContextualLevel(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        HudTransforms.push(graphics, ModConfig.experience, graphics.guiWidth() / 2.0F - 91.0F, graphics.guiHeight() - 29.0F);
    }

    @Inject(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V", shift = At.Shift.AFTER), require = 0)
    private void vanillahud$popContextualLevel(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        HudTransforms.pop(graphics);
    }

    @Inject(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"), require = 0)
    private void vanillahud$pushContextualBar(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        HudTransforms.push(graphics, ModConfig.experience, graphics.guiWidth() / 2.0F - 91.0F, graphics.guiHeight() - 29.0F);
    }

    @Inject(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.AFTER), require = 0)
    private void vanillahud$popContextualBar(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        HudTransforms.pop(graphics);
    }

    @Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderArmor(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIII)V"))
    private void vanillahud$pushArmor(GuiGraphics graphics, CallbackInfo ci) {
        HudTransforms.push(graphics, ModConfig.armor, graphics.guiWidth() / 2.0F - 91.0F, graphics.guiHeight() - 49.0F);
    }

    @Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderArmor(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIII)V", shift = At.Shift.AFTER))
    private void vanillahud$popArmor(GuiGraphics graphics, CallbackInfo ci) {
        HudTransforms.pop(graphics);
    }

    @Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHearts(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V"))
    private void vanillahud$pushHealth(GuiGraphics graphics, CallbackInfo ci) {
        HudTransforms.push(graphics, ModConfig.health, graphics.guiWidth() / 2.0F - 91.0F, graphics.guiHeight() - 39.0F);
    }

    @Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHearts(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V", shift = At.Shift.AFTER))
    private void vanillahud$popHealth(GuiGraphics graphics, CallbackInfo ci) {
        HudTransforms.pop(graphics);
    }

    @Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderFood(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;II)V"))
    private void vanillahud$pushFood(GuiGraphics graphics, CallbackInfo ci) {
        HudTransforms.push(graphics, ModConfig.hunger, graphics.guiWidth() / 2.0F + 10.0F, graphics.guiHeight() - 39.0F);
    }

    @Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderFood(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;II)V", shift = At.Shift.AFTER))
    private void vanillahud$popFood(GuiGraphics graphics, CallbackInfo ci) {
        HudTransforms.pop(graphics);
    }

    @Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderAirBubbles(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;III)V"), require = 0)
    private void vanillahud$pushAir(GuiGraphics graphics, CallbackInfo ci) {
        HudTransforms.push(graphics, ModConfig.air, graphics.guiWidth() / 2.0F + 10.0F, graphics.guiHeight() - 59.0F);
    }

    @Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderAirBubbles(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;III)V", shift = At.Shift.AFTER), require = 0)
    private void vanillahud$popAir(GuiGraphics graphics, CallbackInfo ci) {
        HudTransforms.pop(graphics);
    }

    @Inject(method = "renderVehicleHealth", at = @At("HEAD"), cancellable = true)
    private void vanillahud$pushMountHealth(GuiGraphics graphics, CallbackInfo ci) {
        if (!ModConfig.mountHealth.enabled && !ModConfig.hunger.enabled) {
            ci.cancel();
            return;
        }
        HudTransforms.push(graphics, ModConfig.mountHealth.enabled ? ModConfig.mountHealth : ModConfig.hunger, graphics.guiWidth() / 2.0F + 10.0F, graphics.guiHeight() - 39.0F);
    }

    @Inject(method = "renderVehicleHealth", at = @At("RETURN"))
    private void vanillahud$popMountHealth(GuiGraphics graphics, CallbackInfo ci) {
        if (ModConfig.mountHealth.enabled || ModConfig.hunger.enabled) {
            HudTransforms.pop(graphics);
        }
    }
}
*///?} else {
/*public final class GuiMixin {
}
*///?}
