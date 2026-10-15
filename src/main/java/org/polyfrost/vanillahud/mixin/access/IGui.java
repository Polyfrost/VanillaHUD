package org.polyfrost.vanillahud.mixin.access;

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} elif > 1.8.9 {
/*import net.minecraft.client.gui.Gui;
*///?} else {
/*import net.minecraft.client.gui.GameGui;
*///?}
//? if > 1.8.9 {
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.network.chat.Component;
//?}
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >=26.2 {
@Mixin(Hud.class)
//?} elif > 1.8.9 {
/*@Mixin(Gui.class)
*///?} else {
/*@Mixin(GameGui.class)
*///?}
public interface IGui {
    //? if > 1.8.9 {
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
    //?} else {
    /*// 1.8.9 holds these as legacy formatted strings, and has no subtitle overlay at all
    @Accessor("title")
    String getTitleText();

    @Accessor("subtitle")
    String getSubtitleText();

    @Accessor("titleTime")
    int getTitleTime();

    @Accessor("overlayMessage")
    String getOverlayText();

    @Accessor("overlayMessageCooldown")
    int getOverlayMessageCooldown();

    @Accessor("itemSelectedTimer")
    int getItemSelectedTimer();

    @Accessor("selectedItem")
    ItemStack getLastToolHighlight();
    *///?}
}
