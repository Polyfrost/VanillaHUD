package org.polyfrost.vanillahud.mixin.minecraft;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.vanillahud.Compatibility;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.hud.bars.ExperienceHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameForge.class)
public abstract class Experience_GuiIngame_Mixin {

    @Shadow protected abstract void post(RenderGameOverlayEvent.ElementType type);

    @Inject(method = {"renderExperience", "renderJumpBar"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", ordinal = 0), cancellable = true)
    private void setupExpTranslationAndScale(int width, int height, CallbackInfo ci) {
        if (Compatibility.INSTANCE.isApec()) {
            return;
        }
        ExperienceHud hud = VanillaHUD.getExperience();
        if (hud.getHidden()) {
            post(RenderGameOverlayEvent.ElementType.EXPERIENCE);
            ci.cancel();
            return;
        }
        float scale = hud.getScale();
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) hud.getX() - 182, (int) hud.getY() - 29, 0F);
        GlStateManager.scale(scale, scale, 1F);
    }

    @Inject(method = {"renderExperience", "renderJumpBar"}, at = @At("RETURN"), remap = false)
    private void popExpMatrix(CallbackInfo ci) {
        if (Compatibility.INSTANCE.isApec()) {
            return;
        }
        GlStateManager.popMatrix();
    }

    @ModifyArg(method = "renderExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I"), index = 1)
    private int expLevelHeight(int yIn) {
        if (Compatibility.INSTANCE.isApec()) {
            return yIn;
        }
        return yIn + 4 - (int) VanillaHUD.getExperience().getExpHeight();
    }

    @Redirect(method = "renderExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;gameIsSurvivalOrAdventure()Z"))
    private boolean expExample(PlayerControllerMP instance) {
        if (Compatibility.INSTANCE.isApec()) {
            return instance.gameIsSurvivalOrAdventure();
        }
        return HudManager.isPanelOpen() || instance.gameIsSurvivalOrAdventure();
    }
}
