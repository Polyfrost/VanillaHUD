package org.polyfrost.vanillahud.mixin.editor;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.objectweb.asm.Opcodes;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.hud.TabListHud;
import org.polyfrost.vanillahud.util.TabListManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
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
    @Shadow
    private Component header;

    @Shadow
    private Component footer;

    @Final
    @Shadow
    private Minecraft minecraft;

    @ModifyReturnValue(method = "getPlayerInfos", at = @At("RETURN"))
    private List<PlayerInfo> vanillahud$demoPlayers(List<PlayerInfo> original) {
        TabListHud hud = Huds.INSTANCE.getTabList();
        if (HudManager.INSTANCE.isEditing()) {
            TabListManager.INSTANCE.ensureLoaded();
            return TabListManager.INSTANCE.getDevInfo();
        }
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

    @Redirect(
            //? if <26 {
            /*method = "render",
            *///?} else {
            method = "extractRenderState",
            //?}
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;header:Lnet/minecraft/network/chat/Component;", opcode = Opcodes.GETFIELD)
    )
    private Component vanillahud$header(PlayerTabOverlay self) {
        if (HudManager.INSTANCE.isEditing()) return Component.literal("Tab List");
        return Huds.INSTANCE.getTabList().getShowHeader() ? this.header : null;
    }

    @Redirect(
            //? if <26 {
            /*method = "render",
            *///?} else {
            method = "extractRenderState",
            //?}
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;footer:Lnet/minecraft/network/chat/Component;", opcode = Opcodes.GETFIELD)
    )
    private Component vanillahud$footer(PlayerTabOverlay self) {
        if (HudManager.INSTANCE.isEditing()) return Component.literal("VanillaHUD");
        return Huds.INSTANCE.getTabList().getShowFooter() ? this.footer : null;
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
    @ModifyExpressionValue(
            method = "extractRenderState",
            //~ if >=26.2 'Lnet/minecraft/network/Connection;isEncrypted()Z' -> 'Lnet/minecraft/client/multiplayer/ClientPacketListener;onlineMode()Z'
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;isEncrypted()Z")
    )
    private boolean vanillahud$showHead(boolean original) {
        return original && Huds.INSTANCE.getTabList().getShowHead();
    }

    @Redirect(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V")
    )
    private void vanillahud$textShadowComponent(GuiGraphicsExtractor graphics, Font font, Component str, int x, int y,
                                                int color) {
        graphics.text(font, str, x, y, color, Huds.INSTANCE.getTabList().getTextType() != 0);
    }

    @Redirect(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)V")
    )
    private void vanillahud$textShadowSequence(GuiGraphicsExtractor graphics, Font font, FormattedCharSequence str,
                                               int x, int y, int color) {
        graphics.text(font, str, x, y, color, Huds.INSTANCE.getTabList().getTextType() != 0);
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
}
