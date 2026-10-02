package org.polyfrost.vanillahud.mixin.access;

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public interface IGui {
    @Accessor("title")
    Component getTitle();

    @Accessor("subtitle")
    Component getSubtitle();

    @Accessor("titleTime")
    int getTitleTime();

    @Accessor("overlayMessageString")
    Component getOverlay();

    @Accessor("overlayMessageTime")
    int getOverlayMessageTime();

    @Accessor("toolHighlightTimer")
    int getToolHighlightTimer();

    @Accessor("lastToolHighlight")
    ItemStack getLastToolHighlight();

    @Accessor("subtitleOverlay")
    SubtitleOverlay getSubtitleOverlay();
}
