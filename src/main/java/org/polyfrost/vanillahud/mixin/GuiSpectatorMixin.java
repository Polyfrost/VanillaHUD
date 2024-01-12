package org.polyfrost.vanillahud.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSpectator;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.polyfrost.vanillahud.hud.Hotbar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GuiSpectator.class)
public abstract class GuiSpectatorMixin {

    @ModifyArgs(method = "func_175258_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiSpectator;func_175266_a(IIFFLnet/minecraft/client/gui/spectator/ISpectatorMenuObject;)V"))
    private void cancel(Args args) {
        int j = args.get(0);
        if (Hotbar.hud.isEnabled()) args.set(1, (int) (Hotbar.hud.position.getX() / Hotbar.hud.getScale() + j * 20 + 3));
    }

    @ModifyArgs(method = "func_175258_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiSpectator;drawTexturedModalRect(FFIIII)V"))
    private void setPosition(Args args) {
        if (!Hotbar.hud.isEnabled()) return;
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int x = sr.getScaledWidth() / 2 - 91;
        int y = sr.getScaledHeight() - 22;
        args.set(0, ((float) args.get(0)) - x);
        args.set(1, ((float) args.get(1)) - y);
    }

    @Inject(method = "func_175258_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiSpectator;drawTexturedModalRect(FFIIII)V", ordinal = 0))
    private void set(CallbackInfo ci) {
        if (!Hotbar.hud.isEnabled()) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(Hotbar.hud.position.getX(), Hotbar.hud.position.getY(), 0f);
        GlStateManager.scale(Hotbar.hud.getScale(), Hotbar.hud.getScale(), 1f);
    }

    @Inject(method = "func_175258_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderHelper;disableStandardItemLighting()V"))
    private void pop(CallbackInfo ci) {
        if (!Hotbar.hud.isEnabled()) return;

        GlStateManager.popMatrix();
    }

    @ModifyArgs(method = "func_175258_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiSpectator;func_175266_a(IIFFLnet/minecraft/client/gui/spectator/ISpectatorMenuObject;)V"))
    private void iconPosition(Args args) {
        if (!Hotbar.hud.isEnabled()) return;

        int index = args.get(0);
        args.set(1, index * 20 +3);
        args.set(2, 3F);
    }

}
