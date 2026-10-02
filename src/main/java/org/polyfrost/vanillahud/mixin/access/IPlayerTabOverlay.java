package org.polyfrost.vanillahud.mixin.access;

//? if > 1.8.9 {
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
//?} else {
/*import net.minecraft.client.gui.overlay.PlayerTabOverlay;
import net.minecraft.text.Text;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerTabOverlay.class)
public interface IPlayerTabOverlay {
    //? if > 1.8.9 {
    @Accessor("header")
    Component getHeader();

    @Accessor("footer")
    Component getFooter();
    //?} else {
    /*@Accessor("header")
    Text getHeader();

    @Accessor("footer")
    Text getFooter();
    *///?}
}
