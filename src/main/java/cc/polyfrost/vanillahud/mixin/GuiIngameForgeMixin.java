package cc.polyfrost.vanillahud.mixin;

import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiIngameForge.class, remap = false, priority = 9000)
public class GuiIngameForgeMixin {

    @Inject(method = "renderRecordOverlay", at = @At("HEAD"), cancellable = true)
    private void cancelActionBar(int width, int height, float partialTicks, CallbackInfo ci) {
        ci.cancel();
    }
}
