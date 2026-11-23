package org.polyfrost.vanillahud.mixin.elements;

import org.spongepowered.asm.mixin.Mixin;

//? if > 1.8.9 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import org.polyfrost.vanillahud.mixin.access.IWindow;
import org.polyfrost.vanillahud.render.HudTransform;
import org.polyfrost.vanillahud.hud.StatBarHud;
import org.polyfrost.vanillahud.hud.Huds;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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
public class GuiMixinStatBars {
    //? if > 1.8.9 {
    //? if >=1.21.6 {
    private static final String BLIT = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V";
    private static final int X = 2;
    //?} elif >=1.21.4 {
    /*private static final String BLIT = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/Identifier;IIII)V";
    private static final int X = 2;
    *///?} else {
    /*private static final String BLIT = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lnet/minecraft/resources/Identifier;IIII)V";
    private static final int X = 1;
    *///?}

    @Unique
    private static final String VANILLAHUD$GRAPHICS =
            //? if >=26 {
            "Lnet/minecraft/client/gui/GuiGraphicsExtractor;";
            //?} else
            /*"Lnet/minecraft/client/gui/GuiGraphics;";*/
    @Unique
    private static final String VANILLAHUD$GUI_WIDTH = VANILLAHUD$GRAPHICS + "guiWidth()I";
    @Unique
    private static final String VANILLAHUD$GUI_HEIGHT = VANILLAHUD$GRAPHICS + "guiHeight()I";

    private static int vanillahud$centre() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2;
    }

    private static int vanillahud$left(int x, StatBarHud hud) {
        if (hud.isDrawnAsVanilla() || hud.getFillSide() == StatBarHud.ALIGN_LEFT) return x;
        return 2 * (vanillahud$centre() - 91) + 72 - x;
    }

    private static int vanillahud$right(int x, StatBarHud hud) {
        if (hud.isDrawnAsVanilla() || hud.getFillSide() == StatBarHud.ALIGN_RIGHT) return x;
        return 2 * (vanillahud$centre() + 91) - 90 - x;
    }

    //? if >=1.21.8 {
    @ModifyExpressionValue(
            method = {
                    //? if >=26 {
                    "extractPlayerHealth", "extractVehicleHealth"
                    //?} else
                    /*"renderPlayerHealth", "renderVehicleHealth"*/
            },
            at = @At(value = "INVOKE", target = VANILLAHUD$GUI_WIDTH)
    )
    private int vanillahud$barsWidth(int original) {
        return HudTransform.placesStatBars() ? ((IWindow) (Object) Minecraft.getInstance().getWindow()).realWidth() : original;
    }

    @ModifyExpressionValue(
            method = {
                    //? if >=26 {
                    "extractPlayerHealth", "extractVehicleHealth"
                    //?} else
                    /*"renderPlayerHealth", "renderVehicleHealth"*/
            },
            at = @At(value = "INVOKE", target = VANILLAHUD$GUI_HEIGHT)
    )
    private int vanillahud$barsHeight(int original) {
        return HudTransform.placesStatBars() ? ((IWindow) (Object) Minecraft.getInstance().getWindow()).realHeight() : original;
    }
    //?}

    @ModifyVariable(
            //? if >=26 {
            method = "extractHeart",
            //?} else
            /*method = "renderHeart",*/
            at = @At("HEAD"), argsOnly = true, ordinal = 0
    )
    private int vanillahud$heartX(int x) {
        return vanillahud$left(x, Huds.INSTANCE.getHealth());
    }

    @ModifyArg(
            //? if >=26 {
            method = "extractArmor",
            //?} else
            /*method = "renderArmor",*/
            at = @At(value = "INVOKE", target = BLIT), index = X
    )
    private static int vanillahud$armorX(int x) {
        return vanillahud$left(x, Huds.INSTANCE.getArmor());
    }

    @ModifyArg(
            //? if >=26 {
            method = "extractFood",
            //?} else
            /*method = "renderFood",*/
            at = @At(value = "INVOKE", target = BLIT), index = X
    )
    private int vanillahud$foodX(int x) {
        return vanillahud$right(x, Huds.INSTANCE.getHunger());
    }

    @ModifyArg(
            //? if >=26 {
            method = "extractAirBubbles",
            //?} elif >=1.21.4 {
            /*method = "renderAirBubbles",
            *///?} else
            /*method = "renderPlayerHealth",*/
            at = @At(value = "INVOKE", target = BLIT), index = X
    )
    private int vanillahud$airX(int x) {
        return vanillahud$right(x, Huds.INSTANCE.getAir());
    }

    @ModifyArg(
            //? if >=26 {
            method = "extractVehicleHealth",
            //?} else
            /*method = "renderVehicleHealth",*/
            at = @At(value = "INVOKE", target = BLIT), index = X
    )
    private int vanillahud$mountX(int x) {
        return vanillahud$right(x, Huds.INSTANCE.getHunger());
    }
    //?}
}
