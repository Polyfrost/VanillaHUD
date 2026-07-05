package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
//import net.minecraft.client.gui.Gui;
//?}
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
//@Mixin(Gui.class)
//?}
public class GuiMixinScoreboard {
    @WrapMethod(
            //? if < 26 {
            /*method = "renderScoreboardSidebar"
            *///?} else {
            method = "extractScoreboardSidebar"
            //?}
    )
    private void vanillahud$scoreboard(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (!Huds.INSTANCE.getScoreboard().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getScoreboard());
        original.call(graphics, deltaTracker);
        HudTransform.end(graphics);
    }

    @ModifyExpressionValue(
            method = "displayScoreboardSidebar",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Objective;numberFormatOrDefault(Lnet/minecraft/network/chat/numbers/NumberFormat;)Lnet/minecraft/network/chat/numbers/NumberFormat;")
    )
    private NumberFormat vanillahud$scoreboard$scorePointsColor(NumberFormat original) {
        return new StyledFormat(Style.EMPTY.withColor(Huds.INSTANCE.getScoreboard().getScorePointsColor().getArgb()));
    }

    @ModifyArg(
            method = "displayScoreboardSidebar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"
            ),
            index = 4
    )
    private int vanillahud$scoreboard$backgroundColor(int x0) {
        return Huds.INSTANCE.getScoreboard().getBgColor();
    }

    // TODO: Independent shadows for Header, Body, and Score
    @ModifyArg(
            method = "displayScoreboardSidebar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V"
            ),
            index = 5
    )
    private boolean vanillahud$scoreboard$textShadow(boolean dropShadow) {
        return Huds.INSTANCE.getScoreboard().getShowShadow();
    }
}
