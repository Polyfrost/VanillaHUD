package org.polyfrost.vanillahud.mixin.editor;

//? if > 1.8.9 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//? if > 1.8.9
import net.minecraft.client.DeltaTracker;
//? if > 1.8.9
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} elif > 1.8.9 {
/*import net.minecraft.client.gui.Gui;
*///?} else {
/*import net.minecraft.client.gui.GameGui;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.Objective;
import org.objectweb.asm.Opcodes;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.hud.VanillaHud;
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
//?} elif > 1.8.9 {
/*@Mixin(Gui.class)
*///?} else {
/*@Mixin(GameGui.class)
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
    private static boolean vanillahud$editing(VanillaHud hud) {
        return VanillaHud.previewing(hud);
    }

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "renderArmor",
            *///?} else {
             method = "extractArmor",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getArmorValue()I"))
    private static int vanillahud$forceArmor(int original) {
        return vanillahud$editing(Huds.INSTANCE.getHotbar()) && original <= 0 ? 20 : original;
    }

    @Inject(
            //? if <26 {
            /*method = "renderOverlayMessage",
            *///?} else {
             method = "extractOverlayMessage",
            //?}
            at = @At("HEAD"))
    private void vanillahud$forceActionBar(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        if (vanillahud$editing(Huds.INSTANCE.getActionBar())) {
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
        if (vanillahud$editing(Huds.INSTANCE.getHeldItemTooltip())) {
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
        return vanillahud$editing(Huds.INSTANCE.getHeldItemTooltip()) || original;
    }

    @Inject(
            //? if <26 {
            /*method = "renderTitle",
            *///?} else {
             method = "extractTitle",
            //?}
            at = @At("HEAD"))
    private void vanillahud$forceTitle(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        if (vanillahud$editing(Huds.INSTANCE.getTitle())) {
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
        return vanillahud$editing(Huds.INSTANCE.getHotbar()) || original;
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
        return vanillahud$editing(Huds.INSTANCE.getHotbar()) ? 200 : original;
    }

    //? if <=1.21.5 {
    /*@ModifyExpressionValue(method = "renderExperienceLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;isExperienceBarVisible()Z"))
    private boolean vanillahud$forceXpLevelVisible(boolean original) {
        return vanillahud$editing(Huds.INSTANCE.getHotbar()) || original;
    }

    @ModifyExpressionValue(method = "renderExperienceLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;experienceLevel:I", opcode = Opcodes.GETFIELD))
    private int vanillahud$forceXpLevel(int original) {
        return vanillahud$editing(Huds.INSTANCE.getHotbar()) && original <= 0 ? 30 : original;
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
        return vanillahud$editing(Huds.INSTANCE.getHotbar()) || original;
    }

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "renderHotbarAndDecorations",
            *///?} else {
             method = "extractHotbarAndDecorations",
            //?}
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;experienceLevel:I", opcode = Opcodes.GETFIELD))
    private int vanillahud$forceXpLevel(int original) {
        return vanillahud$editing(Huds.INSTANCE.getHotbar()) && original <= 0 ? 30 : original;
    }
    //?}

    //? if <=1.21.5 {
    /*@ModifyExpressionValue(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;isExperienceBarVisible()Z"))
    private boolean vanillahud$forceXpBar(boolean original) {
        return vanillahud$editing(Huds.INSTANCE.getHotbar()) || original;
    }
    *///?} else {
    @ModifyExpressionValue(method = "nextContextualInfoState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasExperience()Z"))
    private boolean vanillahud$forceXpBar(boolean original) {
        return vanillahud$editing(Huds.INSTANCE.getHotbar()) || original;
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
        return vanillahud$editing(Huds.INSTANCE.getTabList()) || original;
    }

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "renderTabList",
            *///?} else {
            method = "extractTabList",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isLocalServer()Z"))
    private boolean vanillahud$forceTabListLocal(boolean original) {
        return !vanillahud$editing(Huds.INSTANCE.getTabList()) && original;
    }

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "renderScoreboardSidebar",
            *///?} else {
            method = "extractScoreboardSidebar",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;getDisplayObjective(Lnet/minecraft/world/scores/DisplaySlot;)Lnet/minecraft/world/scores/Objective;", ordinal = 1))
    private Objective vanillahud$forceScoreboard(Objective original) {
        return vanillahud$editing(Huds.INSTANCE.getScoreboard()) ? DemoData.demoScoreboardObjective() : original;
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
        if (vanillahud$editing(Huds.INSTANCE.getStatusEffects())) return DemoData.demoEffects();
        return original;
    }
}
//?} else {
/*// 1.8.9 has no extract pass: the action bar and the title are drawn inline in GameGui.render
// and the rest hang off sub methods, so the demo state is pushed in at the head of the method
// that draws each element and the expressions that gate it are forced open. Every target below
// was read off the 1.8.9 bytecode.
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.BossBar;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.render.Window;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.hud.VanillaHud;
import org.polyfrost.vanillahud.util.DemoData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameGui.class)
public abstract class GuiEditorMixin {

    @Shadow private String overlayMessage;
    @Shadow private int overlayMessageCooldown;
    @Shadow private boolean overlayMessageTinted;
    @Shadow private ItemStack selectedItem;
    @Shadow private int itemSelectedTimer;
    @Shadow private String title;
    @Shadow private String subtitle;
    @Shadow private int titleTime;
    @Shadow private int titleFadeInTime;
    @Shadow private int titleDuration;
    @Shadow private int titleFadeOutTime;

    @Unique
    private boolean vanillahud$forcedActionBar;
    @Unique
    private boolean vanillahud$forcedItemName;
    @Unique
    private boolean vanillahud$forcedTitle;

    @Unique
    private static boolean vanillahud$editing(VanillaHud hud) {
        return VanillaHud.previewing(hud);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void vanillahud$forceActionBar(float tickDelta, CallbackInfo ci) {
        if (vanillahud$editing(Huds.INSTANCE.getActionBar())) {
            if (!vanillahud$forcedActionBar) {
                this.overlayMessage = "Action Bar";
                this.overlayMessageTinted = false;
                vanillahud$forcedActionBar = true;
            }
            this.overlayMessageCooldown = 60;
        } else if (vanillahud$forcedActionBar) {
            this.overlayMessageCooldown = 0;
            vanillahud$forcedActionBar = false;
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void vanillahud$forceTitle(float tickDelta, CallbackInfo ci) {
        if (vanillahud$editing(Huds.INSTANCE.getTitle())) {
            if (!vanillahud$forcedTitle) {
                this.title = "Title";
                this.subtitle = "Subtitle";
                this.titleFadeInTime = 10;
                this.titleDuration = 70;
                this.titleFadeOutTime = 20;
                vanillahud$forcedTitle = true;
            }
            this.titleTime = this.titleFadeOutTime + this.titleDuration;
        } else if (vanillahud$forcedTitle) {
            this.titleTime = 0;
            vanillahud$forcedTitle = false;
        }
    }

    // the held item name sits behind a video setting as well as its own timer
    @ModifyExpressionValue(method = "render", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/options/GameOptions;itemInHandTooltips:Z", opcode = Opcodes.GETFIELD))
    private boolean vanillahud$forceItemNameEnabled(boolean original) {
        return vanillahud$editing(Huds.INSTANCE.getHeldItemTooltip()) || original;
    }

    @Inject(method = "renderSelectedItemName", at = @At("HEAD"))
    private void vanillahud$forceItemName(Window window, CallbackInfo ci) {
        if (vanillahud$editing(Huds.INSTANCE.getHeldItemTooltip())) {
            this.selectedItem = new ItemStack(Items.DIAMOND_SWORD);
            this.itemSelectedTimer = 100;
            vanillahud$forcedItemName = true;
        } else if (vanillahud$forcedItemName) {
            this.itemSelectedTimer = 0;
            vanillahud$forcedItemName = false;
        }
    }

    // vanilla drops the name by 14 when the status bars are hidden, so the preview keeps the
    // in game offset the same way the newer versions force canHurtPlayer
    @ModifyExpressionValue(method = "renderSelectedItemName", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/ClientPlayerInteractionManager;hasStatusBars()Z"))
    private boolean vanillahud$forceItemNameOffset(boolean original) {
        return vanillahud$editing(Huds.INSTANCE.getHeldItemTooltip()) || original;
    }

    // the bottom cluster: the status bars and the experience bar are gated on the game mode,
    // and the armour row, the air row and the level only draw when the player has them
    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/ClientPlayerInteractionManager;hasStatusBars()Z"))
    private boolean vanillahud$forceStatusBars(boolean original) {
        return vanillahud$editing(Huds.INSTANCE.getHotbar()) || original;
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/ClientPlayerInteractionManager;hasXpBar()Z"))
    private boolean vanillahud$forceXpBar(boolean original) {
        return vanillahud$editing(Huds.INSTANCE.getHotbar()) || original;
    }

    @ModifyExpressionValue(method = "renderStatusBars", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getArmorProtection()I"))
    private int vanillahud$forceArmor(int original) {
        return vanillahud$editing(Huds.INSTANCE.getHotbar()) && original <= 0 ? 20 : original;
    }

    @ModifyExpressionValue(method = "renderStatusBars", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;isSubmergedIn(Lnet/minecraft/block/material/Material;)Z"))
    private boolean vanillahud$forceAirVisible(boolean original) {
        return vanillahud$editing(Huds.INSTANCE.getHotbar()) || original;
    }

    @ModifyExpressionValue(method = "renderStatusBars", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;getBreath()I"))
    private int vanillahud$forceAir(int original) {
        return vanillahud$editing(Huds.INSTANCE.getHotbar()) ? 200 : original;
    }

    // both reads gate the level text, so neither carries an ordinal on purpose
    @ModifyExpressionValue(method = "renderXpBar", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/player/LocalPlayer;xpLevel:I", opcode = Opcodes.GETFIELD))
    private int vanillahud$forceXpLevel(int original) {
        return vanillahud$editing(Huds.INSTANCE.getHotbar()) && original <= 0 ? 30 : original;
    }

    // the tab list is hidden unless the key is held, and hidden again on a single player
    // integrated server with no tab objective
    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/options/KeyBinding;isPressed()Z"))
    private boolean vanillahud$forceTabListKey(boolean original) {
        return vanillahud$editing(Huds.INSTANCE.getTabList()) || original;
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;isIntegratedServerRunning()Z"))
    private boolean vanillahud$forceTabListLocal(boolean original) {
        return !vanillahud$editing(Huds.INSTANCE.getTabList()) && original;
    }

    // ordinal 1 is the sidebar lookup: 0 is the team coloured slot and 2 feeds the tab list
    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/scoreboard/Scoreboard;getDisplayObjective(I)Lnet/minecraft/scoreboard/ScoreboardObjective;",
            ordinal = 1))
    private ScoreboardObjective vanillahud$forceScoreboard(ScoreboardObjective original) {
        return vanillahud$editing(Huds.INSTANCE.getScoreboard()) ? DemoData.demoScoreboardObjective() : original;
    }

    // the one 1.8.9 boss bar is static state the server owns and the method decrements its
    // timer, so the preview swaps the values in around the call instead of writing over them
    @WrapMethod(method = "renderBossBars")
    private void vanillahud$forceBossBar(Operation<Void> original) {
        if (!vanillahud$editing(Huds.INSTANCE.getBossBar())) {
            original.call();
            return;
        }
        String name = BossBar.name;
        int timer = BossBar.timer;
        float health = BossBar.health;
        BossBar.name = DemoData.demoBossName();
        BossBar.timer = 100;
        BossBar.health = 0.67F;
        try {
            original.call();
        } finally {
            BossBar.name = name;
            BossBar.timer = timer;
            BossBar.health = health;
        }
    }
}
*///?}
