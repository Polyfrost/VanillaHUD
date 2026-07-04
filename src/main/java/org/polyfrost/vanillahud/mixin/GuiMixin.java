package org.polyfrost.vanillahud.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
//? if >=1.21.6 {
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
//?}
import net.minecraft.world.entity.player.Player;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class GuiMixin {

    @WrapMethod(
            //? if < 26 {
            method = "renderArmor"
            //?} else {
            // method = "extractArmor"
            //?}
    )
    private static void vanillahud$armor(
            GuiGraphics graphics, Player player, int a, int b, int c, int d, Operation<Void> original) {
        if (!Huds.INSTANCE.getArmor().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getArmor());
        original.call(graphics, player, a, b, c, d);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            method = "renderItemHotbar"
            //?} else {
            // method = "extractItemHotbar"
            //?}
    )
    private void vanillahud$hotbar(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        if (!Huds.INSTANCE.getHotbar().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getHotbar());
        original.call(graphics, delta);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            method = "renderOverlayMessage"
            //?} else {
            // method = "extractOverlayMessage"
            //?}
    )
    private void vanillahud$actionBar(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        if (!Huds.INSTANCE.getActionBar().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getActionBar());
        original.call(graphics, delta);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            method = "renderSelectedItemName"
            //?} else {
            // method = "extractSelectedItemName"
            //?}
    )
    private void vanillahud$itemName(GuiGraphics graphics, Operation<Void> original) {
        if (!Huds.INSTANCE.getHeldItemTooltip().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getHeldItemTooltip());
        original.call(graphics);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            method = "renderTitle"
            //?} else {
            // method = "extractTitle"
            //?}
    )
    private void vanillahud$title(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        if (!Huds.INSTANCE.getTitle().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getTitle());
        original.call(graphics, delta);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            method = "renderScoreboardSidebar"
            //?} else {
            // method = "extractScoreboardSidebar"
            //?}
    )
    private void vanillahud$scoreboard(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        if (!Huds.INSTANCE.getScoreboard().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getScoreboard());
        original.call(graphics, delta);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            method = "renderTabList"
            //?} else {
            // method = "extractTabList"
            //?}
    )
    private void vanillahud$tabList(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        if (!Huds.INSTANCE.getTabList().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getTabList());
        original.call(graphics, delta);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            method = "renderVehicleHealth"
            //?} else {
            // method = "extractVehicleHealth"
            //?}
    )
    private void vanillahud$mount(GuiGraphics graphics, Operation<Void> original) {
        if (!Huds.INSTANCE.getMountHealth().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getMountHealth());
        original.call(graphics);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            method = "renderHearts"
            //?} else {
            // method = "extractHearts"
            //?}
    )
    private void vanillahud$health(
            GuiGraphics graphics, Player player, int x, int y, int height,
            int offsetHeartIndex, float maxHealth, int currentHealth, int displayHealth,
            int absorptionAmount, boolean renderHighlight, Operation<Void> original) {
        if (!Huds.INSTANCE.getHealth().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getHealth());
        original.call(graphics, player, x, y, height, offsetHeartIndex, maxHealth,
                currentHealth, displayHealth, absorptionAmount, renderHighlight);
        HudTransform.end(graphics);
    }

    @WrapMethod(
            //? if < 26 {
            method = "renderFood"
            //?} else {
            // method = "extractFood"
            //?}
    )
    private void vanillahud$hunger(
            GuiGraphics graphics, Player player, int a, int b, Operation<Void> original) {
        if (!Huds.INSTANCE.getHunger().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getHunger());
        original.call(graphics, player, a, b);
        HudTransform.end(graphics);
    }

    //? if 1.21.1 {
    /*@Inject(
            method = "renderPlayerHealth",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getMaxAirSupply()I"),
            cancellable = true
    )
    private void vanillahud$air(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (!Huds.INSTANCE.getAir().shouldRender()) ci.cancel();

        HudTransform.begin(guiGraphics, Huds.INSTANCE.getAir());
    }

    @Inject(
            method = "renderPlayerHealth",
            at = @At("TAIL")
    )
    private void vanillahud$airEnd(GuiGraphics guiGraphics, CallbackInfo ci) {
        HudTransform.end(guiGraphics);
    }
    *///?} elif >=1.21.4 {
    @WrapMethod(
            //? if < 26 {
            method = "renderAirBubbles"
            //?} else {
            // method = "extractAirBubbles"
            //?}
    )
    private void vanillahud$air(GuiGraphics graphics, Player player, int a, int b, int c, Operation<Void> original) {
        if (!Huds.INSTANCE.getAir().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getAir());
        original.call(graphics, player, a, b, c);
        HudTransform.end(graphics);
    }
    //?}

    //? if <=1.21.5 {
    /*@WrapMethod(method = "renderExperienceBar")
    private void vanillahud$xpBar(GuiGraphics graphics, int xpBarX, Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceBar().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getExperienceBar());
        original.call(graphics, xpBarX);
        HudTransform.end(graphics);
    }

    @WrapMethod(method = "renderExperienceLevel")
    private void vanillahud$xpLevel(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceLevel().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getExperienceLevel());
        original.call(graphics, delta);
        HudTransform.end(graphics);
    }
    *///?}

    //? if >=1.21.6 && <26 {
    @WrapOperation(
            method = "renderHotbarAndDecorations",
            at = {
                    @At(
                            value = "INVOKE",
                            // these method signatures are getting out of hand... I really gotta buy an ultrawide monitor.
                            target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"
                    ),
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"
                    )
            }
    )
    private void vanillahud$xpBar(ContextualBarRenderer instance, GuiGraphics guiGraphics, DeltaTracker deltaTracker,
                                  Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceBar().shouldRender()) return;

        HudTransform.begin(guiGraphics, Huds.INSTANCE.getExperienceBar());
        original.call(instance, guiGraphics, deltaTracker);
        HudTransform.end(guiGraphics);
    }

    @WrapOperation(
            method = "renderHotbarAndDecorations",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"
            )
    )
    private void vanillahud$xpLevel(GuiGraphics guiGraphics, Font font, int i, Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceLevel().shouldRender()) return;

        HudTransform.begin(guiGraphics, Huds.INSTANCE.getExperienceLevel());
        original.call(guiGraphics, font, i);
        HudTransform.end(guiGraphics);
    }
    //?}

    //? if >=26 {
    /*@WrapOperation(
            method = "extractHotbarAndDecorations",
            at = {
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;extractBackground(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"
                    ),
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;extractRenderState(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"
                    )
            }
    )
    private void vanillahud$xpBar(ContextualBarRenderer instance, GuiGraphics graphics, DeltaTracker delta,
                                  Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceBar().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getExperienceBar());
        original.call(instance, graphics, delta);
        HudTransform.end(graphics);
    }

    @WrapOperation(
            method = "extractHotbarAndDecorations",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"
            )
    )
    private void vanillahud$xpLevel(GuiGraphics graphics, Font font, int i, Operation<Void> original) {
        if (!Huds.INSTANCE.getExperienceLevel().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getExperienceLevel());
        original.call(graphics, font, i);
        HudTransform.end(graphics);
    }
    *///?}
}
