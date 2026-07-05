package org.polyfrost.vanillahud.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
//import net.minecraft.client.gui.Gui;
//?}
//? if >=1.21.6 {
import net.minecraft.client.gui.contextualbar.ContextualBar;
//?}

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
//@Mixin(Gui.class)
//?}
public class GuiMixin {

    @WrapMethod(
            //? if < 26 {
            /*method = "renderArmor"
            *///?} else {
            method = "extractArmor"
            //?}
    )
    private static void vanillahud$armor(
            GuiGraphicsExtractor graphics, Player player, int yLineBase, int numHealthRows, int healthRowHeight, int xLeft, Operation<Void> original) {
        if (!Huds.INSTANCE.getArmor().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getArmor());
        original.call(graphics, player, yLineBase, numHealthRows, healthRowHeight, xLeft);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            /*method = "renderItemHotbar"
            *///?} else {
            method = "extractItemHotbar"
            //?}
    )
    private void vanillahud$hotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (!Huds.INSTANCE.getHotbar().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getHotbar());
        original.call(graphics, deltaTracker);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            /*method = "renderOverlayMessage"
            *///?} else {
            method = "extractOverlayMessage"
            //?}
    )
    private void vanillahud$actionBar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (!Huds.INSTANCE.getActionBar().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getActionBar());
        original.call(graphics, deltaTracker);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            /*method = "renderSelectedItemName"
            *///?} else {
            method = "extractSelectedItemName"
            //?}
    )
    private void vanillahud$itemName(GuiGraphicsExtractor graphics, Operation<Void> original) {
        if (!Huds.INSTANCE.getHeldItemTooltip().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getHeldItemTooltip());
        original.call(graphics);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            /*method = "renderTitle"
            *///?} else {
            method = "extractTitle"
            //?}
    )
    private void vanillahud$title(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (!Huds.INSTANCE.getTitle().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getTitle());
        original.call(graphics, deltaTracker);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            /*method = "renderScoreboardSidebar"
            *///?} else {
            method = "extractScoreboardSidebar"
            //?}
    )
    private void vanillahud$scoreboard(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (!Huds.INSTANCE.getScoreboard().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getScoreboard());
        original.call(graphics, deltaTracker);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            /*method = "renderTabList"
            *///?} else {
            method = "extractTabList"
            //?}
    )
    private void vanillahud$tabList(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker,
                                    Operation<Void> original) {
        if (!Huds.INSTANCE.getTabList().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getTabList());
        original.call(graphics, deltaTracker);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            /*method = "renderVehicleHealth"
            *///?} else {
            method = "extractVehicleHealth"
            //?}
    )
    private void vanillahud$mount(GuiGraphicsExtractor graphics, Operation<Void> original) {
        if (!Huds.INSTANCE.getMountHealth().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getMountHealth());
        original.call(graphics);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            /*method = "renderHearts"
            *///?} else {
            method = "extractHearts"
            //?}
    )
    private void vanillahud$health(
            GuiGraphicsExtractor graphics, Player player, int xLeft, int yLineBase, int healthRowHeight,
            int heartOffsetIndex, float maxHealth, int currentHealth, int oldHealth,
            int absorption, boolean blink, Operation<Void> original) {
        if (!Huds.INSTANCE.getHealth().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getHealth());
        original.call(graphics, player, xLeft, yLineBase, healthRowHeight, heartOffsetIndex, maxHealth,
                currentHealth, oldHealth, absorption, blink);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            /*method = "renderFood"
            *///?} else {
            method = "extractFood"
            //?}
    )
    private void vanillahud$hunger(
            GuiGraphicsExtractor graphics, Player player, int yLineBase, int xRight, Operation<Void> original) {
        if (!Huds.INSTANCE.getHunger().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getHunger());
        original.call(graphics, player, yLineBase, xRight);
        HudTransform.end(graphics);
    }

    //? if 1.21.1 {
    /*@Inject(
            method = "renderPlayerHealth",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getMaxAirSupply()I"),
            cancellable = true
    )
    private void vanillahud$air(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        if (!Huds.INSTANCE.getAir().shouldRender()) ci.cancel();

        HudTransform.begin(guiGraphics, Huds.INSTANCE.getAir());
    }

    @Inject(
            method = "renderPlayerHealth",
            at = @At("TAIL")
    )
    private void vanillahud$airEnd(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        HudTransform.end(guiGraphics);
    }
    *///?} elif >=1.21.4 {
    @WrapMethod(
            //? if < 26 {
            /*method = "renderAirBubbles"
            *///?} else {
            method = "extractAirBubbles"
            //?}
    )
    private void vanillahud$air(GuiGraphicsExtractor graphics, Player player, int vehicleHearts, int yLineAir,
                                int xRight, Operation<Void> original) {
        if (!Huds.INSTANCE.getAir().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getAir());
        original.call(graphics, player, vehicleHearts, yLineAir, xRight);
        HudTransform.end(graphics);
    }
    //?}

    //? if <=1.21.5 {
    /*@WrapMethod(method = "renderExperienceBar")
    private void vanillahud$xpBar(GuiGraphicsExtractor graphics, int xpBarX, Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceBar().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getExperienceBar());
        original.call(graphics, xpBarX);
        HudTransform.end(graphics);
    }

    @WrapMethod(method = "renderExperienceLevel")
    private void vanillahud$xpLevel(GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceLevel().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getExperienceLevel());
        original.call(graphics, delta);
        HudTransform.end(graphics);
    }
    *///?}

    //? if >=1.21.6 && <26 {
    /*@WrapOperation(
            method = "renderHotbarAndDecorations",
            at = {
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;renderBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
                    ),
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;render(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
                    )
            }
    )
    private void vanillahud$xpBar(ContextualBar instance, GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker,
                                  Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceBar().shouldRender()) return;

        HudTransform.begin(guiGraphics, Huds.INSTANCE.getExperienceBar());
        original.call(instance, guiGraphics, deltaTracker);
        HudTransform.end(guiGraphics);
    }

    @WrapOperation(
            method = "renderHotbarAndDecorations",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"
            )
    )
    private void vanillahud$xpLevel(GuiGraphicsExtractor guiGraphics, Font font, int i, Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceLevel().shouldRender()) return;

        HudTransform.begin(guiGraphics, Huds.INSTANCE.getExperienceLevel());
        original.call(guiGraphics, font, i);
        HudTransform.end(guiGraphics);
    }
    *///?}

    //? if >=26 {
    @WrapOperation(
            method = "extractHotbarAndDecorations",
            at = {
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
                    ),
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
                    )
            }
    )
    private void vanillahud$xpBar(ContextualBar instance, GuiGraphicsExtractor graphics, DeltaTracker delta,
                                  Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceBar().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getExperienceBar());
        original.call(instance, graphics, delta);
        HudTransform.end(graphics);
    }

    @WrapOperation(
            method = "extractHotbarAndDecorations",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"
            )
    )
    private void vanillahud$xpLevel(GuiGraphicsExtractor graphics, Font font,
                                    int experienceLevel, Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceLevel().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getExperienceLevel());
        original.call(graphics, font, experienceLevel);
        HudTransform.end(graphics);
    }
    //?}
}
