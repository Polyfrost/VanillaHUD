package org.polyfrost.vanillahud.mixin;

import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.IBossDisplayData;
import org.polyfrost.vanillahud.hooks.BossStatusHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossStatus.class)
public class BossStatusMixin {

    @Inject(method = "setBossStatus", at = @At("TAIL"))
    private static void setBossStatus(IBossDisplayData displayData, boolean hasColorModifierIn, CallbackInfo ci) {
        BossStatusHook.onStatusSet();
    }
}
