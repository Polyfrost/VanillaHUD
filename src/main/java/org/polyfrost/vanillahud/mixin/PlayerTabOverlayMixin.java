package org.polyfrost.vanillahud.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.UUID;

// FIXME: Null Pointer & It should fetch developers from tablist_uuids.json
@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyReturnValue(method = "getPlayerInfos", at = @At("RETURN"))
    private List<PlayerInfo> vanillahud$demoPlayers(List<PlayerInfo> original) {
        if (original.isEmpty() || HudManager.INSTANCE.isEditing()) {

            return List.of(
                    new PlayerInfo(getProfile(UUID.fromString("0b4d470f-f2fb-4874-9334-1eaef8ba4804")), false),
                    new PlayerInfo(getProfile(UUID.fromString("c8bf4768-af44-48cb-a259-01e42fb7bc79")), false),
                    new PlayerInfo(getProfile(UUID.fromString("0e3ee1e0-f4d2-4550-8fe9-4f7a0d2cd08a")), false),
                    new PlayerInfo(getProfile(UUID.fromString("0d68ec06-ec8f-4558-959f-7a6d7efd7fa5")), false),
                    new PlayerInfo(getProfile(UUID.fromString("a5331404-0e77-440e-8bef-24c071dac1ae")), false)
            );
        }
        return original;
    }

    @Unique
    public GameProfile getProfile(UUID uuid) {
        return minecraft.services().sessionService().fetchProfile(uuid, true).profile();
    }
}
