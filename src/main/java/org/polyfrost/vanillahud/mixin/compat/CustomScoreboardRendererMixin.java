package org.polyfrost.vanillahud.mixin.compat;

import kotlin.Pair;
import org.polyfrost.vanillahud.compat.CustomScoreboardBridge;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "me.owdding.customscoreboard.feature.customscoreboard.CustomScoreboardRenderer", remap = false)
public class CustomScoreboardRendererMixin {

    @Shadow
    private static Pair<Integer, Integer> position;

    @Shadow
    private static Pair<Integer, Integer> dimensions;

    @Dynamic("Custom Scoreboard")
    @Inject(method = "updatePosition", at = @At("TAIL"), remap = false)
    private void vanillahud$reposition(CallbackInfo ci) {
        if (position == null || dimensions == null) return;
        int csX = position.getFirst();
        int csY = position.getSecond();
        int width = dimensions.getFirst();
        int height = dimensions.getSecond();

        int[] override = CustomScoreboardBridge.onCsPosition(csX, csY, width, height);
        if (override != null) {
            position = new Pair<>(override[0], override[1]);
        }
    }
}
