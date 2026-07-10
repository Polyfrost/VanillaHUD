package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.vanillahud.hook.HeadHook;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.hud.TabListHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//? if >=26 {
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2fStack;
//?}

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
    @Final
    @Shadow
    private Minecraft minecraft;

    @ModifyReturnValue(method = "getPlayerInfos", at = @At("RETURN"))
    private List<PlayerInfo> vanillahud$selfAtTop(List<PlayerInfo> original) {
        if (HudManager.INSTANCE.isEditing()) return original;
        TabListHud hud = Huds.INSTANCE.getTabList();
        if (hud.getSelfAtTop() && this.minecraft.player != null) {
            UUID self = this.minecraft.player.getUUID();
            List<PlayerInfo> reordered = new ArrayList<>(original);
            for (int i = 0; i < reordered.size(); i++) {
                //? if >=1.21.9 {
                UUID id = reordered.get(i).getProfile().id();
                //?} else {
                /*UUID id = reordered.get(i).getProfile().getId();
                *///?}
                if (self.equals(id)) {
                    reordered.addFirst(reordered.remove(i));
                    break;
                }
            }
            return reordered;
        }
        return original;
    }

    @ModifyArg(
            method = "getPlayerInfos",
            at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;limit(J)Ljava/util/stream/Stream;")
    )
    private long vanillahud$playerLimit(long original) {
        return Huds.INSTANCE.getTabList().getPlayerLimit();
    }

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "render",
            *///?} else {
            method = "extractRenderState",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getBackgroundColor(I)I")
    )
    private int vanillahud$widgetColor(int original) {
        return Huds.INSTANCE.getTabList().getTabWidgetArgb();
    }

    //? if >=26 {
    @org.spongepowered.asm.mixin.injection.Redirect(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V")
    )
    private void vanillahud$textShadowComponent(GuiGraphicsExtractor graphics, Font font, Component str, int x, int y,
                                                int color) {
        graphics.text(font, str, x, y, color, Huds.INSTANCE.getTabList().getTextType() != 0);
    }

    @org.spongepowered.asm.mixin.injection.Redirect(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)V")
    )
    private void vanillahud$textShadowSequence(GuiGraphicsExtractor graphics, Font font, FormattedCharSequence str,
                                               int x, int y, int color) {
        graphics.text(font, str, x, y, color, Huds.INSTANCE.getTabList().getTextType() != 0);
    }
    //?}

    @ModifyVariable(
            //? if <26 {
            /*method = "render",
            *///?} else {
            method = "extractRenderState",
            //?}
            at = @At("STORE"), ordinal = 0
    )
    private boolean vanillahud$showHead(boolean original) {
        return original && Huds.INSTANCE.getTabList().getShowHead();
    }

    @Inject(
            //? if <26 {
            /*method = "renderPingIcon",
            *///?} else {
            method = "extractPingIcon",
            //?}
            at = @At("HEAD"), cancellable = true
    )
    private void vanillahud$ping(GuiGraphicsExtractor graphics, int slotWidth, int xo, int yo, PlayerInfo info,
                                 CallbackInfo ci) {
        TabListHud hud = Huds.INSTANCE.getTabList();
        if (!hud.getShowPing()) {
            ci.cancel();
            return;
        }
        //? if >=26 {
        if (hud.getNumberPing()) {
            int ping = info.getLatency();
            if (hud.getHideFalsePing() && (ping <= 1 || ping >= 999)) {
                ci.cancel();
                return;
            }
            int color = hud.pingColor(ping);
            String str = String.valueOf(ping);
            Component text = Component.literal(str);
            int w = this.minecraft.font.width(str);
            if (hud.getPingType() == 1) {
                graphics.text(this.minecraft.font, text, xo + slotWidth - w - 1, yo, color);
            } else {
                Matrix3x2fStack pose = graphics.pose();
                pose.pushMatrix();
                pose.scale(0.5f, 0.5f);
                graphics.text(this.minecraft.font, text, 2 * (xo + slotWidth) - w - 2, 2 * yo + 2, color);
                pose.popMatrix();
            }
            ci.cancel();
        }
        //?}
    }

    @WrapOperation(
            //? if <26 {
            /*method = "render",
            *///?} else {
            method = "extractRenderState",
            //?}
            at = @At(
                    value = "INVOKE",
                    //? if >= 26.1 {
                    target = "Lnet/minecraft/client/gui/components/PlayerFaceExtractor;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;IIIZZI)V"
                    //?} else if >= 1.21.11 {
                    /*target = "Lnet/minecraft/client/gui/components/PlayerFaceRenderer;draw(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;IIIZZI)V"
                    *///?} else if >= 1.21.4 {
                    /*target = "Lnet/minecraft/client/gui/components/PlayerFaceRenderer;draw(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;IIIZZI)V"
                    *///?} else {
                    /*target = "Lnet/minecraft/client/gui/components/PlayerFaceRenderer;draw(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;IIIZZ)V"
                    *///?}
            )
    )
    private void vanilla$betterHatLayer(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int size, boolean hat, boolean flip, /*? if >= 1.21.4 {*/ int color, /*?}*/ Operation<Void> original) {
        if (Huds.INSTANCE.getTabList().getBetterHatLayer()) {
            HeadHook.INSTANCE.vanillahud$draw(graphics, texture, x, y, size, /*? if >= 1.21.4 {*/ color /*?} else {*/ /*-1 *//*?}*/, hat, flip);
        } else {
            original.call(graphics, texture, x, y, size, hat, flip /*? if >= 1.21.4 {*/, color /*?}*/);
        }
    }
}
