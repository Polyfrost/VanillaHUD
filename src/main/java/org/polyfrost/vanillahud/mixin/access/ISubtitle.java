package org.polyfrost.vanillahud.mixin.access;

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.components.SubtitleOverlay$Subtitle")
public interface ISubtitle {
    @Accessor("text")
    Component getSubtitleText();
}
