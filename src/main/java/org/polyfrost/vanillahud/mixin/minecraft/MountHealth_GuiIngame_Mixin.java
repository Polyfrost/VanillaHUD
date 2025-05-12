package org.polyfrost.vanillahud.mixin.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.vanillahud.VanillaHUDOld;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.hud.hotbar.MountHealthHUD;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HEALTHMOUNT;

@Mixin(GuiIngameForge.class)
public abstract class MountHealth_GuiIngame_Mixin {
    @Shadow
    public static boolean renderHealth;

    @Shadow
    protected abstract void post(RenderGameOverlayEvent.ElementType type);

    @Shadow
    public static int right_height;

    @Redirect(method = "renderHealthMount", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;ridingEntity:Lnet/minecraft/entity/Entity;"))
    private Entity mountExample(EntityPlayer instance) {
        if (VanillaHUDOld.isApec()) {
            return instance.ridingEntity;
        }
        Entity entity = instance.ridingEntity;

        return (entity instanceof EntityLivingBase) ? entity : HudManager.INSTANCE.getPanelOpen() ? instance : null;
    }

    @Dynamic
    @ModifyVariable(method = "renderHealthMount", at = @At("STORE"), ordinal = 13, remap = false)
    private int mountAlignment(int x) {
        if (VanillaHUDOld.isApec()) {
            return x;
        }
        return VanillaHUD.getMount().getAlignment() == 1 ? 81 + x : -(x + 9);
    }

    @Redirect(method = "renderHealthMount", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V"))
    private void mountFlip(GuiIngameForge instance, int x, int y, int textureX, int textureY, int width, int height) {
        if (VanillaHUDOld.isApec()) {
            instance.drawTexturedModalRect(x, y, textureX, textureY, width, height);
            return;
        }
        if (textureX < 97 || ((textureX - 16) / 9) % 2 == 0 || VanillaHUD.getMount().getAlignment() == 1) {
            instance.drawTexturedModalRect(x, y, textureX, textureY, width, height);
        } else {
            Gui.drawScaledCustomSizeModalRect(x, y, textureX + 9, textureY, -9, 9, 9, 9, 256, 256);
        }
    }

    @Redirect(method = "renderHealthMount", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getRenderViewEntity()Lnet/minecraft/entity/Entity;"))
    private Entity mountEdit(Minecraft instance) {
        if (VanillaHUDOld.isApec()) {
            return instance.getRenderViewEntity();
        }
        return HudManager.INSTANCE.getPanelOpen() ? instance.thePlayer : instance.getRenderViewEntity();
    }

    @ModifyConstant(method = "renderHealthMount", constant = @Constant(intValue = 10, ordinal = 1), remap = false)
    private int mountMode(int constant) {
        if (VanillaHUDOld.isApec()) {
            return constant;
        }
        if (HudManager.INSTANCE.getPanelOpen()) return 0;
        return (VanillaHUD.getMount().getMode() == 1 ? 1 : -1) * 10;
    }

    @Inject(method = "renderHealthMount", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V"), cancellable = true)
    private void mount(CallbackInfo ci) {
        if (VanillaHUDOld.isApec()) {
            return;
        }
        MountHealthHUD hud = VanillaHUD.getMount();
        if (hud.getHidden()) {
            ci.cancel();
            post(HEALTHMOUNT);
            return;
        }
        float scale = hud.getScale();
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) hud.getX() + 182, (int) hud.getY() - (hud.getHealthLink() ? VanillaHUD.getHealthLinkAmount() : 0) - right_height, 0F);
        GlStateManager.scale(scale, scale, 1F);
    }

    @Inject(method = "renderHealthMount", at = @At("RETURN"), remap = false)
    private void mountReturn(CallbackInfo ci) {
        if (VanillaHUDOld.isApec()) {
            return;
        }
        GlStateManager.popMatrix();
    }
}
