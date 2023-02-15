package cc.polyfrost.vanillahud.mixin;

import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiIngame.class)
public interface GuiIngameAccessor {

    @Accessor("recordPlaying")
    String getRecordPlaying();

    @Accessor("recordIsPlaying")
    boolean getRecordIsPlaying();

    @Accessor("recordPlayingUpFor")
    int getRecordPlayingUpFor();

}