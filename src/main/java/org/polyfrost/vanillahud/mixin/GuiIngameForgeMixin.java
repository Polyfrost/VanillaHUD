package org.polyfrost.vanillahud.mixin;

import cc.polyfrost.oneconfig.hud.*;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.common.ForgeHooks;
import org.lwjgl.opengl.GL11;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.config.ModConfig;
import org.polyfrost.vanillahud.hooks.ScoreboardHook;
import org.polyfrost.vanillahud.hooks.TabHook;
import org.polyfrost.vanillahud.hud.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Random;

import static org.polyfrost.vanillahud.hud.Health.healthLink;
import static org.polyfrost.vanillahud.hud.Hunger.*;

@Mixin(value = GuiIngameForge.class)
public abstract class GuiIngameForgeMixin {

    @Unique
    private final Minecraft mc = Minecraft.getMinecraft();

    @Shadow
    public static int left_height;

    @Shadow
    public static int right_height;

    @Shadow
    public static boolean renderHealthMount;

    @Shadow
    public static boolean renderFood;

    @ModifyArgs(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderAir(II)V"))
    private void air(Args args) {
        if (VanillaHUD.isApec()) {
            return;
        }
        args.set(0, -182);
        args.set(1, right_height);
    }

    private boolean needsToResetAir = false;

    @Inject(method = "renderAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V"))
    private void air(CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) Air.hud.position.getX(), (int) Air.hud.position.getY() - (Air.AirHud.healthLink ? healthLink() : 0) - (Air.AirHud.mountLink ? mountLink() : 0), 0F);
        GlStateManager.scale(Air.hud.getScale(), Air.hud.getScale(), 1F);
        needsToResetAir = true;
    }

    @Inject(method = "renderAir", at = @At("RETURN"), remap = false)
    private void airReturn(CallbackInfo ci) {
        if (needsToResetAir) {
            GlStateManager.popMatrix();
            needsToResetAir = false;
        }
    }

    @Inject(method = "renderAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V"))
    private void airPop(CallbackInfo ci) {
        if (needsToResetAir) {
            GlStateManager.popMatrix();
            needsToResetAir = false;
        }
    }

    @Redirect(method = "renderAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isInsideOfMaterial(Lnet/minecraft/block/material/Material;)Z"))
    private boolean airExample(EntityPlayer instance, Material material) {
        if (VanillaHUD.isApec()) {
            return instance.isInsideOfMaterial(material);
        }
        return instance.isInsideOfMaterial(material) || HudCore.editing;
    }

    @ModifyArgs(method = "renderAir", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V"))
    private void airAlignment(Args args) {
        if (VanillaHUD.isApec()) {
            return;
        }
        int left = (int) args.get(0) + (Air.hud.alignment ? 81 : 9);
        args.set(0, Air.hud.alignment ? left : -left);
    }

    @Redirect(method = "renderAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getRenderViewEntity()Lnet/minecraft/entity/Entity;"))
    private Entity airEdit(Minecraft instance) {
        if (VanillaHUD.isApec()) {
            return instance.getRenderViewEntity();
        }
        return HudCore.editing ? instance.thePlayer : instance.getRenderViewEntity();
    }

    @ModifyArgs(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderArmor(II)V"))
    private void armor(Args args) {
        if (VanillaHUD.isApec()) {
            return;
        }
        args.set(0, 182);
        args.set(1, left_height);
    }

    private boolean needsToResetArmor = false;

    @Inject(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V"))
    private void armor(CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) Armor.hud.position.getX(), (int) Armor.hud.position.getY() - (Armor.ArmorHud.healthLink ? healthLink() : 0) - (Armor.ArmorHud.mountLink ? mountLink() : 0), 0F);
        GlStateManager.scale(Armor.hud.getScale(), Armor.hud.getScale(), 1F);
        needsToResetArmor = true;
    }

    @Inject(method = "renderArmor", at = @At("RETURN"), remap = false)
    private void armorReturn(CallbackInfo ci) {
        if (needsToResetArmor) {
            GlStateManager.popMatrix();
            needsToResetArmor = false;
        }
    }

    @Inject(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V"))
    private void armorPop(CallbackInfo ci) {
        if (needsToResetArmor) {
            GlStateManager.popMatrix();
            needsToResetArmor = false;
        }
    }

    @Redirect(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;getTotalArmorValue(Lnet/minecraft/entity/player/EntityPlayer;)I"), remap = false)
    private int armorExample(EntityPlayer player) {
        if (VanillaHUD.isApec()) {
            return ForgeHooks.getTotalArmorValue(player);
        }
        int value = ForgeHooks.getTotalArmorValue(player);
        return HudCore.editing ? value > 0 ? value : 10 : value;
    }

    @Redirect(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V"))
    private void armorFlip(GuiIngameForge instance, int x, int y, int textureX, int textureY, int width, int height) {
        if (VanillaHUD.isApec()) {
            instance.drawTexturedModalRect(x, y, textureX, textureY, width, height);
            return;
        }
        int left = Armor.hud.alignment ? 72 - x : x;
        if (Armor.hud.alignment && textureX == 25) {
            Gui.drawScaledCustomSizeModalRect(left, y, textureX + 9, textureY, -9, 9, 9, 9, 256, 256);
        } else {
            instance.drawTexturedModalRect(left, y, textureX, textureY, width, height);
        }
    }

    @ModifyArgs(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderExperience(II)V"))
    private void exp(Args args) {
        if (VanillaHUD.isApec()) {
            return;
        }
        args.set(0, 182);
        args.set(1, 29);
    }

    private boolean needsToResetExp = false;

    @Inject(method = "renderExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", ordinal = 0))
    private void experience(int width, int height, CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) Experience.hud.position.getX(), (int) Experience.hud.position.getY(), 0F);
        GlStateManager.scale(Experience.hud.getScale(), Experience.hud.getScale(), 1F);
        needsToResetExp = true;
    }

    @Inject(method = "renderExperience", at = @At("RETURN"), remap = false)
    private void experienceReturn(CallbackInfo ci) {
        if (needsToResetExp) {
            GlStateManager.popMatrix();
            needsToResetExp = false;
        }
    }

    @Inject(method = "renderExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", shift = At.Shift.AFTER, ordinal = 1))
    private void experiencePop(CallbackInfo ci) {
        if (needsToResetExp) {
            GlStateManager.popMatrix();
            needsToResetExp = false;
        }
    }

    @ModifyArgs(method = "renderExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I"))
    private void expLevelHeight(Args args) {
        if (VanillaHUD.isApec()) {
            return;
        }
        args.set(2, (int) args.get(2) + 4 - (int) Experience.ExperienceHud.expHeight);
    }

    @Redirect(method = "renderExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;gameIsSurvivalOrAdventure()Z"))
    private boolean expExample(PlayerControllerMP instance) {
        if (VanillaHUD.isApec()) {
            return instance.gameIsSurvivalOrAdventure();
        }
        return HudCore.editing || instance.gameIsSurvivalOrAdventure();
    }

    @ModifyArgs(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderHealth(II)V"))
    private void health(Args args) {
        if (VanillaHUD.isApec()) {
            return;
        }
        args.set(0, 182);
        args.set(1, left_height);
    }

    private boolean needsToResetHealth = false;

    @Inject(method = "renderHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V"))
    private void health(CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) Health.hud.position.getX(), (int) Health.hud.position.getY() - (Health.HealthHud.mountLink ? mountLink() : 0), 0F);
        GlStateManager.scale(Health.hud.getScale(), Health.hud.getScale(), 1F);
        needsToResetHealth = true;
    }

    @Inject(method = "renderHealth", at = @At("RETURN"), remap = false)
    private void healthReturn(CallbackInfo ci) {
        if (needsToResetHealth) {
            GlStateManager.popMatrix();
            needsToResetHealth = false;
        }
    }

    @Inject(method = "renderHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V"))
    private void healthPop(CallbackInfo ci) {
        if (needsToResetHealth) {
            GlStateManager.popMatrix();
            needsToResetHealth = false;
        }
    }

    @Redirect(method = "renderHealth", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V"))
    private void healthFlip(GuiIngameForge instance, int x, int y, int textureX, int textureY, int width, int height) {
        if (VanillaHUD.isApec()) {
            instance.drawTexturedModalRect(x, y, textureX, textureY, width, height);
            return;
        }
        if (textureX < 61 || ((textureX - 16) / 9) % 2 == 0 || !Health.hud.alignment) {
            instance.drawTexturedModalRect(x, y, textureX, textureY, width, height);
        } else {
            Gui.drawScaledCustomSizeModalRect(x, y, textureX + 9, textureY, -9, 9, 9, 9, 256, 256);
        }
    }

    @ModifyVariable(method = "renderHealth", at = @At("STORE"), ordinal = 14, remap = false)
    private int healthAlignment(int x) {
        if (VanillaHUD.isApec()) {
            return x;
        }
        return Health.hud.alignment ? 72 - x : x;
    }

    @ModifyVariable(method = "renderHealth", at = @At("STORE"), ordinal = 15, remap = false)
    private int healthMode(int y) {
        if (VanillaHUD.isApec()) {
            return y;
        }
        if (HudCore.editing) return 0;
        if (Health.HealthHud.mode) return y;
        return -y;
    }

    @Redirect(method = "renderHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getRenderViewEntity()Lnet/minecraft/entity/Entity;"))
    private Entity healthEdit(Minecraft instance) {
        if (VanillaHUD.isApec()) {
            return instance.getRenderViewEntity();
        }
        return HudCore.editing ? instance.thePlayer : instance.getRenderViewEntity();
    }


    @ModifyVariable(method = "renderHealth", at = @At(value = "STORE"), name = "regen", remap = false)
    private int healthAnimation(int value) {
        return Health.HealthHud.animation ? value : -1;
    }

    @Redirect(method = "renderHealth", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"), remap = false)
    private int healthAnimation1(Random instance, int i) {
        return Health.HealthHud.animation ? instance.nextInt(i) : 0;
    }

    @ModifyArgs(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderFood(II)V"))
    private void renderFood(Args args) {
        if (VanillaHUD.isApec()) {
            return;
        }
        args.set(0, -182);
        args.set(1, right_height);
    }

    @Inject(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderFood(II)V"))
    private void hungerTranslate(float partialTicks, CallbackInfo ci) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) Hunger.hud.position.getX(), (int) Hunger.hud.position.getY() - (Hunger.HungerHud.healthLink ? healthLink() : 0) - (Hunger.HungerHud.mountLink ? mountLink() : 0), 0F);
        GlStateManager.scale(Hunger.hud.getScale(), Hunger.hud.getScale(), 1F);
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true, remap = false)
    private void hungerCancel(int width, int height, CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        if (!(renderFood || Hunger.mountHud.isEnabled())) {
            ci.cancel();
        }
    }

    @Inject(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderFood(II)V", shift = At.Shift.AFTER))
    private void hungerReturn(CallbackInfo ci) {
        GlStateManager.popMatrix();
    }

    @ModifyVariable(method = "renderFood", at = @At("STORE"), ordinal = 8, remap = false)
    private int hungerAlignment(int x) {
        if (VanillaHUD.isApec()) {
            return x;
        }
        return Hunger.hud.alignment ? 81 + x : -(x + 9);
    }

    @Redirect(method = "renderFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getRenderViewEntity()Lnet/minecraft/entity/Entity;"))
    private Entity hungerEdit(Minecraft instance) {
        if (VanillaHUD.isApec()) {
            return instance.getRenderViewEntity();
        }
        return HudCore.editing ? instance.thePlayer : instance.getRenderViewEntity();
    }

    @Redirect(method = "renderFood", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"), remap = false)
    private int hungerAnimation(Random instance, int i) {
        return HungerHud.animation ? instance.nextInt(i) : 1;
    }

    @Redirect(method = "renderHealthMount", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;ridingEntity:Lnet/minecraft/entity/Entity;"))
    private Entity mountExample(EntityPlayer instance) {
        if (VanillaHUD.isApec()) {
            return instance.ridingEntity;
        }
        Entity entity = instance.ridingEntity;

        return (entity instanceof EntityLivingBase) ? entity : HudCore.editing ? instance : null;
    }

    @Dynamic
    @ModifyVariable(method = "renderHealthMount", at = @At("STORE"), ordinal = 13, remap = false)
    private int mountAlignment(int x) {
        if (VanillaHUD.isApec()) {
            return x;
        }
        boolean alignment = Hunger.mountHud.isEnabled() ? Hunger.mountHud.alignment : Hunger.hud.alignment;
        return alignment ? 81 + x : -(x + 9);
    }

    @Redirect(method = "renderHealthMount", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V"))
    private void mountFlip(GuiIngameForge instance, int x, int y, int textureX, int textureY, int width, int height) {
        if (VanillaHUD.isApec()) {
            instance.drawTexturedModalRect(x, y, textureX, textureY, width, height);
            return;
        }
        if (textureX < 97 || ((textureX - 16) / 9) % 2 == 0 || getMountHud().alignment) {
            instance.drawTexturedModalRect(x, y, textureX, textureY, width, height);
        } else {
            Gui.drawScaledCustomSizeModalRect(x, y, textureX + 9, textureY, -9, 9, 9, 9, 256, 256);
        }
    }

    @Redirect(method = "renderHealthMount", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getRenderViewEntity()Lnet/minecraft/entity/Entity;"))
    private Entity mountEdit(Minecraft instance) {
        if (VanillaHUD.isApec()) {
            return instance.getRenderViewEntity();
        }
        return HudCore.editing ? instance.thePlayer : instance.getRenderViewEntity();
    }

    @ModifyConstant(method = "renderHealthMount", constant = @Constant(intValue = 10, ordinal = 1), remap = false)
    private int mountMode(int constant) {
        if (VanillaHUD.isApec()) {
            return constant;
        }
        if (HudCore.editing) return 0;
        return (Hunger.mode ? 1 : -1) * 10;
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;shouldDrawHUD()Z"))
    private boolean example(PlayerControllerMP instance) {
        return instance.shouldDrawHUD() || (HudCore.editing && !VanillaHUD.isApec());
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getRenderViewEntity()Lnet/minecraft/entity/Entity;"))
    private Entity example(Minecraft instance) {
        return (HudCore.editing && !VanillaHUD.isApec()) ? instance.thePlayer : instance.getRenderViewEntity();
    }


    @ModifyArgs(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderHealthMount(II)V"))
    private void mountHealth(Args args) {
        if (VanillaHUD.isApec()) {
            return;
        }
        args.set(0, -182);
        args.set(1, right_height);
    }

    private boolean needsToResetMount = false;

    @Inject(method = "renderHealthMount", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V"), cancellable = true)
    private void mount(CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        if (!(renderHealthMount || Hunger.mountHud.isEnabled())) {
            ci.cancel();
            return;
        }
        Hud hud = getMountHud();
        boolean healthLink = Hunger.mountHud.isEnabled() ? Hunger.MountHud.healthLink : Hunger.HungerHud.healthLink;
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) hud.position.getX(), (int) hud.position.getY() - (healthLink ? healthLink() : 0), 0F);
        GlStateManager.scale(hud.getScale(), hud.getScale(), 1F);
        needsToResetMount = true;
    }

    @Inject(method = "renderHealthMount", at = @At("RETURN"), remap = false)
    private void mountReturn(CallbackInfo ci) {
        if (needsToResetMount) {
            GlStateManager.popMatrix();
            needsToResetMount = false;
        }
    }

    @Inject(method = "renderHealthMount", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;post(Lnet/minecraftforge/client/event/RenderGameOverlayEvent$ElementType;)V"), remap = false)
    private void mountPop(CallbackInfo ci) {
        if (needsToResetMount) {
            GlStateManager.popMatrix();
            needsToResetMount = false;
        }
    }

    @ModifyArgs(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderJumpBar(II)V"))
    private void jump(Args args) {
        if (VanillaHUD.isApec()) {
            return;
        }
        args.set(0, 182);
        args.set(1, 29);
    }

    private boolean needsToResetJump = false;

    @Inject(method = "renderJumpBar", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V", ordinal = 0), remap = false)
    private void jump(CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) Experience.hud.position.getX(), (int) Experience.hud.position.getY(), 0F);
        GlStateManager.scale(Experience.hud.getScale(), Experience.hud.getScale(), 1F);
        needsToResetJump = true;
    }

    @Inject(method = "renderJumpBar", at = @At("RETURN"), remap = false)
    private void jumpReturn(CallbackInfo ci) {
        if (needsToResetJump) {
            GlStateManager.popMatrix();
            needsToResetJump = false;
        }
    }

    @Inject(method = "renderJumpBar", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;post(Lnet/minecraftforge/client/event/RenderGameOverlayEvent$ElementType;)V"), remap = false)
    private void jumpPop(CallbackInfo ci) {
        if (needsToResetJump) {
            GlStateManager.popMatrix();
            needsToResetJump = false;
        }
    }

    @Inject(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderPlayerList(II)V"))
    private void setCheck(float partialTicks, CallbackInfo ci) {
        TabList.isGuiIngame = true;
    }

    @Unique
    private boolean toggled, isPressed;

    @Redirect(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z"))
    private boolean tabExample(KeyBinding instance) {
        if (VanillaHUD.isCompactTab() || !ModConfig.tab.enabled) {
            return instance.isKeyDown();
        }
        ScoreObjective scoreobjective = mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(0);
        NetHandlerPlayClient handler = mc.thePlayer.sendQueue;

        if (!mc.isIntegratedServerRunning() || handler.getPlayerInfoMap().size() > 1 || scoreobjective != null || HudCore.editing) {
            TabHook.gettingSize = true;
            mc.ingameGUI.getTabList().renderPlayerlist(UResolution.getScaledWidth(), mc.theWorld.getScoreboard(), scoreobjective);
        } else {
            return toggled = false;
        }

        boolean down = instance.isKeyDown();
        if (TabList.TabHud.displayMode) {
            if (down != isPressed && (isPressed = down)) {
                toggled = !toggled;
            }
        } else {
            toggled = down;
        }

        TabList.hud.drawBG(toggled || HudCore.editing);

        return (toggled || !TabList.animation.isFinished() || HudCore.editing) && TabList.hud.shouldRender();
    }

    @Redirect(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isIntegratedServerRunning()Z"))
    private boolean tabExample2(Minecraft instance) {
        return instance.isIntegratedServerRunning() && !HudCore.editing;
    }

    @Inject(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;renderPlayerlist(ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreObjective;)V"))
    private void enableScissor(int width, int height, CallbackInfo ci) {
        if (HudCore.editing || VanillaHUD.isCompactTab()) return;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        Position position = TabList.hud.position;
        int scale = (int) UResolution.getScaleFactor();
        GL11.glScissor((int) (position.getX() * scale), (int) ((UResolution.getScaledHeight() - position.getY() - TabList.animation.get()) * scale), (int) (position.getWidth() * scale), (int) (TabList.animation.get() * scale));
    }

    @Inject(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;renderPlayerlist(ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreObjective;)V", shift = At.Shift.AFTER))
    private void disable(int width, int height, CallbackInfo ci) {
        if (HudCore.editing || VanillaHUD.isCompactTab()) return;
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Inject(method = "renderGameOverlay", at = @At("HEAD"))
    private void head(float partialTicks, CallbackInfo ci) {
        ScoreboardHook.canDraw = false;
    }

}
