package org.polyfrost.vanillahud.mixin.elements;

//? if > 1.8.9 {
import org.spongepowered.asm.mixin.Mixin;

//? if >=1.21.6 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
//? if > 1.8.9
import net.minecraft.client.DeltaTracker;
//? if > 1.8.9
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ContextualBar;
//? if >=26.2 {
import net.minecraft.client.gui.contextualbar.ExperienceBar;
import net.minecraft.client.gui.contextualbar.LocatorBar;
//?} else {
/*import net.minecraft.client.gui.contextualbar.ExperienceBarRenderer;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
*///?}
import org.polyfrost.vanillahud.hud.HotbarHud;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
//?}

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
public class GuiMixinLocatorBar {
    //? if >=1.21.6 {
    @WrapOperation(
            //? if >=26 {
            method = "extractHotbarAndDecorations",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V")
            //?} else {
            /*method = "renderHotbarAndDecorations",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;render(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V")
            *///?}
    )
    private void vanillahud$locatorBar(ContextualBar bar, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!Huds.INSTANCE.getHotbar().shouldDraw()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getHotbar());
        original.call(bar, graphics, delta);
        if (vanillahud$overlaysLocator()
                && Minecraft.getInstance().player.connection.getWaypointManager().hasWaypoints()) {
            if (vanillahud$locator == null) {
                //? if >=26.2 {
                vanillahud$locator = new LocatorBar(Minecraft.getInstance());
                //?} else
                //vanillahud$locator = new LocatorBarRenderer(Minecraft.getInstance());
            }
            //? if >=26 {
            vanillahud$locator.extractRenderState(graphics, delta);
            //?} else
            //vanillahud$locator.render(graphics, delta);
        }
        HudTransform.end(graphics);
    }

    @Unique private ContextualBar vanillahud$locator;

    @Unique
    private static boolean vanillahud$overlaysLocator() {
        HotbarHud hud = Huds.INSTANCE.getHotbar();
        if (!hud.getOverlayLocatorBar()) return false;
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player.jumpableVehicle() != null
                || (hud.getExperienceBar() && minecraft.gameMode.hasExperience());
    }

    @ModifyExpressionValue(method = "nextContextualInfoState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/waypoints/ClientWaypointManager;hasWaypoints()Z"))
    private boolean vanillahud$keepBarUnderLocator(boolean original) {
        return original && !vanillahud$overlaysLocator();
    }

    @ModifyExpressionValue(method = "nextContextualInfoState", at = @At(value = "INVOKE",
            //? if >=26.2 {
            target = "Lnet/minecraft/client/gui/Hud;willPrioritizeExperienceInfo()Z"
            //?} else {
            /*target = "Lnet/minecraft/client/gui/Gui;willPrioritizeExperienceInfo()Z"
            *///?}
    ))
    private boolean vanillahud$prioritizeExperience(boolean original) {
        return original && Huds.INSTANCE.getHotbar().getExperienceBar();
    }

    @WrapOperation(
            //? if >=26 {
            method = "extractHotbarAndDecorations",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V")
            //?} else {
            /*method = "renderHotbarAndDecorations",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;renderBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V")
            *///?}
    )
    private void vanillahud$experienceBar(ContextualBar bar, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        //? if >=26.2 {
        if (bar instanceof ExperienceBar && !Huds.INSTANCE.getHotbar().getExperienceBar()) return;
        //?} else
        //if (bar instanceof ExperienceBarRenderer && !Huds.INSTANCE.getHotbar().getExperienceBar()) return;
        original.call(bar, graphics, delta);
    }
    //?}
}
//?} else {
/*// the locator bar arrived in 1.21.6
import net.minecraft.client.gui.GameGui;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameGui.class)
public class GuiMixinLocatorBar {
}
*///?}
