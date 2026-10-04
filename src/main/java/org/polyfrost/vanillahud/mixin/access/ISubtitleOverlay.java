package org.polyfrost.vanillahud.mixin.access;

//? if > 1.8.9 {
import net.minecraft.client.gui.components.SubtitleOverlay;
//?} else {
/*import net.minecraft.client.gui.GameGui;
*///?}
import org.spongepowered.asm.mixin.Mixin;
//? if > 1.8.9 {
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
//?}

// 1.8.9 has no subtitles, so this has no target there
//? if > 1.8.9 {
@Mixin(SubtitleOverlay.class)
//?} else {
/*@Mixin(GameGui.class)
*///?}
public interface ISubtitleOverlay {
    //? if > 1.8.9 {
    @Accessor("audibleSubtitles")
    List<?> getAudibleSubtitles();

    @Accessor("subtitles")
    List<Object> getSubtitles();
    //?}
}
