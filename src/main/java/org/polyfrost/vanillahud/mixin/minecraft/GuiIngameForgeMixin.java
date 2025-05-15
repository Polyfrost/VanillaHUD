package org.polyfrost.vanillahud.mixin.minecraft;

import dev.deftu.omnicore.client.render.OmniResolution;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.oneconfig.hud.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.*;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraftforge.client.GuiIngameForge;
import org.lwjgl.opengl.GL11;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.config.ModConfig;
import org.polyfrost.vanillahud.hooks.ScoreboardHook;
import org.polyfrost.vanillahud.hooks.TabHook;
import org.polyfrost.vanillahud.hud.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.polyfrost.vanillahud.hud.Health.healthLink;
import static org.polyfrost.vanillahud.hud.Hunger.*;

@Mixin(value = GuiIngameForge.class)
public abstract class GuiIngameForgeMixin {

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;shouldDrawHUD()Z"))
    private boolean example(PlayerControllerMP instance) {
        return instance.shouldDrawHUD() || (HudManager.isPanelOpen() && !VanillaHUD.isApec());
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getRenderViewEntity()Lnet/minecraft/entity/Entity;"))
    private Entity example(Minecraft instance) {
        return (HudManager.isPanelOpen() && !VanillaHUD.isApec()) ? instance.thePlayer : instance.getRenderViewEntity();
    }




    @Inject(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderPlayerList(II)V"))
    private void setCheck(float partialTicks, CallbackInfo ci) {
        TabList.isGuiIngame = true;
    }

    @Unique
    private boolean toggled, isPressed;

    @Redirect(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z"))
    private boolean tabExample(KeyBinding instance) {
        if (!ModConfig.tab.enabled) {
            return instance.isKeyDown();
        }
        ScoreObjective scoreobjective = mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(0);
        NetHandlerPlayClient handler = mc.thePlayer.sendQueue;

        boolean flag = !VanillaHUD.isCompactTab() || HudManager.isPanelOpen();

        if (flag) {
            if (!mc.isIntegratedServerRunning() || handler.getPlayerInfoMap().size() > 1 || scoreobjective != null || HudManager.isPanelOpen()) {
                TabHook.gettingSize = true;
                GlStateManager.pushMatrix();
                GlStateManager.scale(0f, 0f, 1f);
                mc.ingameGUI.getTabList().renderPlayerlist(OmniResolution.getScaledWidth(), mc.theWorld.getScoreboard(), scoreobjective);
                GlStateManager.popMatrix();
                TabHook.gettingSize = false;
            } else {
                return toggled = false;
            }
        }

        boolean down = instance.isKeyDown();
        if (TabList.TabHud.displayMode) {
            if (down != isPressed && (isPressed = down)) {
                toggled = !toggled;
            }
        } else {
            toggled = down;
        }

        TabList.hud.doAnimation(toggled || HudManager.isPanelOpen());
        if (flag) TabList.hud.drawBG();

        return (toggled || !TabList.animation.isFinished() || HudManager.isPanelOpen()) && TabList.hud.shouldRender();
    }

    @Redirect(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isIntegratedServerRunning()Z"))
    private boolean tabExample2(Minecraft instance) {
        return instance.isIntegratedServerRunning() && !HudManager.isPanelOpen();
    }

    @Inject(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;renderPlayerlist(ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreObjective;)V"))
    private void enableScissor(int width, int height, CallbackInfo ci) {
        if (VanillaHUD.isCompactTab()) return;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        Position position = TabList.hud.position;
        int scale = (int) OmniResolution.getScaleFactor();
        GL11.glScissor((int) (position.getX() * scale), (int) ((OmniResolution.getScaledHeight() - position.getY() - TabList.animation.get()) * scale), (int) (position.getWidth() * scale), (int) (TabList.animation.get() * scale));
    }

    @Inject(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;renderPlayerlist(ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreObjective;)V", shift = At.Shift.AFTER))
    private void disable(int width, int height, CallbackInfo ci) {
        if (VanillaHUD.isCompactTab()) return;
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Inject(method = "renderGameOverlay", at = @At("HEAD"))
    private void head(float partialTicks, CallbackInfo ci) {
        ScoreboardHook.canDraw = false;
    }

}
