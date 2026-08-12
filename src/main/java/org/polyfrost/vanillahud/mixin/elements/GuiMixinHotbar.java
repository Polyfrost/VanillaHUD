package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.polyfrost.vanillahud.hud.HotbarHud;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;

//? if >=1.21.6 {
import com.mojang.blaze3d.pipeline.RenderPipeline;
//?}
//? if >=1.21.2 <1.21.6 {
/*import net.minecraft.client.renderer.RenderType;
import java.util.function.Function;
*///?}

import net.minecraft.resources.Identifier;

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
public abstract class GuiMixinHotbar {
    @WrapMethod(
            //? if < 26 {
            /*method = "renderItemHotbar"
            *///?} else {
            method = "extractItemHotbar"
            //?}
    )
    private void vanillahud$hotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        HotbarHud hud = Huds.INSTANCE.getHotbar();
        if (!hud.shouldDraw()) return;

        HudTransform.begin(graphics, hud);
        vanillahud$setup(deltaTracker, hud);
        original.call(graphics, deltaTracker);
        vanillahud$active = false;
        HudTransform.end(graphics);
    }

    @Invoker("getCameraPlayer")
    abstract Player vanillahud$getCameraPlayer();

    @Unique private static final String VANILLAHUD$SELECTION = "minecraft:hud/hotbar_selection";

    /** half an item icon so the counter rotation can pivot on the icon centre */
    @Unique private static final float VANILLAHUD$ITEM_HALF = 8f;

    @Unique private boolean vanillahud$active;
    @Unique private float vanillahud$animSlot;
    @Unique private boolean vanillahud$animInit;

    @Unique
    private void vanillahud$setup(DeltaTracker deltaTracker, HotbarHud hud) {
        Player player = vanillahud$getCameraPlayer();
        int selected;
        if (player == null) {
            selected = 0;
        } else {
            //? if >=1.21.5 {
            selected = player.getInventory().getSelectedSlot();
            //?} else {
            /*selected = player.getInventory().selected;
            *///?}
        }
        if (!vanillahud$animInit) {
            vanillahud$animSlot = (float) selected;
            vanillahud$animInit = true;
        }
        if (!hud.getAnimation() || Math.abs((float) selected - vanillahud$animSlot) > 4.5f) {
            vanillahud$animSlot = (float) selected;
        } else {
            float t = Math.min(1f, deltaTracker.getRealtimeDeltaTicks() * 0.6f);
            vanillahud$animSlot += ((float) selected - vanillahud$animSlot) * t;
        }

        vanillahud$active = true;
    }

    @WrapOperation(
            //? if < 26 {
            /*method = "renderItemHotbar",
            *///?} else {
            method = "extractItemHotbar",
            //?}
            //? if >=1.21.6 {
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V")
            //?} else if >=1.21.2 {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/Identifier;IIII)V")
            *///?} else {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lnet/minecraft/resources/Identifier;IIII)V")
            *///?}
    )
    private void vanillahud$blit(GuiGraphicsExtractor graphics,
            //? if >=1.21.6 {
            RenderPipeline pipeline,
            //?} else if >=1.21.2 {
            /*Function<Identifier, RenderType> pipeline,
            *///?}
            Identifier sprite,
            int x, int y, int width, int height, Operation<Void> original) {
        if (vanillahud$active && sprite.toString().equals(VANILLAHUD$SELECTION)) {
            x = graphics.guiWidth() / 2 - 92 + Math.round(vanillahud$animSlot * 20f);
        }
        original.call(graphics,
                //? if >=1.21.2 {
                pipeline,
                //?}
                sprite, x, y, width, height);
    }

    /** items and their overlays are counter rotated so only the slot frames follow the element rotation */
    @WrapOperation(
            //? if < 26 {
            /*method = "renderItemHotbar",
            *///?} else {
            method = "extractItemHotbar",
            //?}
            //? if >=26.2 {
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V")
            //?} else if >=26 {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V")
            *///?} else {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V")
            *///?}
    )
    private void vanillahud$slot(
            //? if >=26.2 {
            Hud self,
            //?} else {
            /*Gui self,
            *///?}
            GuiGraphicsExtractor graphics, int x, int y, DeltaTracker deltaTracker,
            Player player, ItemStack stack, int seed, Operation<Void> original) {
        HudTransform.beginUpright(graphics, Huds.INSTANCE.getHotbar(),
                (float) x + VANILLAHUD$ITEM_HALF, (float) y + VANILLAHUD$ITEM_HALF);
        original.call(self, graphics, x, y, deltaTracker, player, stack, seed);
        HudTransform.endUpright(graphics);
    }
}
