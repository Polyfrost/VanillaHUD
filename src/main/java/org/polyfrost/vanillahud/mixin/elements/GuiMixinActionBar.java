package org.polyfrost.vanillahud.mixin.elements;

//? if = 1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GameGui;
import org.polyfrost.vanillahud.compat.LegacyDrawContext;
import org.polyfrost.vanillahud.render.HudTransform;
import org.polyfrost.vanillahud.render.TextBackdrop;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}

//? if > 1.8.9 {
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.polyfrost.vanillahud.hook.TextShadowHook;
import org.polyfrost.vanillahud.render.TextBackdrop;
//?}
import org.polyfrost.vanillahud.hud.ActionBarHud;
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

    @WrapOperation(
            //? if >=26 {
            method = "extractOverlayMessage",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;textWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V")
            //?} elif >=1.21.8 {
            /*method = "renderOverlayMessage",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V")
            *///?} else {
            /*method = "renderOverlayMessage",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)I")
            *///?}
    )
    //? if >=1.21.8 {
    private void vanillahud$actionBarText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y,
                                      int width, int color, Operation<Void> original) {
    //?} else
    //private int vanillahud$actionBarText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int width, int color, Operation<Integer> original) {
        ActionBarHud hud = Huds.INSTANCE.getActionBar();
        TextBackdrop.draw(graphics, hud, x, y, width, 9, color);
        TextShadowHook.push(hud.getTextShadow());
        try {
            //? if >=1.21.8 {
            original.call(graphics, font, text, x, y, width, color);
            //?} else
            //return original.call(graphics, font, text, x, y, width, color);
        } finally {
            TextShadowHook.pop();
        }
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
        ActionBarHud hud = Huds.INSTANCE.getActionBar();
        if (!hud.shouldDraw()) return 0;
        TextBackdrop.draw(LegacyDrawContext.INSTANCE, hud, x, y, font.width(text), 9, color);
        return font.draw(text, (float) x, (float) y, color, hud.getTextShadow());
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/gui/GameGui;overlayMessageTinted:Z", opcode = Opcodes.GETFIELD))
    private boolean vanillahud$actionBar$rainbowTimer(boolean original) {
        return original && Huds.INSTANCE.getActionBar().getRainbowTimer();
    }
    *///?}
}
