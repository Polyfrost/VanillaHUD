package org.polyfrost.vanillahud.mixin;

//? if <26 {

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiEditorMixin {

    @Shadow
    private Component overlayMessageString;
    @Shadow
    private int overlayMessageTime;
    @Shadow
    private boolean animateOverlayMessageColor;
    @Shadow
    private ItemStack lastToolHighlight;
    @Shadow
    private int toolHighlightTimer;
    @Shadow
    private Component title;
    @Shadow
    private Component subtitle;
    @Shadow
    private int titleTime;
    @Shadow
    private int titleFadeInTime;
    @Shadow
    private int titleStayTime;
    @Shadow
    private int titleFadeOutTime;

    @Unique
    private static boolean vanillahud$editing() {
        return HudManager.INSTANCE.isEditing();
    }

    @ModifyExpressionValue(
            method = "renderArmor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getArmorValue()I"))
    private static int vanillahud$forceArmor(int original) {
        return vanillahud$editing() && original <= 0 ? 20 : original;
    }

    @Inject(method = "renderOverlayMessage", at = @At("HEAD"))
    private void vanillahud$forceActionBar(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        if (vanillahud$editing() && (overlayMessageString == null || overlayMessageTime <= 0)) {
            overlayMessageString = Component.literal("Action Bar");
            overlayMessageTime = 60;
            animateOverlayMessageColor = false;
        }
    }

    @Inject(method = "renderSelectedItemName", at = @At("HEAD"))
    private void vanillahud$forceItemName(GuiGraphics graphics, CallbackInfo ci) {
        if (vanillahud$editing() && (toolHighlightTimer <= 0 || lastToolHighlight.isEmpty())) {
            lastToolHighlight = new ItemStack(Items.DIAMOND_SWORD);
            toolHighlightTimer = 10;
        }
    }

    @Inject(method = "renderTitle", at = @At("HEAD"))
    private void vanillahud$forceTitle(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        if (vanillahud$editing() && (title == null || titleTime <= 0)) {
            title = Component.literal("Title");
            subtitle = Component.literal("Subtitle");
            titleFadeInTime = 10;
            titleStayTime = 70;
            titleFadeOutTime = 20;
            titleTime = 90;
        }
    }
}
//?}

//? if >=26 {
/*import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;

    @Mixin(Gui.class)
    public abstract class GuiEditorMixin {
}*/
//?}
