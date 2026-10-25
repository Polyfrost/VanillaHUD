package org.polyfrost.vanillahud.mixin.elements;

//? if > 1.8.9 {
import net.minecraft.client.gui.components.LerpingBossEvent;
import org.polyfrost.vanillahud.hud.Huds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LerpingBossEvent.class)
public class LerpingBossEventMixin {
    @Shadow
    protected float targetPercent;

    @Inject(method = "getProgress", at = @At("HEAD"), cancellable = true)
    private void vanillahud$instant(CallbackInfoReturnable<Float> cir) {
        if (!Huds.INSTANCE.getBossBar().getSmoothHealth()) cir.setReturnValue(targetPercent);
    }

    @ModifyConstant(method = "getProgress", constant = @Constant(floatValue = 100.0f))
    private float vanillahud$lerpSpeed(float original) {
        return Huds.INSTANCE.getBossBar().getLerpSpeed();
    }
}
//?} else {
/*// 1.8.9 has no lerping boss event, BossBarHud smooths the health itself
import net.minecraft.client.gui.GameGui;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameGui.class)
public class LerpingBossEventMixin {
}
*///?}
