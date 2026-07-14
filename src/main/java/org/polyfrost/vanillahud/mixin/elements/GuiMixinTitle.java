package org.polyfrost.vanillahud.mixin.elements;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.polyfrost.vanillahud.hud.Huds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if <1.21.4 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.polyfrost.vanillahud.render.HudTransform;
*///?}

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public class GuiMixinTitle {
    @Shadow private Component title;
    @Shadow private Component subtitle;

    //? if <1.21.4 {
    /*@WrapMethod(method = "renderTitle")
    private void vanillahud$title(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (!Huds.INSTANCE.getTitle().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getTitle());
        original.call(graphics, deltaTracker);
        HudTransform.end(graphics);
    }
    *///?}

    @Inject(
            //? if >=26 {
            method = "extractTitle",
            //?} else {
            /*method = "renderTitle",
            *///?}
            at = @At(value = "INVOKE",
                    //? if >=1.21.6 {
                    target = "Lorg/joml/Matrix3x2fStack;scale(FF)Lorg/joml/Matrix3x2f;",
                    //?} else {
                    /*target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V",
                    *///?}
                    ordinal = 0, shift = At.Shift.AFTER))
    private void vanillahud$titleScale(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        vanillahud$autoScale(graphics, title, 4.0F);
    }

    @Inject(
            //? if >=26 {
            method = "extractTitle",
            //?} else {
            /*method = "renderTitle",
            *///?}
            at = @At(value = "INVOKE",
                    //? if >=1.21.6 {
                    target = "Lorg/joml/Matrix3x2fStack;scale(FF)Lorg/joml/Matrix3x2f;",
                    //?} else {
                    /*target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V",
                    *///?}
                    ordinal = 1, shift = At.Shift.AFTER))
    private void vanillahud$subtitleScale(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        vanillahud$autoScale(graphics, subtitle, 2.0F);
    }

    @Unique
    private void vanillahud$autoScale(GuiGraphicsExtractor graphics, Component text, float drawScale) {
        if (!Huds.INSTANCE.getTitle().getAutoTitleScale()) return;
        if (!vanillahud$isNormalTitle(text)) return;

        float width = Minecraft.getInstance().font.width(text) * drawScale + 50.0F;
        if (width <= graphics.guiWidth()) return;

        float scale = graphics.guiWidth() / width;
        //? if >=1.21.6 {
        graphics.pose().scale(scale, scale);
        //?} else {
        /*graphics.pose().scale(scale, scale, 1.0F);
        *///?}
    }

    @Unique
    private boolean vanillahud$isNormalTitle(Component text) {
        String string = text.getString();
        if (string.isEmpty()) return true;

        char first = string.charAt(0);
        boolean isCustomIcon = first >= '\uE000' && first <= '\uF8FF';
        return !(isCustomIcon && string.length() <= 2);
    }
}
