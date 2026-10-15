package org.polyfrost.vanillahud.mixin.access;

//? if > 1.8.9 {
import net.minecraft.network.chat.Component;
//?} else {
/*import net.minecraft.client.gui.GameGui;
*///?}
import org.spongepowered.asm.mixin.Mixin;
//? if > 1.8.9 {
import org.spongepowered.asm.mixin.gen.Accessor;
//?}

// 1.8.9 has no subtitles, so this has no target there
//? if > 1.8.9 {
@Mixin(targets = "net.minecraft.client.gui.components.SubtitleOverlay$Subtitle")
//?} else {
/*@Mixin(GameGui.class)
*///?}
public interface ISubtitle {
    //? if > 1.8.9 {
    @Accessor("text")
    Component getSubtitleText();
    //?}
}
