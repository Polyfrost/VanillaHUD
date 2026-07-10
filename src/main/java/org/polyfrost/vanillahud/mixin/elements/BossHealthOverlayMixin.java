package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.render.HudTransform;
import org.polyfrost.vanillahud.util.DemoData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;

@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayMixin {
    //? if <1.21.4 {
    /*@WrapMethod(method = "render")
    private void vanillahud$boss(GuiGraphicsExtractor graphics, Operation<Void> original) {
        if (!Huds.INSTANCE.getBossBar().shouldRender()) return;

        HudTransform.begin(graphics, Huds.INSTANCE.getBossBar());
        original.call(graphics);
        HudTransform.end(graphics);
    }
    *///?}

    @WrapOperation(
            //? if >=26 {
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;extractBar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/world/BossEvent;)V"
            )
            //?} else {
            /*method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;drawBar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/world/BossEvent;)V"
            )
            *///?}
    )
    private void vanillahud$bossHealth(BossHealthOverlay self, GuiGraphicsExtractor graphics, int x, int y, BossEvent event, Operation<Void> original) {
        if (Huds.INSTANCE.getBossBar().getRenderHealth()) {
            original.call(self, graphics, x, y, event);
        }
    }

    @WrapOperation(
            //? if >=26 {
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
            )
            //?} elif >=1.21.6 {
            /*method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
            )
            *///?} else {
            /*method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"
            )
            *///?}
    )
            //? if <1.21.6 {
    /*private int vanillahud$bossText(GuiGraphicsExtractor graphics, Font font, Component name, int x, int y, int color, Operation<Integer> original) {
        if (Huds.INSTANCE.getBossBar().getRenderText()) {
            return original.call(graphics, font, name, x, y, color);
        }
        return 0;
    }
    *///?} else {
    private void vanillahud$bossText(GuiGraphicsExtractor graphics, Font font, Component name, int x, int y, int color, Operation<Void> original) {
        if (Huds.INSTANCE.getBossBar().getRenderText()) {
            original.call(graphics, font, name, x, y, color);
        }
    }
    //?}

    @ModifyExpressionValue(
            //? if >= 26 {
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;isEmpty()Z"
            )
            //?} else {
            /*method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;isEmpty()Z"
            )
            *///?}
    )
    private boolean vanillahud$editing(boolean empty) {
        return !HudManager.INSTANCE.isEditing() && empty;
    }

    @ModifyExpressionValue(
            //? if >= 26 {
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;values()Ljava/util/Collection;"
            )
            //?} else {
            /*method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;values()Ljava/util/Collection;"
            )
            *///?}
    )
    private Collection<LerpingBossEvent> vanillahud$demo(Collection<LerpingBossEvent> original) {
        if (!HudManager.INSTANCE.isEditing() || !original.isEmpty()) {
            return original;
        }

        return DemoData.demoBossEvents();
    }
}
