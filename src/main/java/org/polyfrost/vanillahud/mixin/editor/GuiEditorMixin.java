package org.polyfrost.vanillahud.mixin.editor;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.Objective;
import org.objectweb.asm.Opcodes;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.vanillahud.util.DemoData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
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
    private boolean vanillahud$forcedActionBar;
    @Unique
    private boolean vanillahud$forcedItemName;
    @Unique
    private boolean vanillahud$forcedTitle;

    @Unique
    private static boolean vanillahud$editing() {
        return HudManager.INSTANCE.isEditing();
    }

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "renderArmor",
            *///?} else {
             method = "extractArmor",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getArmorValue()I"))
    private static int vanillahud$forceArmor(int original) {
        return vanillahud$editing() && original <= 0 ? 20 : original;
    }

    @Inject(
            //? if <26 {
            /*method = "renderOverlayMessage",
            *///?} else {
             method = "extractOverlayMessage",
            //?}
            at = @At("HEAD"))
    private void vanillahud$forceActionBar(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        if (vanillahud$editing()) {
            if (!vanillahud$forcedActionBar) {
                overlayMessageString = Component.literal("Action Bar");
                animateOverlayMessageColor = false;
                vanillahud$forcedActionBar = true;
            }
            overlayMessageTime = 60;
        } else if (vanillahud$forcedActionBar) {
            overlayMessageTime = 0;
            vanillahud$forcedActionBar = false;
        }
    }

    @Inject(
            //? if <26 {
            /*method = "renderSelectedItemName",
            *///?} else {
             method = "extractSelectedItemName",
            //?}
            at = @At("HEAD"))
    private void vanillahud$forceItemName(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (vanillahud$editing()) {
            lastToolHighlight = new ItemStack(Items.DIAMOND_SWORD);
            vanillahud$forcedItemName = true;
            toolHighlightTimer = 100;
        } else if (vanillahud$forcedItemName) {
            toolHighlightTimer = 0;
            vanillahud$forcedItemName = false;
        }
    }

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "renderSelectedItemName",
            *///?} else {
             method = "extractSelectedItemName",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;canHurtPlayer()Z"))
    private boolean vanillahud$forceItemNameOffset(boolean original) {
        return vanillahud$editing() || original;
    }

    @Inject(
            //? if <26 {
            /*method = "renderTitle",
            *///?} else {
             method = "extractTitle",
            //?}
            at = @At("HEAD"))
    private void vanillahud$forceTitle(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        if (vanillahud$editing()) {
            if (!vanillahud$forcedTitle) {
                title = Component.literal("Title");
                subtitle = Component.literal("Subtitle");
                titleFadeInTime = 10;
                titleStayTime = 70;
                titleFadeOutTime = 20;
                vanillahud$forcedTitle = true;
            }
            titleTime = titleFadeOutTime + titleStayTime;
        } else if (vanillahud$forcedTitle) {
            titleTime = 0;
            vanillahud$forcedTitle = false;
        }
    }

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "renderHotbarAndDecorations",
            *///?} else {
             method = "extractHotbarAndDecorations",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;canHurtPlayer()Z"))
    private boolean vanillahud$forceHealthHungerAir(boolean original) {
        return vanillahud$editing() || original;
    }

    @ModifyExpressionValue(
            //? if 1.21.1 {
            /*method = "renderPlayerHealth",
            *///?} elif <26 {
            /*method = "renderAirBubbles",
            *///?} else {
             method = "extractAirBubbles",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAirSupply()I"))
    private int vanillahud$forceAir(int original) {
        return vanillahud$editing() ? 200 : original;
    }

    //? if <=1.21.5 {
    /*@ModifyExpressionValue(method = "renderExperienceLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;isExperienceBarVisible()Z"))
    private boolean vanillahud$forceXpLevelVisible(boolean original) {
        return vanillahud$editing() || original;
    }

    @ModifyExpressionValue(method = "renderExperienceLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;experienceLevel:I", opcode = Opcodes.GETFIELD))
    private int vanillahud$forceXpLevel(int original) {
        return vanillahud$editing() && original <= 0 ? 30 : original;
    }
    *///?} else {
    @ModifyExpressionValue(
            //? if <26 {
            /*method = "renderHotbarAndDecorations",
            *///?} else {
             method = "extractHotbarAndDecorations",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasExperience()Z"))
    private boolean vanillahud$forceXpHasExperience(boolean original) {
        return vanillahud$editing() || original;
    }

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "renderHotbarAndDecorations",
            *///?} else {
             method = "extractHotbarAndDecorations",
            //?}
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;experienceLevel:I", opcode = Opcodes.GETFIELD))
    private int vanillahud$forceXpLevel(int original) {
        return vanillahud$editing() && original <= 0 ? 30 : original;
    }
    //?}

    //? if <=1.21.5 {
    /*@ModifyExpressionValue(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;isExperienceBarVisible()Z"))
    private boolean vanillahud$forceXpBar(boolean original) {
        return vanillahud$editing() || original;
    }
    *///?} else {
    @ModifyExpressionValue(method = "nextContextualInfoState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasExperience()Z"))
    private boolean vanillahud$forceXpBar(boolean original) {
        return vanillahud$editing() || original;
    }
    //?}

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "renderTabList",
            *///?} else {
            method = "extractTabList",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    private boolean vanillahud$forceTabListKey(boolean original) {
        return vanillahud$editing() || original;
    }

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "renderTabList",
            *///?} else {
            method = "extractTabList",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isLocalServer()Z"))
    private boolean vanillahud$forceTabListLocal(boolean original) {
        return !vanillahud$editing() && original;
    }

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "renderScoreboardSidebar",
            *///?} else {
            method = "extractScoreboardSidebar",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;getDisplayObjective(Lnet/minecraft/world/scores/DisplaySlot;)Lnet/minecraft/world/scores/Objective;", ordinal = 1))
    private Objective vanillahud$forceScoreboard(Objective original) {
        return vanillahud$editing() && original == null ? DemoData.demoScoreboardObjective() : original;
    }

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "renderEffects",
            *///?} else {
            method = "extractEffects",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getActiveEffects()Ljava/util/Collection;")
    )
    private Collection<MobEffectInstance> vanillahud$forceEffects(Collection<MobEffectInstance> original) {
        if (HudManager.INSTANCE.isEditing() && original.isEmpty()) return DemoData.demoEffects();
        return original;
    }
}
