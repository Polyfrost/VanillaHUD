package cc.polyfrost.vanillahud.mixin;

import cc.polyfrost.oneconfig.libs.universal.UGraphics;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.ScoreObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiIngame.class, priority = 9000)
public class GuiIngameMixin {

    @Inject(method = "renderBossHealth", at = @At("HEAD"), cancellable = true)
    private void cancelBossBar(CallbackInfo ci) {
        ci.cancel();
        UGraphics.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        UMinecraft.getMinecraft().getTextureManager().bindTexture(Gui.icons);
    }

    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void cancelScoreboard(ScoreObjective s, ScaledResolution sr, CallbackInfo ci) {
        ci.cancel();
    }
}
