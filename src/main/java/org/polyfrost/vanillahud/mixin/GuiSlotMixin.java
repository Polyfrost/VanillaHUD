package org.polyfrost.vanillahud.mixin;

import cc.polyfrost.oneconfig.gui.animations.*;
import net.minecraft.client.gui.GuiSlot;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.utils.EaseOutQuart;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiSlot.class)
public class GuiSlotMixin {

    @Shadow protected float amountScrolled;

    @Unique private Animation animation = new DummyAnimation(0f);

    @Unique private float end = 0f;

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiSlot;bindAmountScrolled()V", shift = At.Shift.AFTER))
    private void setAnimation(CallbackInfo ci) {
        if (!VanillaHUD.scrollableTooltip.enabled) return;
        end = amountScrolled;
        if (end != animation.getEnd()) {
            animation = new EaseOutQuart(200, animation.get(), end, false);
        }
        amountScrolled = animation.get();
    }

    @Inject(method = "drawScreen", at = @At("TAIL"))
    private void reset(int mouseXIn, int mouseYIn, float p_148128_3_, CallbackInfo ci) {
        if (!VanillaHUD.scrollableTooltip.enabled) return;
        amountScrolled = end;
    }

}