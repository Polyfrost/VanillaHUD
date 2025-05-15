package org.polyfrost.vanillahud.mixin.appleskin;

import org.polyfrost.vanillahud.VanillaHUD;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Pseudo
@Mixin(targets = "squeek.applecore.client.HUDOverlayHandler", priority = 1500, remap = false)
public class HUDOverlayHandlerMixin {

    @Dynamic("Apple Skin")
    @ModifyVariable(method = "onRender", at = @At("STORE"), ordinal = 0)
    private int x(int old) {
        return 0;
    }

    @Dynamic("Apple Skin")
    @ModifyVariable(method = "onRender", at = @At("STORE"), ordinal = 1)
    private int y(int old) {
        return 0;
    }

    @Dynamic("Apple Skin")
    @ModifyVariable(method = "onPreRender", at = @At("STORE"), ordinal = 0)
    private int preX(int old) {
        return 0;
    }

    @Dynamic("Apple Skin")
    @ModifyVariable(method = "onPreRender", at = @At("STORE"), ordinal = 1)
    private int preY(int old) {
        return 0;
    }

    @Dynamic("Apple Skin")
    @ModifyVariable(method = "drawSaturationOverlay", at = @At("STORE"), ordinal = 6)
    private static int Saturation(int x) {
        return VanillaHUD.getHunger().getAlignment() == 0 ? 81 + x : - (x + 9);
    }

    @Dynamic("Apple Skin")
    @ModifyVariable(method = "drawHungerOverlay", at = @At("STORE"), ordinal = 9)
    private static int overlay(int x) {
        return VanillaHUD.getHunger().getAlignment() == 0 ? 81 + x : - (x + 9);
    }

    @Dynamic("Apple Skin")
    @ModifyArgs(method = "drawExhaustionOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;func_73729_b(IIIIII)V"))
    private static void modify(Args args) {
        int width = args.get(4);
        int x = VanillaHUD.getHunger().getAlignment() == 0 ? 81 - width : 0;
        args.set(4, Math.min(width, 81));
        args.set(0, x);
    }
}
