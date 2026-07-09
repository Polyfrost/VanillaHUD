package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.vanillahud.hud.HotbarHud;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.spongepowered.asm.mixin.Mixin;

//? if >=26.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
*///?} else {
import net.minecraft.client.gui.Gui;
//?}

//? if >=26.2 {
/*@Mixin(Hud.class)
*///?} else {
@Mixin(Gui.class)
//?}
public
//? if >=26.2 {
/*abstract
*///?}
class GuiMixinHotbar {
    @WrapMethod(
            //? if < 26 {
            /*method = "renderItemHotbar"
            *///?} else {
            method = "extractItemHotbar"
            //?}
    )
    private void vanillahud$hotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        HotbarHud hud = Huds.INSTANCE.getHotbar();
        if (!hud.shouldRender()) return;

        HudTransform.begin(graphics, hud);
        //? if >=26.2 {
        /*vanillahud$setup(graphics, deltaTracker, hud);
        original.call(graphics, deltaTracker);
        vanillahud$active = false;
        *///?} else {
        original.call(graphics, deltaTracker);
        //?}
        HudTransform.end(graphics);
    }

    //? if >=26.2 {
    /*@Invoker("getCameraPlayer")
    abstract Player vanillahud$getCameraPlayer();

    @Unique private static final Identifier VANILLAHUD$HOTBAR = Identifier.withDefaultNamespace("hud/hotbar");
    @Unique private static final Identifier VANILLAHUD$SELECTION = Identifier.withDefaultNamespace("hud/hotbar_selection");
    @Unique private static final Identifier VANILLAHUD$OFFHAND_LEFT = Identifier.withDefaultNamespace("hud/hotbar_offhand_left");
    @Unique private static final Identifier VANILLAHUD$OFFHAND_RIGHT = Identifier.withDefaultNamespace("hud/hotbar_offhand_right");
    @Unique private static final Identifier VANILLAHUD$ATTACK_BG = Identifier.withDefaultNamespace("hud/hotbar_attack_indicator_background");
    @Unique private static final Identifier VANILLAHUD$ATTACK_PROGRESS = Identifier.withDefaultNamespace("hud/hotbar_attack_indicator_progress");

    // Per-frame state shared with the wrapped blit/slot operations below.
    @Unique private boolean vanillahud$active;
    @Unique private boolean vanillahud$vertical;
    @Unique private int vanillahud$originX;
    @Unique private int vanillahud$originY;
    @Unique private int vanillahud$slotCall;
    @Unique private float vanillahud$animSlot;
    @Unique private boolean vanillahud$animInit;

    @Unique
    private void vanillahud$setup(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, HotbarHud hud) {
        vanillahud$vertical = hud.getVertical();
        int w = graphics.guiWidth();
        int h = graphics.guiHeight();
        vanillahud$originX = Math.round(hud.vanillaOriginX(w, h));
        vanillahud$originY = Math.round(hud.vanillaOriginY(w, h));
        vanillahud$slotCall = 0;

        Player player = vanillahud$getCameraPlayer();
        int selected = player == null ? 0 : player.getInventory().getSelectedSlot();
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
            method = "extractItemHotbar",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V")
    )
    private void vanillahud$blit(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite,
                                 int x, int y, int width, int height, Operation<Void> original) {
        if (!vanillahud$active) {
            original.call(graphics, pipeline, sprite, x, y, width, height);
            return;
        }

        boolean selection = sprite.equals(VANILLAHUD$SELECTION);

        if (!vanillahud$vertical) {
            if (selection) {
                x = graphics.guiWidth() / 2 - 92 + Math.round(vanillahud$animSlot * 20f);
            }
            original.call(graphics, pipeline, sprite, x, y, width, height);
            return;
        }

        int ox = vanillahud$originX;
        int oy = vanillahud$originY;
        if (sprite.equals(VANILLAHUD$HOTBAR)) {
            vanillahud$blitRotated(graphics, pipeline, sprite, ox + 11, oy + 91, width, height);
        } else if (selection) {
            vanillahud$blitRotated(graphics, pipeline, sprite, ox + 11, oy + 11 + Math.round(vanillahud$animSlot * 20f), width, height);
        } else if (sprite.equals(VANILLAHUD$ATTACK_BG)) {
            original.call(graphics, pipeline, sprite, ox + 24, oy + 82, width, height);
        } else if (sprite.equals(VANILLAHUD$OFFHAND_LEFT) || sprite.equals(VANILLAHUD$OFFHAND_RIGHT)) {
        } else {
            original.call(graphics, pipeline, sprite, x, y, width, height);
        }
    }

    @WrapOperation(
            method = "extractItemHotbar",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIIIIII)V")
    )
    private void vanillahud$blitProgress(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite,
                                         int texW, int texH, int u, int v, int x, int y, int width, int height,
                                         Operation<Void> original) {
        if (vanillahud$active && vanillahud$vertical) {
            x = vanillahud$originX + 24;
            y = vanillahud$originY + 82 + 18 - height;
        }
        original.call(graphics, pipeline, sprite, texW, texH, u, v, x, y, width, height);
    }

    @WrapOperation(
            method = "extractItemHotbar",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V")
    )
    private void vanillahud$slot(Hud self, GuiGraphicsExtractor graphics, int x, int y, DeltaTracker deltaTracker,
                                 Player player, ItemStack stack, int seed, Operation<Void> original) {
        if (!vanillahud$active || !vanillahud$vertical) {
            original.call(self, graphics, x, y, deltaTracker, player, stack, seed);
            return;
        }

        int i = vanillahud$slotCall++;
        int nx = vanillahud$originX + 3;
        int ny = i < 9 ? vanillahud$originY + 3 + i * 20 : vanillahud$originY + 186;
        original.call(self, graphics, nx, ny, deltaTracker, player, stack, seed);
    }

    @Unique
    private void vanillahud$blitRotated(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite, int cx, int cy, int w, int h) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate((float) cx, (float) cy);
        pose.rotate((float) (Math.PI / 2.0));
        graphics.blitSprite(pipeline, sprite, -w / 2, -h / 2, w, h);
        pose.popMatrix();
    }
    *///?}
}
