package org.polyfrost.vanillahud.mixin.compat.soundfix;

//? if = 1.8.9 {
/*import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "io.github.moehreag.soundfix.subtitles.SubtitlesHud$Subtitle", remap = false)
public interface ISubtitleEntry {
    @Accessor("text")
    String getSubtitleText();
}
*///?}
