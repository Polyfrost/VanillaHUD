package org.polyfrost.vanillahud.mixin.editor;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.objectweb.asm.Opcodes;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.util.TabListManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
    @ModifyReturnValue(method = "getPlayerInfos", at = @At("RETURN"))
    private List<PlayerInfo> vanillahud$demoPlayers(List<PlayerInfo> original) {
        if (!HudManager.INSTANCE.isEditing()) return original;
        if (!original.isEmpty() && !Minecraft.getInstance().hasSingleplayerServer()) return original;
        TabListManager.INSTANCE.ensureLoaded();
        List<PlayerInfo> dev = TabListManager.INSTANCE.getDevInfo();
        int limit = Huds.INSTANCE.getTabList().getPlayerLimit();
        return dev.size() > limit ? dev.subList(0, limit) : dev;
    }

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "render",
            *///?} else {
            method = "extractRenderState",
            //?}
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;header:Lnet/minecraft/network/chat/Component;", opcode = Opcodes.GETFIELD)
    )
    private Component vanillahud$header(Component header) {
        if (!Huds.INSTANCE.getTabList().getShowHeader()) return null;
        if (header != null) return header;
        return HudManager.INSTANCE.isEditing() ? Component.literal("Tab List") : null;
    }

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "render",
            *///?} else {
            method = "extractRenderState",
            //?}
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;footer:Lnet/minecraft/network/chat/Component;", opcode = Opcodes.GETFIELD)
    )
    private Component vanillahud$footer(Component footer) {
        if (!Huds.INSTANCE.getTabList().getShowFooter()) return null;
        if (footer != null) return footer;
        return HudManager.INSTANCE.isEditing() ? Component.literal("VanillaHUD") : null;
    }
}
