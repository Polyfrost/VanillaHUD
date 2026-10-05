package org.polyfrost.vanillahud.mixin.elements;

//? if = 1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.render.Window;
import org.polyfrost.vanillahud.compat.LegacyDrawContext;
import org.polyfrost.vanillahud.hud.HotbarHud;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.injection.At;
*///?}

import org.spongepowered.asm.mixin.Mixin;

//? if <=1.21.5 && >1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.vanillahud.hud.Huds;
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
public class GuiMixinExperienceBar {
    //? if > 1.8.9 {
    //? if <=1.21.5 && >1.8.9 {
    /*@WrapMethod(method = "renderExperienceBar")
    private void vanillahud$xpBar(GuiGraphicsExtractor graphics, int xpBarX, Operation<Void> original) {
        if (!Huds.INSTANCE.getHotbar().shouldDraw() || !Huds.INSTANCE.getHotbar().getExperienceBar()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getHotbar());
        original.call(graphics, xpBarX);
        HudTransform.end(graphics);
    }
    *///?}
    //?} else {
    /*// 1.8.9 draws the bar and the level number together in renderXpBar, and renderJumpBar
    // replaces it while riding, so both belong to the fused hotbar element.
    @WrapMethod(method = "renderXpBar")
    private void vanillahud$xpBar(Window window, int x, Operation<Void> original) {
        HotbarHud hud = Huds.INSTANCE.getHotbar();
        if (!hud.shouldDraw()) return;

        HudTransform.begin(LegacyDrawContext.INSTANCE, hud);
        original.call(window, x);
        HudTransform.end(LegacyDrawContext.INSTANCE);
    }

    @WrapMethod(method = "renderJumpBar")
    private void vanillahud$jumpBar(Window window, int x, Operation<Void> original) {
        HotbarHud hud = Huds.INSTANCE.getHotbar();
        if (!hud.shouldDraw()) return;

        HudTransform.begin(LegacyDrawContext.INSTANCE, hud);
        original.call(window, x);
        HudTransform.end(LegacyDrawContext.INSTANCE);
    }

    @WrapOperation(method = "renderXpBar", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GameGui;drawTexture(IIIIII)V"))
    private void vanillahud$xpBarSprite(GameGui self, int x, int y, int u, int v, int width, int height,
                                        Operation<Void> original) {
        if (Huds.INSTANCE.getHotbar().getExperienceBar()) original.call(self, x, y, u, v, width, height);
    }

    // the level is one block of text, so each of its draws is counter rotated to stay readable
    @WrapOperation(method = "renderXpBar", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;draw(Ljava/lang/String;III)I"))
    private int vanillahud$xpLevel(Font font, String text, int x, int y, int color,
                                   Operation<Integer> original) {
        if (!Huds.INSTANCE.getHotbar().getExperienceLevel()) return 0;
        HudTransform.beginUpright(LegacyDrawContext.INSTANCE, Huds.INSTANCE.getHotbar(),
                LegacyDrawContext.INSTANCE.guiWidth() / 2f,
                LegacyDrawContext.INSTANCE.guiHeight() - HotbarHud.LEVEL_CENTER_Y);
        int width = original.call(font, text, x, y, color);
        HudTransform.endUpright(LegacyDrawContext.INSTANCE);
        return width;
    }
    *///?}
}
