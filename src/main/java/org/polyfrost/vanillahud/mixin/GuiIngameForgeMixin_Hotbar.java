package org.polyfrost.vanillahud.mixin;

import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import org.polyfrost.vanillahud.hud.*;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static org.polyfrost.vanillahud.hud.Hunger.getMountHud;

@Mixin(value = GuiIngameForge.class, remap = false, priority = 9000)
public abstract class GuiIngameForgeMixin_Hotbar {

    @Shadow
    public static int left_height;

    @Shadow public static int right_height;

    @Shadow public static boolean renderFood;

    @Shadow protected abstract void renderExperience(int width, int height);

    @Shadow protected abstract void renderArmor(int width, int height);

    @Shadow protected abstract void renderAir(int width, int height);

    @Shadow public abstract void renderFood(int width, int height);

    @Shadow public abstract void renderHealth(int width, int height);

    @Shadow protected abstract void renderHealthMount(int width, int height);

    @Shadow protected abstract void renderJumpBar(int width, int height);

    @Shadow public static boolean renderHealthMount;

    @Unique
    @Exclude
    private final Minecraft mc = Minecraft.getMinecraft();

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;shouldDrawHUD()Z"))
    private boolean example(PlayerControllerMP instance) {
        return instance.shouldDrawHUD() || HudCore.editing;
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getRenderViewEntity()Lnet/minecraft/entity/Entity;"))
    private Entity example(Minecraft instance) {
        return HudCore.editing ? instance.thePlayer : instance.getRenderViewEntity();
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderHealth(II)V"))
    private void health(GuiIngameForge instance, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) Health.hud.position.getX(), (int) Health.hud.position.getY() - (Health.HealthHud.mountLink ? mountLink() : 0), 0F);
        GlStateManager.scale(Health.hud.getScale(), Health.hud.getScale(), 1F);
        renderHealth(182, left_height);
        GlStateManager.popMatrix();
    }

    @Redirect(method = "renderHealth", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V"))
    private void healthFlip(GuiIngameForge instance, int x, int y, int textureX, int textureY, int width, int height) {
        if (textureX < 61 || ((textureX - 16) / 9) % 2 == 0 || !Health.hud.alignment) {
            instance.drawTexturedModalRect(x, y, textureX, textureY, width, height);
        } else {
            Gui.drawScaledCustomSizeModalRect(x, y, textureX + 9, textureY, -9, 9, 9, 9, 256, 256);
        }
    }

    @ModifyVariable(method = "renderHealth", at = @At("STORE"), ordinal = 14)
    private int healthAlignment(int x) {
        return Health.hud.alignment ? 72 - x : x;
    }

    @ModifyVariable(method = "renderHealth", at = @At("STORE"), ordinal = 15)
    private int healthMode(int y) {
        if (HudCore.editing) return 0;
        if (Health.HealthHud.mode) return y;
        return - y;
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderFood(II)V"))
    private void hunger(GuiIngameForge instance, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) Hunger.hud.position.getX(), (int) Hunger.hud.position.getY() - (Hunger.HungerHud.healthLink ? healthLink() : 0) - (Hunger.HungerHud.mountLink ? mountLink() : 0), 0F);
        GlStateManager.scale(Hunger.hud.getScale(), Hunger.hud.getScale(), 1F);
        renderFood(-182, right_height);
        GlStateManager.popMatrix();
    }

