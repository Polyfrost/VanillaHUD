package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
public class GuiMixinTabList {
    //? if <1.21.4 {
    /*@WrapMethod(method = "renderTabList")
    private void vanillahud$tabList(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker,
                                    Operation<Void> original) {
        if (!Huds.INSTANCE.getTabList().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getTabList());
        original.call(graphics, deltaTracker);
        HudTransform.end(graphics);
    }
    *///?}

    @Unique
    private boolean vanillahud$keyHeld;
    @Unique
    private boolean vanillahud$toggled;

    @Redirect(
            //? if <26 {
            /*method = "renderTabList",
            *///?} else {
            method = "extractTabList",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z")
    )
    private boolean vanillahud$displayMode(KeyMapping key) {
        boolean down = key.isDown();
        if (HudManager.INSTANCE.isEditing()) return true;
        if (Huds.INSTANCE.getTabList().getDisplayMode() == 0) return down;
        if (down && !vanillahud$keyHeld) vanillahud$toggled = !vanillahud$toggled;
        vanillahud$keyHeld = down;
        return vanillahud$toggled;
    }
}
