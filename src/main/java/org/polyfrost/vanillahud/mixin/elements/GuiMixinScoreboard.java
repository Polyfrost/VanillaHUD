package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Font;
//? if > 1.8.9
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} elif > 1.8.9 {
/*import net.minecraft.client.gui.Gui;
*///?} else {
/*import net.minecraft.client.gui.GameGui;
*///?}
//? if > 1.8.9 {
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.world.scores.Objective;
//?} else {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.render.Window;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import org.polyfrost.vanillahud.compat.LegacyDrawContext;
import org.polyfrost.vanillahud.hud.ScoreboardHud;
import org.polyfrost.vanillahud.render.HudTransform;
*///?}
import org.polyfrost.vanillahud.hud.Huds;
//? if > 1.8.9 {
import org.polyfrost.vanillahud.hud.ScoreboardHud;
import org.polyfrost.vanillahud.render.ScoreboardBackground;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if <1.21.4 && >1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import net.minecraft.client.DeltaTracker;
import org.polyfrost.vanillahud.render.HudTransform;
*///?}

//? if >=26.2 {
@Mixin(Hud.class)
//?} elif > 1.8.9 {
/*@Mixin(Gui.class)
*///?} else {
/*@Mixin(GameGui.class)
*///?}
public class GuiMixinScoreboard {
    //? if > 1.8.9 {
    @Unique
    private int vanillahud$scoreboardTop = Integer.MIN_VALUE;

    @Unique
    private int vanillahud$titleX0, vanillahud$titleX1, vanillahud$titleY1;

    @Unique
    private static void vanillahud$fill(Operation<Void> original, GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color) {
        if ((color >>> 24) == 0) return;
        original.call(graphics, x0, y0, x1, y1, color);
    }

    //? if <1.21.4 && >1.8.9 {
    /*@WrapMethod(method = "renderScoreboardSidebar")
    private void vanillahud$scoreboard(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (!Huds.INSTANCE.getScoreboard().shouldDraw()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getScoreboard());
        original.call(graphics, deltaTracker);
        HudTransform.end(graphics);
    }
    *///?}

    @Inject(method = "displayScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void vanillahud$scoreboard$persistent(GuiGraphicsExtractor graphics, Objective objective, CallbackInfo ci) {
        this.vanillahud$scoreboardTop = Integer.MIN_VALUE;
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
            //? if <1.21.4 && >1.8.9 {
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
        ScoreboardHud hud = Huds.INSTANCE.getScoreboard();
        if (!hud.getScoreboardTitle()) return;
        this.vanillahud$scoreboardTop = y0;
        if (hud.getHasCustomBackground()) {
            this.vanillahud$titleX0 = x0;
            this.vanillahud$titleX1 = x1;
            this.vanillahud$titleY1 = y1;
            return;
        }
        vanillahud$fill(original, graphics, x0, y0, x1, y1, hud.getTitleBgColor());
    }

    @WrapOperation(
            //? if <1.21.4 && >1.8.9 {
            /*method = "method_55440",
            *///?} else {
            method = "displayScoreboardSidebar",
            //?}
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V",
                    ordinal = 1
            )
    )
    private void vanillahud$scoreboard$background(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color, Operation<Void> original) {
        ScoreboardHud hud = Huds.INSTANCE.getScoreboard();
        if (hud.getHasCustomBackground()) {
            boolean hasTitle = this.vanillahud$scoreboardTop != Integer.MIN_VALUE;
            int top = hasTitle ? this.vanillahud$scoreboardTop : y0;
            boolean drew = ScoreboardBackground.render(graphics, x0, top, x1, y1, hud.getBackgroundImagePath());
            if (hasTitle && (hud.getKeepBackgroundColor() || !drew)) {
                vanillahud$fill(original, graphics, this.vanillahud$titleX0, top, this.vanillahud$titleX1, this.vanillahud$titleY1, hud.getTitleBgColor());
            }
            if (drew && !hud.getKeepBackgroundColor()) return;
        }
        vanillahud$fill(original, graphics, x0, y0, x1, y1, hud.getBodyBgColor());
    }

    @WrapOperation(
            //? if <1.21.4 && >1.8.9 {
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
    //? if <1.21.6 && >1.8.9 {
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
            //? if <1.21.4 && >1.8.9 {
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
    //?} else {
    /*// 1.8.9 renders the whole sidebar in GameGui.renderScoreboardObjective, one row at a time,
    // and draws the title in the last iteration. Read off the bytecode, the calls in there are:
    //   fill   0 = row background, 1 = title background, 2 = the strip between title and rows
    //   draw   0 = entry name,     1 = score points,     2 = title
    @WrapMethod(method = "renderScoreboardObjective")
    private void vanillahud$scoreboard(ScoreboardObjective objective, Window window, Operation<Void> original) {
        if (!Huds.INSTANCE.getScoreboard().shouldDraw()) return;

        HudTransform.begin(LegacyDrawContext.INSTANCE, Huds.INSTANCE.getScoreboard());
        original.call(objective, window);
        HudTransform.end(LegacyDrawContext.INSTANCE);
    }

    @Unique
    private static void vanillahud$fill(Operation<Void> original, int x0, int y0, int x1, int y1, int color) {
        if ((color >>> 24) == 0) return;
        original.call(x0, y0, x1, y1, color);
    }

    // a legacy colour code in the string wins over the colour argument, so it has to go before
    // the configured score colour can take effect
    @Unique
    private static String vanillahud$stripLeadingColor(String text) {
        String out = text;
        while (out.length() >= 2 && out.charAt(0) == '\u00a7') {
            out = out.substring(2);
        }
        return out;
    }

    @Unique
    private int vanillahud$draw(Font font, String text, int x, int y, int color, Operation<Integer> original) {
        if (Huds.INSTANCE.getScoreboard().getTextShadow()) {
            return font.drawWithShadow(text, (float) x, (float) y, color);
        }
        return original.call(font, text, x, y, color);
    }

    @WrapOperation(method = "renderScoreboardObjective", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GameGui;fill(IIIII)V", ordinal = 0))
    private void vanillahud$rowBackground(int x0, int y0, int x1, int y1, int color, Operation<Void> original) {
        vanillahud$fill(original, x0, y0, x1, y1, Huds.INSTANCE.getScoreboard().getBodyBgColor());
    }

    @WrapOperation(method = "renderScoreboardObjective", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GameGui;fill(IIIII)V", ordinal = 1))
    private void vanillahud$titleBackground(int x0, int y0, int x1, int y1, int color, Operation<Void> original) {
        if (!Huds.INSTANCE.getScoreboard().getScoreboardTitle()) return;
        vanillahud$fill(original, x0, y0, x1, y1, Huds.INSTANCE.getScoreboard().getTitleBgColor());
    }

    @WrapOperation(method = "renderScoreboardObjective", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GameGui;fill(IIIII)V", ordinal = 2))
    private void vanillahud$titleGap(int x0, int y0, int x1, int y1, int color, Operation<Void> original) {
        vanillahud$fill(original, x0, y0, x1, y1, Huds.INSTANCE.getScoreboard().getBodyBgColor());
    }

    // vanilla folds ": <score>" into every row when it sizes the sidebar, so hiding the score
    // points left a dead column on the right. width 1 is the sizing loop: 0 is the title, 2 right
    // aligns the drawn score and 3 centres the title.
    @WrapOperation(method = "renderScoreboardObjective", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I", ordinal = 1))
    private int vanillahud$entryWidth(Font font, String text, Operation<Integer> original,
                                      @Local ScoreboardScore score,
                                      @Local(argsOnly = true) ScoreboardObjective objective) {
        ScoreboardHud hud = Huds.INSTANCE.getScoreboard();
        if (hud.showScorePoints(objective.getScoreboard().getScores(objective))) {
            return original.call(font, text);
        }
        // the colour code vanilla splices in front of the number measures zero, so the suffix
        // is exactly what ": " plus the digits take up
        return original.call(font, text) - font.width(": " + score.get());
    }

    @WrapOperation(method = "renderScoreboardObjective", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;draw(Ljava/lang/String;III)I", ordinal = 0))
    private int vanillahud$entryName(Font font, String text, int x, int y, int color,
                                     Operation<Integer> original) {
        return vanillahud$draw(font, text, x, y, color, original);
    }

    @WrapOperation(method = "renderScoreboardObjective", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;draw(Ljava/lang/String;III)I", ordinal = 1))
    private int vanillahud$scorePoints(Font font, String text, int x, int y, int color,
                                       Operation<Integer> original,
                                       @Local(argsOnly = true) ScoreboardObjective objective) {
        ScoreboardHud hud = Huds.INSTANCE.getScoreboard();
        if (!hud.showScorePoints(objective.getScoreboard().getScores(objective))) return 0;
        return vanillahud$draw(font, vanillahud$stripLeadingColor(text), x, y,
                hud.getScorePointsColor().getArgb(), original);
    }

    @WrapOperation(method = "renderScoreboardObjective", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;draw(Ljava/lang/String;III)I", ordinal = 2))
    private int vanillahud$title(Font font, String text, int x, int y, int color,
                                 Operation<Integer> original) {
        if (!Huds.INSTANCE.getScoreboard().getScoreboardTitle()) return 0;
        return vanillahud$draw(font, text, x, y, color, original);
    }
    *///?}
}
