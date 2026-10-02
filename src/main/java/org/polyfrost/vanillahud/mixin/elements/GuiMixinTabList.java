package org.polyfrost.vanillahud.mixin.elements;

//? if = 1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.overlay.PlayerTabOverlay;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.polyfrost.vanillahud.compat.LegacyDrawContext;
import org.polyfrost.vanillahud.render.HudTransform;
*///?}

import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.hud.TabListHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

//? if <1.21.4 && >1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.vanillahud.render.HudTransform;
*///?}

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} elif > 1.8.9 {
/*import net.minecraft.client.gui.Gui;
*///?} else {
/*import net.minecraft.client.gui.GameGui;
*///?}

//? if >=26.2 {
@Mixin(Hud.class)
//?} elif > 1.8.9 {
/*@Mixin(Gui.class)
*///?} else {
/*@Mixin(GameGui.class)
*///?}
public class GuiMixinTabList {
    //? if > 1.8.9 {
    //? if <1.21.4 && >1.8.9 {
    /*@WrapMethod(method = "renderTabList")
    private void vanillahud$tabList(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker,
                                    Operation<Void> original) {
        if (!Huds.INSTANCE.getTabList().shouldDraw()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getTabList());
        original.call(graphics, deltaTracker);
        HudTransform.end(graphics);
    }
    *///?}

    @Unique
    private boolean vanillahud$keyHeld;
    @Unique
    private boolean vanillahud$toggled;

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "renderTabList",
            *///?} else {
            method = "extractTabList",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z")
    )
    private boolean vanillahud$displayMode(boolean down) {
        TabListHud hud = Huds.INSTANCE.getTabList();
        if (hud.getPreviewing()) {
            hud.updateOpen(true);
            return true;
        }
        boolean open;
        if (hud.getDisplayMode() == 0) {
            open = down;
        } else {
            if (down && !vanillahud$keyHeld) vanillahud$toggled = !vanillahud$toggled;
            vanillahud$keyHeld = down;
            open = vanillahud$toggled;
        }
        hud.updateOpen(open);
        return open || hud.isRendering();
    }
    //?} else {
    /*// 1.8.9 only asks the overlay to render while the list is open, so that call is both the
    // transform bracket and the open signal the slide animation needs.
    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/overlay/PlayerTabOverlay;render(ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"))
    private void vanillahud$tabList(PlayerTabOverlay overlay, int width, Scoreboard scoreboard,
                                    ScoreboardObjective objective, Operation<Void> original) {
        TabListHud hud = Huds.INSTANCE.getTabList();
        hud.updateOpen(true);
        if (!hud.shouldDraw()) return;

        HudTransform.begin(LegacyDrawContext.INSTANCE, hud);
        original.call(overlay, width, scoreboard, objective);
        HudTransform.end(LegacyDrawContext.INSTANCE);
    }
    *///?}
}
