package org.polyfrost.vanillahud.mixin.minecraft.interfaces;

import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiPlayerTabOverlay.class)
public interface GuiPlayerTabOverlayAccessor {

    @Invoker("drawScoreboardValues")
    void renderScore(ScoreObjective scoreObjective, int i, String string, int j, int k, NetworkPlayerInfo networkPlayerInfo);

    @Invoker("drawPing")
    void renderPing(int i, int j, int k, NetworkPlayerInfo networkPlayerInfoIn);

    @Accessor
    IChatComponent getHeader();

    @Accessor
    IChatComponent getFooter();

}