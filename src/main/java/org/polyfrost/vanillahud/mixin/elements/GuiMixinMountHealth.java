package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public class GuiMixinMountHealth {
    //? if <1.21.6 {
    /*@WrapMethod(method = "renderVehicleHealth")
    private void vanillahud$mount(GuiGraphicsExtractor graphics, Operation<Void> original) {
        if (!Huds.INSTANCE.getMountHealth().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getMountHealth());
        original.call(graphics);
        HudTransform.end(graphics);
    }
    *///?}
}