    @ModifyVariable(method = "renderFood", at = @At("STORE"), ordinal = 8)
    private int hungerAlignment(int x) {
        return Hunger.hud.alignment ? 81 + x : - (x + 9);
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "FIELD", target = "Lnet/minecraftforge/client/GuiIngameForge;renderFood:Z", ordinal = 1))
    private boolean hungerRender() {
        return renderFood || Hunger.mountHud.isEnabled();
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderHealthMount(II)V"))
    private void mountHealth(GuiIngameForge instance, int i, int top) {
        Hud hud = getMountHud();
        boolean healthLink = Hunger.mountHud.isEnabled() ? Hunger.MountHud.healthLink : Hunger.HungerHud.healthLink;
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) hud.position.getX(), (int) hud.position.getY() - (healthLink ? healthLink() : 0), 0F);
        GlStateManager.scale(hud.getScale(), hud.getScale(), 1F);
        renderHealthMount(-182, right_height);
        GlStateManager.popMatrix();
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "FIELD", target = "Lnet/minecraftforge/client/GuiIngameForge;renderHealthMount:Z", ordinal = 1))
    private boolean mountRender() {
        return renderHealthMount || Hunger.mountHud.isEnabled();
    }

    @Redirect(method = "renderHealthMount", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;ridingEntity:Lnet/minecraft/entity/Entity;"))
    private Entity mountExample(EntityPlayer instance) {
        Entity entity = instance.ridingEntity;

        return (entity instanceof EntityLivingBase) ? entity : HudCore.editing ? instance : null;
    }

    @Dynamic
    @ModifyVariable(method = "renderHealthMount", at = @At("STORE"), ordinal = 13)
    private int mountAlignment(int x) {
        boolean alignment = Hunger.mountHud.isEnabled() ? Hunger.mountHud.alignment : Hunger.hud.alignment;
        return alignment ? 81 + x : - (x + 9);
    }

    @Redirect(method = "renderHealthMount", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V"))
    private void mountFlip(GuiIngameForge instance, int x, int y, int textureX, int textureY, int width, int height) {
        if (textureX < 97 || ((textureX - 16) / 9) % 2 == 0 || getMountHud().alignment) {
            instance.drawTexturedModalRect(x, y, textureX, textureY, width, height);
        } else {
            Gui.drawScaledCustomSizeModalRect(x, y, textureX + 9, textureY, -9, 9, 9, 9, 256, 256);
        }
    }

    @ModifyConstant(method = "renderHealthMount", constant = @Constant(intValue = 10, ordinal = 1))
    private int mountMode(int constant) {
        if (HudCore.editing) return 0;
        return (Hunger.mode ? 1 : -1) * 10;
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderArmor(II)V"))
    private void armor(GuiIngameForge instance, int width, int height) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) Armor.hud.position.getX(), (int) Armor.hud.position.getY() - (Armor.ArmorHud.healthLink ? healthLink() : 0) - (Armor.ArmorHud.mountLink ? mountLink() : 0), 0F);
        GlStateManager.scale(Armor.hud.getScale(), Armor.hud.getScale(), 1F);
        renderArmor(182, left_height);
        GlStateManager.popMatrix();
    }

    @Redirect(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;getTotalArmorValue(Lnet/minecraft/entity/player/EntityPlayer;)I"))
    private int armorExample(EntityPlayer player) {
        int value = player.getTotalArmorValue();
        return HudCore.editing ? value > 0 ? value : 10 : value;
    }

    @Redirect(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V"))
    private void armorFlip(GuiIngameForge instance, int x, int y, int textureX, int textureY, int width, int height) {
        int left = Armor.hud.alignment ? 72 - x : x;
        if (Armor.hud.alignment && textureX == 25) {
            Gui.drawScaledCustomSizeModalRect(left, y, textureX + 9, textureY, -9, 9, 9, 9, 256, 256);
        } else {
            instance.drawTexturedModalRect(left, y, textureX, textureY, width, height);
        }
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderAir(II)V"))
    private void air(GuiIngameForge instance, int width, int height) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) Air.hud.position.getX(), (int) Air.hud.position.getY() - (Air.AirHud.healthLink ? healthLink() : 0) - (Air.AirHud.mountLink ? mountLink() : 0), 0F);
        GlStateManager.scale(Air.hud.getScale(), Air.hud.getScale(), 1F);
        renderAir(-182, right_height);
        GlStateManager.popMatrix();
    }

    @Redirect(method = "renderAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isInsideOfMaterial(Lnet/minecraft/block/material/Material;)Z"))
    private boolean airExample(EntityPlayer instance, Material material) {
        return instance.isInsideOfMaterial(material) || HudCore.editing;
    }

    @ModifyArgs(method = "renderAir", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V"))
    private void airAlignment(Args args) {
        int left = (int) args.get(0) + (Air.hud.alignment ? 81 : 9);
        args.set(0, Air.hud.alignment ? left : -left);
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderJumpBar(II)V"))
    private void jump(GuiIngameForge instance, int width, int height) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) Experience.hud.position.getX(), (int) Experience.hud.position.getY(), 0F);
        GlStateManager.scale(Experience.hud.getScale(), Experience.hud.getScale(), 1F);
        renderJumpBar(182, 29);
        GlStateManager.popMatrix();
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderExperience(II)V"))
    private void exp(GuiIngameForge instance, int width, int height) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) Experience.hud.position.getX(), (int) Experience.hud.position.getY(), 0F);
        GlStateManager.scale(Experience.hud.getScale(), Experience.hud.getScale(), 1F);
        renderExperience(182, 29);
        GlStateManager.popMatrix();
    }

    @Redirect(method = "renderExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;gameIsSurvivalOrAdventure()Z"))
    private boolean expExample(PlayerControllerMP instance) {
        return HudCore.editing || instance.gameIsSurvivalOrAdventure();
    }

    @Unique
    private int healthLink() {
        IAttributeInstance attrMaxHealth = mc.thePlayer.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        float healthMax = (float)attrMaxHealth.getAttributeValue();
        float absorb = mc.thePlayer.getAbsorptionAmount();
        int healthRows = MathHelper.ceiling_float_int((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);
        int height = healthRows * rowHeight;
        if (rowHeight != 10) height += 10 - rowHeight;
        return HudCore.editing ? 0 : (int) ((height - 10) * (Health.HealthHud.mode ? 1 : -1) * Health.hud.getScale());
    }

    @Unique
    private int mountLink() {
        EntityPlayer player = (EntityPlayer) mc.getRenderViewEntity();
        Entity tmp = player.ridingEntity;
        if (!(tmp instanceof EntityLivingBase)) return 0;
        EntityLivingBase mount = (EntityLivingBase) tmp;
        int hearts = (int) (mount.getMaxHealth() + 0.5F) / 2;
        if (hearts > 30) hearts = 30;
        int rows = MathHelper.ceiling_float_int(hearts / 10f) - 1;
        HudBar hud = getMountHud();
        return HudCore.editing ? 0 : (int) (rows * 10 * (Hunger.mode ? 1 : -1) * hud.getScale());
    }
}
