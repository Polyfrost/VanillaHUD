package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if > 1.8.9
import net.minecraft.client.DeltaTracker;
//? if > 1.8.9
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
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
//?}
//? if >=1.21.2 <1.21.6 {
/*import net.minecraft.client.renderer.RenderType;
import java.util.function.Function;
*///?}

import net.minecraft.resources.Identifier;

//? if = 1.8.9 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Window;
import org.polyfrost.vanillahud.compat.LegacyDrawContext;
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
public abstract class GuiMixinHotbar {
    //? if > 1.8.9 {
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

    @Unique private static final Identifier VANILLAHUD$SELECTION = Identifier.withDefaultNamespace("hud/hotbar_selection");

    // half an item icon so the counter rotation can pivot on the icon centre
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
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V")
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
        if (vanillahud$active && VANILLAHUD$SELECTION.equals(sprite)) {
            x = graphics.guiWidth() / 2 - 92 + Math.round(vanillahud$animSlot * 20f);
        }
        original.call(graphics,
                //? if >=1.21.2 {
                pipeline,
                //?}
                sprite, x, y, width, height);
    }

    // items and their overlays are counter rotated so only the slot frames follow the element rotation
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
    //?} else {
    /*@Unique private boolean vanillahud$active;
    @Unique private float vanillahud$animSlot;
    @Unique private boolean vanillahud$animInit;
    @Unique private long vanillahud$animNanos;

    // half an item icon so the counter rotation can pivot on the icon centre
    @Unique private static final float VANILLAHUD$ITEM_HALF = 8f;

    @WrapMethod(method = "renderHotbar")
    private void vanillahud$hotbar(Window window, float tickDelta, Operation<Void> original) {
        HotbarHud hud = Huds.INSTANCE.getHotbar();
        if (!hud.shouldDraw()) return;

        HudTransform.begin(LegacyDrawContext.INSTANCE, hud);
        vanillahud$setup(hud);
        original.call(window, tickDelta);
        vanillahud$active = false;
        HudTransform.end(LegacyDrawContext.INSTANCE);
    }

     // 1.8.9 has no delta tracker to ask, so the slide is paced off wall clock time converted
     // into ticks to match the easing the newer versions use.
    @Unique
    private void vanillahud$setup(HotbarHud hud) {
        Player player = Minecraft.getInstance().player;
        int selected = player == null ? 0 : player.inventory.selectedSlot;

        long now = System.nanoTime();
        float elapsedTicks = vanillahud$animNanos == 0L ? 0f : (now - vanillahud$animNanos) / 50_000_000f;
        vanillahud$animNanos = now;

        if (!vanillahud$animInit) {
            vanillahud$animSlot = (float) selected;
            vanillahud$animInit = true;
        }
        if (!hud.getAnimation() || Math.abs((float) selected - vanillahud$animSlot) > 4.5f) {
            vanillahud$animSlot = (float) selected;
        } else {
            float t = Math.min(1f, elapsedTicks * 0.6f);
            vanillahud$animSlot += ((float) selected - vanillahud$animSlot) * t;
        }

        vanillahud$active = true;
    }

    // the second blit in renderHotbar is the selected slot frame
    @WrapOperation(
            method = "renderHotbar",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;drawTexture(IIIIII)V", ordinal = 1)
    )
    private void vanillahud$selection(GameGui self, int x, int y, int u, int v, int width, int height,
                                      Operation<Void> original) {
        if (vanillahud$active) {
            x = LegacyDrawContext.INSTANCE.guiWidth() / 2 - 92 + Math.round(vanillahud$animSlot * 20f);
        }
        original.call(self, x, y, u, v, width, height);
    }

    // items and their overlays are counter rotated so only the slot frames follow the rotation
    @WrapOperation(
            method = "renderHotbar",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;renderItemSlot(IIIFLnet/minecraft/world/entity/player/Player;)V")
    )
    private void vanillahud$slot(GameGui self, int slot, int x, int y, float tickDelta, Player player,
                                 Operation<Void> original) {
        HudTransform.beginUpright(LegacyDrawContext.INSTANCE, Huds.INSTANCE.getHotbar(),
                (float) x + VANILLAHUD$ITEM_HALF, (float) y + VANILLAHUD$ITEM_HALF);
        original.call(self, slot, x, y, tickDelta, player);
        HudTransform.endUpright(LegacyDrawContext.INSTANCE);
    }
    *///?}
}
