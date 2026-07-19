package org.polyfrost.vanillahud.mixin.elements;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.polyfrost.vanillahud.hook.HeadHook;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.hud.TabListHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//? if >=26 {
import net.minecraft.util.FormattedCharSequence;
//?}
//? if >=1.21.6 {
import org.joml.Matrix3x2fStack;
//?}
//? if <1.21.6 {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}

@Mixin(value = PlayerTabOverlay.class, priority = 1100)
public abstract class PlayerTabOverlayMixin {
    @Final
    @Shadow
    private Minecraft minecraft;

    @WrapOperation(
            method = "getPlayerInfos",
            at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;limit(J)Ljava/util/stream/Stream;")
    )
    private Stream<PlayerInfo> vanillahud$selfAtTopAndLimit(Stream<PlayerInfo> stream, long original,
                                                           Operation<Stream<PlayerInfo>> op) {
        TabListHud hud = Huds.INSTANCE.getTabList();
        long limit = hud.getPlayerLimit();
        if (hud.getSelfAtTop() && this.minecraft.player != null) {
            UUID self = this.minecraft.player.getUUID();
            List<PlayerInfo> list = stream.collect(Collectors.toList());
            for (int i = 0; i < list.size(); i++) {
                //? if >=1.21.9 {
                UUID id = list.get(i).getProfile().id();
                //?} else {
                /*UUID id = list.get(i).getProfile().getId();
                *///?}
                if (self.equals(id)) {
                    list.addFirst(list.remove(i));
                    break;
                }
            }
            return list.stream().limit(limit);
        }
        return op.call(stream, limit);
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

    @ModifyArg(
            //? if <26 {
            /*method = "render",
            *///?} else {
            method = "extractRenderState",
            //?}
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V",
                    ordinal = 0),
            index = 4
    )
    private int vanillahud$headerBg(int color) {
        return Huds.INSTANCE.getTabList().getHeaderBgArgb();
    }

    @ModifyArg(
            //? if <26 {
            /*method = "render",
            *///?} else {
            method = "extractRenderState",
            //?}
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V",
                    ordinal = 1),
            index = 4
    )
    private int vanillahud$bodyBg(int color) {
        return Huds.INSTANCE.getTabList().getBodyBgArgb();
    }

    @ModifyArg(
            //? if <26 {
            /*method = "render",
            *///?} else {
            method = "extractRenderState",
            //?}
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V",
                    ordinal = 3),
            index = 4
    )
    private int vanillahud$footerBg(int color) {
        return Huds.INSTANCE.getTabList().getFooterBgArgb();
    }

    //? if >=26 {
    @WrapOperation(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V")
    )
    private void vanillahud$textShadowComponent(GuiGraphicsExtractor graphics, Font font, Component str, int x, int y,
                                                int color, Operation<Void> original) {
        graphics.text(font, str, x, y, color, Huds.INSTANCE.getTabList().getTextType() != 0);
    }

    @WrapOperation(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)V")
    )
    private void vanillahud$textShadowSequence(GuiGraphicsExtractor graphics, Font font, FormattedCharSequence str,
                                               int x, int y, int color, Operation<Void> original) {
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

    @Unique
    private int vanillahud$pingReserve() {
        TabListHud hud = Huds.INSTANCE.getTabList();
        if (hud.getShowPing() && hud.getNumberPing() && hud.getPingType() == 1) {
            return this.minecraft.font.width("999") + 3;
        }
        return 0;
    }

    @ModifyConstant(
            //? if <26 {
            /*method = "render",
            *///?} else {
            method = "extractRenderState",
            //?}
            constant = @Constant(intValue = 13))
    private int vanillahud$pingReserveWidth(int original) {
        int reserve = vanillahud$pingReserve();
        return reserve > 0 ? Math.max(original, reserve) : original;
    }

    // Capture each slot's right edge so the name clip below knows where the ping sits.
    @WrapOperation(
            //? if <26 {
            /*method = "render",
            *///?} else {
            method = "extractRenderState",
            //?}
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V",
                    ordinal = 2))
    private void vanillahud$captureSlotRight(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color,
                                             Operation<Void> original,
                                             @Share("vhTabSlotRight") LocalIntRef slotRight) {
        slotRight.set(x2);
        original.call(graphics, x1, y1, x2, y2, color);
    }

    // At clamped GUI scales names overflow their column into the numeric ping. Clip them.
    @WrapOperation(
            //? if <1.21.6 {
            /*method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I", ordinal = 0)
            *///?} else if <26 {
            /*method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V", ordinal = 0)
            *///?} else {
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V", ordinal = 0)
            //?}
    )
    //? if <1.21.6 {
    /*private int vanillahud$clipName(GuiGraphicsExtractor graphics, Font font, Component name, int x, int y, int color,
                                   Operation<Integer> original, @Share("vhTabSlotRight") LocalIntRef slotRight) {
        int clipRight = slotRight.get() - vanillahud$pingReserve();
        boolean clip = vanillahud$pingReserve() > 0 && clipRight > x && x + this.minecraft.font.width(name) > clipRight;
        if (clip) graphics.enableScissor(x, y - 1, clipRight, y + 9);
        int r = original.call(graphics, font, name, x, y, color);
        if (clip) graphics.disableScissor();
        return r;
    }
    *///?} else {
    private void vanillahud$clipName(GuiGraphicsExtractor graphics, Font font, Component name, int x, int y, int color,
                                     Operation<Void> original, @Share("vhTabSlotRight") LocalIntRef slotRight) {
        int clipRight = slotRight.get() - vanillahud$pingReserve();
        boolean clip = vanillahud$pingReserve() > 0 && clipRight > x && x + this.minecraft.font.width(name) > clipRight;
        if (clip) graphics.enableScissor(x, y - 1, clipRight, y + 9);
        original.call(graphics, font, name, x, y, color);
        if (clip) graphics.disableScissor();
    }
    //?}

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
        if (hud.getNumberPing()) {
            int ping = info.getLatency();
            if (hud.getHideFalsePing() && (ping <= 1 || ping >= 999)) {
                ci.cancel();
                return;
            }
            int color = hud.pingColor(ping);
            String str = String.valueOf(ping);
            int w = this.minecraft.font.width(str);
            boolean full = hud.getPingType() == 1;
            int fullX = xo + slotWidth - w - 1;
            int scaledX = 2 * (xo + slotWidth) - w - 2;
            int scaledY = 2 * yo + 2;
            //? if >=26 {
            Component text = Component.literal(str);
            if (full) {
                graphics.text(this.minecraft.font, text, fullX, yo, color);
            } else {
                Matrix3x2fStack pose = graphics.pose();
                pose.pushMatrix();
                pose.scale(0.5f, 0.5f);
                graphics.text(this.minecraft.font, text, scaledX, scaledY, color);
                pose.popMatrix();
            }
            //?} else if >=1.21.6 {
            /*if (full) {
                graphics.drawString(this.minecraft.font, str, fullX, yo, color);
            } else {
                Matrix3x2fStack pose = graphics.pose();
                pose.pushMatrix();
                pose.scale(0.5f, 0.5f);
                graphics.drawString(this.minecraft.font, str, scaledX, scaledY, color);
                pose.popMatrix();
            }
            *///?} else {
            /*if (full) {
                graphics.drawString(this.minecraft.font, str, fullX, yo, color);
            } else {
                PoseStack pose = graphics.pose();
                pose.pushPose();
                pose.scale(0.5f, 0.5f, 1.0f);
                graphics.drawString(this.minecraft.font, str, scaledX, scaledY, color);
                pose.popPose();
            }
            *///?}
            ci.cancel();
        }
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
