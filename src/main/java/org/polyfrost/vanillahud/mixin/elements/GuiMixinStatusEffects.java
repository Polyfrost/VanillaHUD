package org.polyfrost.vanillahud.mixin.elements;

//? if > 1.8.9 {
import org.spongepowered.asm.mixin.Mixin;

//? if <26 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.injection.At;
*///?}
//? if >=26 {
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.hud.StatusEffectsHud;
import org.spongepowered.asm.mixin.injection.At;
//?}
//? if >=1.21.8 {
import org.polyfrost.vanillahud.render.ForeignTransform;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} elif > 1.8.9 {
/*import net.minecraft.client.gui.Gui;
*///?} else {
/*import net.minecraft.client.gui.GameGui;
*///?}

//? if >=26.2 {
@Mixin(Hud.class)
//?} elif > 1.8.9 {
/*@Mixin(Gui.class)
*///?} else {
/*@Mixin(GameGui.class)
*///?}
public class GuiMixinStatusEffects {
    //? if >=1.21.8 {
    @Inject(
            //? if <26 {
            /*method = "renderEffects",
            *///?} else {
            method = "extractEffects",
            //?}
            at = @At("HEAD")
    )
    private void vanillahud$foreignProbe(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ForeignTransform.probe(graphics);
    }
    //?}

    //? if <26 {
    /*@WrapMethod(method = "renderEffects")
    private void vanillahud$statusEffects(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (!Huds.INSTANCE.getStatusEffects().shouldDraw()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getStatusEffects());
        original.call(graphics, deltaTracker);
        HudTransform.end(graphics);
    }
    *///?} else {
    @Inject(
            method = "extractEffects",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;isAmbient()Z")
    )
    private void vanillahud$shiftEffectRow(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci,
                                           @Local(name = "x") LocalIntRef x, @Local(name = "y") LocalIntRef y) {
        StatusEffectsHud hud = Huds.INSTANCE.getStatusEffects();
        if (hud.isDrawnAsVanilla() || !hud.shouldDraw()) return;

        int rowY = y.get();
        x.set(x.get() + hud.rowShiftX(rowY));
        y.set(rowY + hud.rowShiftY(rowY));
    }
    //?}
}
//?} else {
/*// 1.8.9 has no status effect icons on the HUD
import net.minecraft.client.gui.GameGui;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameGui.class)
public class GuiMixinStatusEffects {
}
*///?}
