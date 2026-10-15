package org.polyfrost.vanillahud.mixin.elements;

//? if = 1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GameGui;
import org.polyfrost.vanillahud.compat.LegacyDrawContext;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.objectweb.asm.Opcodes;
import org.polyfrost.vanillahud.hud.Huds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if <1.21.4 && >1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.vanillahud.render.HudTransform;
*///?}

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
public class GuiMixinActionBar {
    //? if > 1.8.9 {
    //? if <1.21.4 && >1.8.9 {
    /*@WrapMethod(method = "renderOverlayMessage")
    private void vanillahud$actionBar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (!Huds.INSTANCE.getActionBar().shouldDraw()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getActionBar());
        original.call(graphics, deltaTracker);
        HudTransform.end(graphics);
    }
    *///?}

    @ModifyExpressionValue(
            //? if < 26 {
            /*method = "renderOverlayMessage",
            *///?} else {
            method = "extractOverlayMessage",
            //?}
            at = @At(
                    value = "FIELD",
                    //? if >=26.2 {
                    target = "Lnet/minecraft/client/gui/Hud;animateOverlayMessageColor:Z",
                    //?} else {
                    /*target = "Lnet/minecraft/client/gui/Gui;animateOverlayMessageColor:Z",
                    *///?}
                    opcode = Opcodes.GETFIELD
            )
    )
    private boolean vanillahud$actionBar$rainbowTimer(boolean original) {
        return original && Huds.INSTANCE.getActionBar().getRainbowTimer();
    }
    //?} else {
    /*// The action bar is drawn inline in render, bracketed by the first pushMatrix and popMatrix
    // of the method. Verified against the 1.8.9 bytecode.
    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/platform/GlStateManager;pushMatrix()V", ordinal = 0))
    private void vanillahud$actionBarBegin(float tickDelta, CallbackInfo ci) {
        HudTransform.begin(LegacyDrawContext.INSTANCE, Huds.INSTANCE.getActionBar());
    }

    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/platform/GlStateManager;popMatrix()V", ordinal = 0,
            shift = At.Shift.AFTER))
    private void vanillahud$actionBarEnd(float tickDelta, CallbackInfo ci) {
        HudTransform.end(LegacyDrawContext.INSTANCE);
    }

    // a bracketed region cannot be cancelled, so hiding the element skips the text draw
    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;draw(Ljava/lang/String;III)I"))
    private int vanillahud$actionBarDraw(Font font, String text, int x, int y, int color,
                                         Operation<Integer> original) {
        if (!Huds.INSTANCE.getActionBar().shouldDraw()) return 0;
        // 1.8.9 draws the overlay message flat while every later version shadows it, and the
        // shadow is a separate draw call here rather than a flag on the draw context
        return font.drawWithShadow(text, (float) x, (float) y, color);
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/gui/GameGui;overlayMessageTinted:Z", opcode = Opcodes.GETFIELD))
    private boolean vanillahud$actionBar$rainbowTimer(boolean original) {
        return original && Huds.INSTANCE.getActionBar().getRainbowTimer();
    }
    *///?}
}
