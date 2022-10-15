package cc.woverflow.vanillahud.mixin;

import cc.polyfrost.oneconfig.gui.GuiPause;
import cc.polyfrost.oneconfig.utils.gui.OneUIScreen;
import cc.woverflow.vanillahud.ActionBar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
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
        if (!ActionBar.INSTANCE.getHud().isEnabled()) return;
        //todo diamond can we get a better check of when the hud gui is open
        final GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof GuiPause && !(screen instanceof OneUIScreen)) return;
        ActionBar.INSTANCE.getHud().drawAll(null, false);
    }
}
