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
import org.joml.Matrix3x2fStack;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
*///?}
//? if >=1.21.2 <1.21.6 {
/*import net.minecraft.client.renderer.RenderType;
import java.util.function.Function;
*///?}

//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}

//? if >=26.2 {
/*import net.minecraft.client.gui.Hud;
*///?} else {
import net.minecraft.client.gui.Gui;
//?}

//? if >=26.2 {
/*@Mixin(Hud.class)
*///?} else {
@Mixin(Gui.class)
//?}
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
        if (!hud.shouldRender()) return;

        HudTransform.begin(graphics, hud);
        vanillahud$setup(graphics, deltaTracker, hud);
        original.call(graphics, deltaTracker);
        vanillahud$active = false;
        HudTransform.end(graphics);
    }

    @Invoker("getCameraPlayer")
    abstract Player vanillahud$getCameraPlayer();

    @Unique private static final String VANILLAHUD$HOTBAR = "minecraft:hud/hotbar";
    @Unique private static final String VANILLAHUD$SELECTION = "minecraft:hud/hotbar_selection";
    @Unique private static final String VANILLAHUD$OFFHAND_LEFT = "minecraft:hud/hotbar_offhand_left";
    @Unique private static final String VANILLAHUD$OFFHAND_RIGHT = "minecraft:hud/hotbar_offhand_right";
    @Unique private static final String VANILLAHUD$ATTACK_BG = "minecraft:hud/hotbar_attack_indicator_background";

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
            //? if >=1.21.11 {
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V")
            //?} else if >=1.21.6 {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V")
            *///?} else if >=1.21.2 {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIII)V")
            *///?} else {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V")
            *///?}
    )
    private void vanillahud$blit(GuiGraphicsExtractor graphics,
            //? if >=1.21.6 {
            RenderPipeline pipeline,
            //?} else if >=1.21.2 {
            /*Function<ResourceLocation, RenderType> pipeline,
            *///?}
            //? if >=1.21.11 {
            Identifier sprite,
            //?} else {
            /*ResourceLocation sprite,
            *///?}
            int x, int y, int width, int height, Operation<Void> original) {
        if (!vanillahud$active) {
            original.call(graphics,
                    //? if >=1.21.2 {
                    pipeline,
                    //?}
                    sprite, x, y, width, height);
            return;
        }

        String id = sprite.toString();
        boolean selection = id.equals(VANILLAHUD$SELECTION);

        if (!vanillahud$vertical) {
            if (selection) {
                x = graphics.guiWidth() / 2 - 92 + Math.round(vanillahud$animSlot * 20f);
            }
            original.call(graphics,
                    //? if >=1.21.2 {
                    pipeline,
                    //?}
                    sprite, x, y, width, height);
            return;
        }

        int ox = vanillahud$originX;
        int oy = vanillahud$originY;
        if (id.equals(VANILLAHUD$HOTBAR)) {
            vanillahud$blitRotated(graphics,
                    //? if >=1.21.2 {
                    pipeline,
                    //?}
                    sprite, ox + 11, oy + 91, width, height);
        } else if (selection) {
            vanillahud$blitRotated(graphics,
                    //? if >=1.21.2 {
                    pipeline,
                    //?}
                    sprite, ox + 11, oy + 11 + Math.round(vanillahud$animSlot * 20f), width, height);
        } else if (id.equals(VANILLAHUD$ATTACK_BG)) {
            original.call(graphics,
                    //? if >=1.21.2 {
                    pipeline,
                    //?}
                    sprite, ox + 24, oy + 82, width, height);
        } else if (id.equals(VANILLAHUD$OFFHAND_LEFT) || id.equals(VANILLAHUD$OFFHAND_RIGHT)) {
        } else {
            original.call(graphics,
                    //? if >=1.21.2 {
                    pipeline,
                    //?}
                    sprite, x, y, width, height);
        }
    }

    @WrapOperation(
            //? if < 26 {
            /*method = "renderItemHotbar",
            *///?} else {
            method = "extractItemHotbar",
            //?}
            //? if >=1.21.11 {
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIIIIII)V")
            //?} else if >=1.21.6 {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V")
            *///?} else if >=1.21.2 {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V")
            *///?} else {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V")
            *///?}
    )
    private void vanillahud$blitProgress(GuiGraphicsExtractor graphics,
            //? if >=1.21.6 {
            RenderPipeline pipeline,
            //?} else if >=1.21.2 {
            /*Function<ResourceLocation, RenderType> pipeline,
            *///?}
            //? if >=1.21.11 {
            Identifier sprite,
            //?} else {
            /*ResourceLocation sprite,
            *///?}
            int texW, int texH, int u, int v, int x, int y, int width, int height,
            Operation<Void> original) {
        if (vanillahud$active && vanillahud$vertical) {
            x = vanillahud$originX + 24;
            y = vanillahud$originY + 82 + 18 - height;
        }
        original.call(graphics,
                //? if >=1.21.2 {
                pipeline,
                //?}
                sprite, texW, texH, u, v, x, y, width, height);
    }

    @WrapOperation(
            //? if < 26 {
            /*method = "renderItemHotbar",
            *///?} else {
            method = "extractItemHotbar",
            //?}
            //? if >=26.2 {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V")
            *///?} else if >=26 {
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V")
            //?} else {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V")
            *///?}
    )
    private void vanillahud$slot(
            //? if >=26.2 {
            /*Hud self,
            *///?} else {
            Gui self,
            //?}
            GuiGraphicsExtractor graphics, int x, int y, DeltaTracker deltaTracker,
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
    private void vanillahud$blitRotated(GuiGraphicsExtractor graphics,
            //? if >=1.21.6 {
            RenderPipeline pipeline,
            //?} else if >=1.21.2 {
            /*Function<ResourceLocation, RenderType> pipeline,
            *///?}
            //? if >=1.21.11 {
            Identifier sprite,
            //?} else {
            /*ResourceLocation sprite,
            *///?}
            int cx, int cy, int w, int h) {
        //? if >=1.21.6 {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate((float) cx, (float) cy);
        pose.rotate((float) (Math.PI / 2.0));
        graphics.blitSprite(pipeline, sprite, -w / 2, -h / 2, w, h);
        pose.popMatrix();
        //?} else {
        /*PoseStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate((float) cx, (float) cy, 0f);
        pose.mulPose(Axis.ZP.rotation((float) (Math.PI / 2.0)));
        graphics.blitSprite(
                //? if >=1.21.2 {
                pipeline,
                //?}
                sprite, -w / 2, -h / 2, w, h);
        pose.popMatrix();
        *///?}
    }
}
