package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=26.2 {
/*import net.minecraft.client.gui.Hud;
*///?} else {
import net.minecraft.client.gui.Gui;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.world.scores.Objective;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.hud.ScoreboardHud;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.2 {
/*@Mixin(Hud.class)
*///?} else {
@Mixin(Gui.class)
//?}
public class GuiMixinScoreboard {
    //? if <1.21.4 {
    /*@WrapMethod(method = "renderScoreboardSidebar")
    private void vanillahud$scoreboard(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (!Huds.INSTANCE.getScoreboard().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getScoreboard());
        original.call(graphics, deltaTracker);
        HudTransform.end(graphics);
    }
    *///?}

    @Inject(method = "displayScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void vanillahud$scoreboard$persistent(GuiGraphicsExtractor graphics, Objective objective, CallbackInfo ci) {
        ScoreboardHud hud = Huds.INSTANCE.getScoreboard();
        long visible = objective.getScoreboard().listPlayerScores(objective).stream()
                .filter(score -> !score.isHidden())
                .count();
        if (visible == 0 && !(hud.getPersistentTitle() && hud.getScoreboardTitle())) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(
            method = "displayScoreboardSidebar",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Objective;numberFormatOrDefault(Lnet/minecraft/network/chat/numbers/NumberFormat;)Lnet/minecraft/network/chat/numbers/NumberFormat;")
    )
    private NumberFormat vanillahud$scoreboard$scorePoints(NumberFormat original, @Local(argsOnly = true) Objective objective) {
        ScoreboardHud hud = Huds.INSTANCE.getScoreboard();
        if (!hud.showScorePoints(objective.getScoreboard().listPlayerScores(objective))) {
            return BlankFormat.INSTANCE;
        }
        return new StyledFormat(Style.EMPTY.withColor(hud.getScorePointsColor().getArgb()));
    }

    @WrapOperation(
            //? if <1.21.4 {
            /*method = "method_55440",
            *///?} else {
            method = "displayScoreboardSidebar",
            //?}
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V",
                    ordinal = 0
            )
    )
    private void vanillahud$scoreboard$titleBackground(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color, Operation<Void> original) {
        if (!Huds.INSTANCE.getScoreboard().getScoreboardTitle()) return;
        original.call(graphics, x0, y0, x1, y1, Huds.INSTANCE.getScoreboard().getTitleBgColor());
    }

    @ModifyArg(
            //? if <1.21.4 {
            /*method = "method_55440",
            *///?} else {
            method = "displayScoreboardSidebar",
            //?}
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V",
                    ordinal = 1
            ),
            index = 4
    )
    private int vanillahud$scoreboard$background(int color) {
        return Huds.INSTANCE.getScoreboard().getBodyBgColor();
    }

    @WrapOperation(
            //? if <1.21.4 {
            /*method = "method_55440",
            *///?} else {
            method = "displayScoreboardSidebar",
            //?}
            at = @At(
                    value = "INVOKE",
                    //? if >=26 {
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V",
                    //?} elif >=1.21.6 {
                    /*target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V",
                    *///?} else {
                    /*target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I",
                    *///?}
                    ordinal = 0
            )
    )
    //? if <1.21.6 {
    /*private int vanillahud$scoreboard$title(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color, boolean dropShadow, Operation<Integer> original) {
        if (!Huds.INSTANCE.getScoreboard().getScoreboardTitle()) return 0;
        return original.call(graphics, font, text, x, y, color, dropShadow);
    }
    *///?} else {
    private void vanillahud$scoreboard$title(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color, boolean dropShadow, Operation<Void> original) {
        if (!Huds.INSTANCE.getScoreboard().getScoreboardTitle()) return;
        original.call(graphics, font, text, x, y, color, dropShadow);
    }
    //?}

    @ModifyArg(
            //? if <1.21.4 {
            /*method = "method_55440",
            *///?} else {
            method = "displayScoreboardSidebar",
            //?}
            at = @At(
                    value = "INVOKE",
                    //? if >=26 {
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V"
                    //?} elif >=1.21.6 {
                    /*target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V"
                    *///?} else {
                    /*target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"
                    *///?}
            ),
            index = 5
    )
    private boolean vanillahud$scoreboard$textShadow(boolean dropShadow) {
        return Huds.INSTANCE.getScoreboard().getTextShadow();
    }
}
