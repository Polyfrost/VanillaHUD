package org.polyfrost.vanillahud.mixin;

import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiPlayerTabOverlay.class)
public interface GuiPlayerTabOverlayAccessor {

    @Invoker("drawPing")
    void renderPing(int i, int j, int k, NetworkPlayerInfo networkPlayerInfoIn);
}