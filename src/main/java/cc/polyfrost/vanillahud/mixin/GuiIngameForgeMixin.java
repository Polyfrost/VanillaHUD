package cc.polyfrost.vanillahud.mixin;

import cc.polyfrost.vanillahud.hud.Hotbar;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiIngameForge.class, remap = false, priority = 9000)
public abstract class GuiIngameForgeMixin {

    @Shadow protected abstract void post(RenderGameOverlayEvent.ElementType type);

    @Inject(method = "renderRecordOverlay", at = @At("HEAD"), cancellable = true)
    private void cancelActionBar(int width, int height, float partialTicks, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderHealth", at = @At("HEAD"))
    private void resetHealth(int width, int height, CallbackInfo ci) {
        Hotbar.HeartStatusHUD.renderHealthBar = false;
    }

    @Inject(method = "renderHealth", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;pre(Lnet/minecraftforge/client/event/RenderGameOverlayEvent$ElementType;)Z", shift = At.Shift.AFTER), cancellable = true)
    private void cancelHealth(int width, int height, CallbackInfo ci) {
        Hotbar.HeartStatusHUD.renderHealthBar = true;
        post(RenderGameOverlayEvent.ElementType.HEALTH);
        ci.cancel();
    }

    @Inject(method = "renderArmor", at = @At("HEAD"))
    private void resetArmor(int width, int height, CallbackInfo ci) {
        Hotbar.HeartStatusHUD.renderArmorBar = false;
    }

    @Inject(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;pre(Lnet/minecraftforge/client/event/RenderGameOverlayEvent$ElementType;)Z", shift = At.Shift.AFTER), cancellable = true)
    private void cancelArmor(int width, int height, CallbackInfo ci) {
        Hotbar.HeartStatusHUD.renderArmorBar = true;
        post(RenderGameOverlayEvent.ElementType.ARMOR);
        ci.cancel();
    }
}
