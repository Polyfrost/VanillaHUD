package org.polyfrost.vanillahud.mixin.access;

//? if >=1.21.8 {
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Window.class)
public interface IWindow {
    @Accessor("guiScaledWidth")
    int realWidth();

    @Accessor("guiScaledHeight")
    int realHeight();
}
//?} else {
/*import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Minecraft.class)
public interface IWindow {
}
*///?}
