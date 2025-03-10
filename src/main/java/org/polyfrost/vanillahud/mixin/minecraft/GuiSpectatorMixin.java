package org.polyfrost.vanillahud.mixin.minecraft;

import dev.deftu.omnicore.client.render.OmniResolution;
import org.polyfrost.universal.UResolution;
import net.minecraft.client.gui.GuiSpectator;
import net.minecraft.client.gui.spectator.ISpectatorMenuObject;
import net.minecraft.client.renderer.GlStateManager;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.hud.Hotbar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GuiSpectator.class)
public abstract class GuiSpectatorMixin {

    @Shadow protected abstract void func_175266_a(int i, int j, float f, float g, ISpectatorMenuObject iSpectatorMenuObject);

    @ModifyArg(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiSpectator;func_175258_a(Lnet/minecraft/client/gui/ScaledResolution;FIFLnet/minecraft/client/gui/spectator/categories/SpectatorDetails;)V"), index = 3)
    private float y(float f) {
        if (VanillaHUD.isApec()) {
            return f;
        }
        if (!Hotbar.hud.isEnabled()) return f;
        return OmniResolution.getScaledHeight() - 22;
    }

    @ModifyArgs(method = "func_175258_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiSpectator;drawTexturedModalRect(FFIIII)V"))
    private void setPosition(Args args) {
        if (VanillaHUD.isApec()) {
            return;
        }
        if (!Hotbar.hud.isEnabled()) return;
        int x = OmniResolution.getScaledWidth() / 2 - 91;
        int y = OmniResolution.getScaledHeight() - 22;
        args.set(0, ((float) args.get(0)) - x);
        args.set(1, ((float) args.get(1)) - y);
    }

    @Inject(method = "func_175258_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiSpectator;drawTexturedModalRect(FFIIII)V", ordinal = 0))
    private void set(CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(Hotbar.hud.position.getX(), Hotbar.hud.position.getY(), 0f);
        GlStateManager.scale(Hotbar.hud.getScale(), Hotbar.hud.getScale(), 1f);
    }

    @Inject(method = "func_175258_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderHelper;disableStandardItemLighting()V"))
    private void pop(CallbackInfo ci) {
        if (VanillaHUD.isApec()) {
            return;
        }
        GlStateManager.popMatrix();
    }

    @Redirect(method = "func_175258_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiSpectator;func_175266_a(IIFFLnet/minecraft/client/gui/spectator/ISpectatorMenuObject;)V"))
    private void icon(GuiSpectator instance, int i, int j, float f, float g, ISpectatorMenuObject iSpectatorMenuObject) {
        if (VanillaHUD.isApec()) {
            func_175266_a(i, j, f, g, iSpectatorMenuObject);
            return;
        }
        func_175266_a(i, i * 20 + 3, 3f, g, iSpectatorMenuObject);
    }

}
