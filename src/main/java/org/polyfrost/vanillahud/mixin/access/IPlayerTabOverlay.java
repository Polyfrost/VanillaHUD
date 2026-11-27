package org.polyfrost.vanillahud.mixin.access;

//? if > 1.8.9 {
import java.util.List;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.gen.Invoker;
//?} else {
/*import com.google.common.collect.Ordering;
import net.minecraft.client.gui.overlay.PlayerTabOverlay;
import net.minecraft.client.network.PlayerInfo;
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

    @Invoker("getPlayerInfos")
    List<PlayerInfo> invokeGetPlayerInfos();
    //?} else {
    /*@Accessor("header")
    Text getHeader();

    @Accessor("footer")
    Text getFooter();

    @Accessor("PLAYER_ORDERING")
    static Ordering<PlayerInfo> getPlayerOrdering() {
        throw new AssertionError();
    }
    *///?}
}
