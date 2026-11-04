package org.polyfrost.vanillahud.mixin;

//? if >=1.21.8 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.Window;
import org.polyfrost.vanillahud.render.ForeignTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

// must run after the handlers of mods that rescale the window, to replace what they returned
@Mixin(value = Window.class, priority = 2000)
public class WindowScaleMixin {
    @Shadow private int guiScaledWidth;
    @Shadow private int guiScaledHeight;

    @ModifyReturnValue(method = "getGuiScaledWidth", at = @At("RETURN"))
    private int vanillahud$realWidth(int original) {
        return ForeignTransform.realWindow ? this.guiScaledWidth : original;
    }

    @ModifyReturnValue(method = "getGuiScaledHeight", at = @At("RETURN"))
    private int vanillahud$realHeight(int original) {
        return ForeignTransform.realWindow ? this.guiScaledHeight : original;
    }
}
//?} else {
/*import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Minecraft.class)
public class WindowScaleMixin {
}
*///?}
