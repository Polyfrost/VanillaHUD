package org.polyfrost.vanillahud.mixin.elements;

//? if > 1.8.9
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
//? if > 1.8.9
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if > 1.8.9
import net.minecraft.network.chat.Component;
import org.polyfrost.vanillahud.hud.Huds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if = 1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import org.polyfrost.vanillahud.compat.LegacyDrawContext;
import org.polyfrost.vanillahud.render.HudTransform;
*///?}

//? if <1.21.4 && >1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
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
public class GuiMixinTitle {
    //? if > 1.8.9 {
    @Shadow private Component title;
    @Shadow private Component subtitle;

    //? if <1.21.4 && >1.8.9 {
    /*@WrapMethod(method = "renderTitle")
    private void vanillahud$title(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (!Huds.INSTANCE.getTitle().shouldDraw()) return;

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
    //?} else {
    /*@Shadow private String title;
    @Shadow private String subtitle;

    // The title is drawn inline in render rather than in a method of its own, so the element
    // transform brackets the region: it opens on the title block's own pushMatrix and closes
    // after the matching popMatrix. Verified against the 1.8.9 bytecode.
    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/platform/GlStateManager;pushMatrix()V", ordinal = 1))
    private void vanillahud$titleBegin(float tickDelta, CallbackInfo ci) {
        HudTransform.begin(LegacyDrawContext.INSTANCE, Huds.INSTANCE.getTitle());
    }

    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/platform/GlStateManager;popMatrix()V", ordinal = 3,
            shift = At.Shift.AFTER))
    private void vanillahud$titleEnd(float tickDelta, CallbackInfo ci) {
        HudTransform.end(LegacyDrawContext.INSTANCE);
    }

    // a bracketed region cannot be cancelled, so hiding the element skips its two text draws
    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;draw(Ljava/lang/String;FFIZ)I"))
    private int vanillahud$titleDraw(Font font, String text, float x, float y, int color, boolean shadow,
                                     Operation<Integer> original) {
        if (!Huds.INSTANCE.getTitle().shouldDraw()) return 0;
        return original.call(font, text, x, y, color, shadow);
    }

    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/platform/GlStateManager;scalef(FFF)V", ordinal = 0,
            shift = At.Shift.AFTER))
    private void vanillahud$titleScale(float tickDelta, CallbackInfo ci) {
        vanillahud$autoScale(title, 4.0F);
    }

    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/platform/GlStateManager;scalef(FFF)V", ordinal = 1,
            shift = At.Shift.AFTER))
    private void vanillahud$subtitleScale(float tickDelta, CallbackInfo ci) {
        vanillahud$autoScale(subtitle, 2.0F);
    }

    @Unique
    private void vanillahud$autoScale(String text, float drawScale) {
        if (!Huds.INSTANCE.getTitle().getAutoTitleScale()) return;
        if (!vanillahud$isNormalTitle(text)) return;

        float width = Minecraft.getInstance().font.width(text) * drawScale + 50.0F;
        int guiWidth = LegacyDrawContext.INSTANCE.guiWidth();
        if (width <= (float) guiWidth) return;

        float scale = (float) guiWidth / width;
        LegacyDrawContext.INSTANCE.pose().scale(scale, scale);
    }

    @Unique
    private boolean vanillahud$isNormalTitle(String string) {
        if (string == null || string.isEmpty()) return true;

        char first = string.charAt(0);
        boolean isCustomIcon = first >= '\uE000' && first <= '\uF8FF';
        return !(isCustomIcon && string.length() <= 2);
    }
    *///?}
}
