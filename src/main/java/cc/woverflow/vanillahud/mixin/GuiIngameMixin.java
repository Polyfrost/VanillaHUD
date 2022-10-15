package cc.woverflow.vanillahud.mixin;

import cc.polyfrost.oneconfig.gui.GuiPause;
import cc.polyfrost.oneconfig.libs.universal.UGraphics;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.woverflow.vanillahud.ActionBar;
import cc.woverflow.vanillahud.BossBar;
import cc.woverflow.vanillahud.Scoreboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.ScoreObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class GuiIngameMixin {

    @Inject(method = "renderBossHealth", at = @At("HEAD"), cancellable = true)
    private void cancelBossBar(CallbackInfo ci) {
        ci.cancel();
        if (ActionBar.INSTANCE.getHud().isEnabled()) {
            //todo diamond can we get a better check of when the hud gui is open
            final GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if (!(screen instanceof GuiPause)) {
                BossBar.INSTANCE.getHud().drawAll(null, false);
            }
        }
        UGraphics.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        UMinecraft.getMinecraft().getTextureManager().bindTexture(Gui.icons);
    }

    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void cancelScoreboard(ScoreObjective s, ScaledResolution sr, CallbackInfo ci) {
        //ci.cancel();
        //Scoreboard.INSTANCE.renderScoreboard(s);
    }
}
