package cc.woverflow.vanillahud.mixin;

import cc.woverflow.vanillahud.ActionBar;
import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiIngameForge.class, remap = false)
public class GuiIngameForgeMixin {

    @Inject(method = "renderRecordOverlay", at = @At("HEAD"), cancellable = true)
    private void cancelActionBar(int width, int height, float partialTicks, CallbackInfo ci) {
        ci.cancel();
        ActionBar.INSTANCE.renderActionBar(width, height, partialTicks);
    }
}
