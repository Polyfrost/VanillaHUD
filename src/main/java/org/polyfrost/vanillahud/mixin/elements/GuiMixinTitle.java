package org.polyfrost.vanillahud.mixin.elements;

//? if > 1.8.9
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
//? if > 1.8.9
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if > 1.8.9
import net.minecraft.network.chat.Component;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import org.polyfrost.vanillahud.hook.TextShadowHook;
import org.polyfrost.vanillahud.hud.BackdropHud;
import org.polyfrost.vanillahud.hud.TitleHud;
import org.polyfrost.vanillahud.render.TextBackdrop;
import org.polyfrost.vanillahud.hud.Huds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if = 1.8.9 {
/*import org.polyfrost.vanillahud.compat.LegacyDrawContext;
import org.polyfrost.vanillahud.render.HudTransform;
import org.polyfrost.vanillahud.hud.SubtitleHud;
import net.minecraft.client.render.platform.GlStateManager;
*///?}

//? if <1.21.4 && >1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
*///?}

//? if > 1.8.9
import org.polyfrost.vanillahud.render.TitlePass;

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
        TitlePass.draw(graphics, () -> original.call(graphics, deltaTracker));
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
        TitleHud hud = Huds.INSTANCE.getTitle();
        vanillahud$customScale(graphics, hud.getTitleScale() / 4.0F);
        vanillahud$autoScale(graphics, title, hud.getTitleScale(), hud.getAutoTitleScale());
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
        float scale = Huds.INSTANCE.getSubtitle().getSubtitleScale();
        vanillahud$customScale(graphics, scale / 2.0F);
        vanillahud$autoScale(graphics, subtitle, scale, Huds.INSTANCE.getSubtitle().getAutoTitleScale());
    }

    @WrapOperation(
            //? if >=26 {
            method = "extractTitle",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;textWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V", ordinal = 0)
            //?} elif >=1.21.8 {
            /*method = "renderTitle",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V", ordinal = 0)
            *///?} else {
            /*method = "renderTitle",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)I", ordinal = 0)
            *///?}
    )
    //? if >=1.21.8 {
    private void vanillahud$titleText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y,
                                      int width, int color, Operation<Void> original) {
    //?} else
    //private int vanillahud$titleText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int width, int color, Operation<Integer> original) {
        if (TitlePass.getCurrent() == TitlePass.SUBTITLE) {
            //? if >=1.21.8 {
            return;
            //?} else
            //return 0;
        }
        BackdropHud hud = Huds.INSTANCE.getTitle();
        if (Huds.INSTANCE.getTitle().getInstantFade()) color |= 0xFF000000;
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

    @WrapOperation(
            //? if >=26 {
            method = "extractTitle",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;textWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V", ordinal = 1)
            //?} elif >=1.21.8 {
            /*method = "renderTitle",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V", ordinal = 1)
            *///?} else {
            /*method = "renderTitle",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)I", ordinal = 1)
            *///?}
    )
    //? if >=1.21.8 {
    private void vanillahud$subtitleText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y,
                                      int width, int color, Operation<Void> original) {
    //?} else
    //private int vanillahud$subtitleText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int width, int color, Operation<Integer> original) {
        if (TitlePass.getCurrent() == TitlePass.TITLE) {
            //? if >=1.21.8 {
            return;
            //?} else
            //return 0;
        }
        BackdropHud hud = Huds.INSTANCE.getSubtitle();
        if (Huds.INSTANCE.getSubtitle().getInstantFade()) color |= 0xFF000000;
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

    @Unique
    private void vanillahud$customScale(GuiGraphicsExtractor graphics, float scale) {
        if (scale == 1.0F) return;
        //? if >=1.21.6 {
        graphics.pose().scale(scale, scale);
        //?} else {
        /*graphics.pose().scale(scale, scale, 1.0F);
        *///?}
    }

    @Unique
    private void vanillahud$autoScale(GuiGraphicsExtractor graphics, Component text, float drawScale, boolean enabled) {
        if (!enabled) return;
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
        if (vanillahud$subtitleColor == null) return;
        int color = vanillahud$subtitleColor;
        vanillahud$subtitleColor = null;

        SubtitleHud hud = Huds.INSTANCE.getSubtitle();
        if (!hud.shouldDraw()) return;
        LegacyDrawContext ctx = LegacyDrawContext.INSTANCE;
        Font font = Minecraft.getInstance().font;
        HudTransform.begin(ctx, hud);
        ctx.pose().pushMatrix();
        ctx.pose().translate(ctx.guiWidth() / 2.0F, ctx.guiHeight() / 2.0F);
        ctx.pose().scale(2.0F, 2.0F);
        vanillahud$customScale(hud.getSubtitleScale() / 2.0F);
        vanillahud$autoScale(subtitle, hud.getSubtitleScale(), hud.getAutoTitleScale());
        GlStateManager.enableBlend();
        int width = font.width(subtitle);
        if (hud.getInstantFade()) color |= 0xFF000000;
        TextBackdrop.draw(ctx, hud, -width / 2, 5, width, 9, color);
        font.draw(subtitle, (float) (-width / 2), 5.0F, color, hud.getTextShadow());
        ctx.pose().popMatrix();
        HudTransform.end(ctx);
    }

    @Unique private Integer vanillahud$subtitleColor;

    // the subtitle has its own element transform, so it is drawn after the title region closes
    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;draw(Ljava/lang/String;FFIZ)I"))
    private int vanillahud$titleDraw(Font font, String text, float x, float y, int color, boolean shadow,
                                     Operation<Integer> original) {
        if (text == this.subtitle) {
            vanillahud$subtitleColor = color;
            return 0;
        }
        TitleHud hud = Huds.INSTANCE.getTitle();
        if (!hud.shouldDraw()) return 0;
        if (hud.getInstantFade()) color |= 0xFF000000;
        TextBackdrop.draw(LegacyDrawContext.INSTANCE, hud, (int) x, (int) y, font.width(text), 9, color);
        return original.call(font, text, x, y, color, hud.getTextShadow());
    }

    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/platform/GlStateManager;scalef(FFF)V", ordinal = 0,
            shift = At.Shift.AFTER))
    private void vanillahud$titleScale(float tickDelta, CallbackInfo ci) {
        TitleHud hud = Huds.INSTANCE.getTitle();
        vanillahud$customScale(hud.getTitleScale() / 4.0F);
        vanillahud$autoScale(title, hud.getTitleScale(), hud.getAutoTitleScale());
    }

    @Unique
    private void vanillahud$customScale(float scale) {
        if (scale != 1.0F) LegacyDrawContext.INSTANCE.pose().scale(scale, scale);
    }

    @Unique
    private void vanillahud$autoScale(String text, float drawScale, boolean enabled) {
        if (!enabled) return;
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
