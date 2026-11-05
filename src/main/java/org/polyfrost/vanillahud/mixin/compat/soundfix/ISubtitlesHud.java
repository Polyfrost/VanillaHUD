package org.polyfrost.vanillahud.mixin.compat.soundfix;

//? if = 1.8.9 {
/*import net.minecraft.client.sound.SoundPool;
import net.minecraft.client.sound.instance.SoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Pseudo
@Mixin(targets = "io.github.moehreag.soundfix.subtitles.SubtitlesHud", remap = false)
public interface ISubtitlesHud {
    @Accessor("audibleSubtitles")
    List<?> getAudibleSubtitles();

    @Accessor("subtitles")
    List<Object> getSubtitles();

    @Accessor("showSubtitles")
    boolean getShowSubtitles();

    @Accessor("showSubtitles")
    void setShowSubtitles(boolean show);

    @Invoker("onPlaySound")
    void playSound(SoundInstance sound, SoundPool pool, float range);
}
*///?}
