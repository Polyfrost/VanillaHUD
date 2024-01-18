package org.polyfrost.vanillahud.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameProfileHelper {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final List<String> devUUIDs = Arrays.asList(
            "0b4d470f-f2fb-4874-9334-1eaef8ba4804", // DeDiamondPro
            "c8bf4768-af44-48cb-a259-01e42fb7bc79", // ImToggle
            "0e3ee1e0-f4d2-4550-8fe9-4f7a0d2cd08a", // Mixces
            "0d68ec06-ec8f-4558-959f-7a6d7efd7fa5", // Redth
            "a5331404-0e77-440e-8bef-24c071dac1ae" // Wyvest

    );

    public static final List<NetworkPlayerInfo> devInfo = devUUIDs.stream()
            .map(UUID::fromString)
            .map(GameProfileHelper::getProfile)
            .map(NetworkPlayerInfo::new)
            .collect(Collectors.toList());

    public static GameProfile getProfile(UUID uuid) {
        return mc.getSessionService().fillProfileProperties(new GameProfile(uuid, null), true);
    }
}
