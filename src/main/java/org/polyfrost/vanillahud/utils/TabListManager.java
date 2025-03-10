package org.polyfrost.vanillahud.utils;

import org.polyfrost.oneconfig.utils.v1.Multithreading;
import org.polyfrost.oneconfig.utils.v1.NetworkUtils;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TabListManager {
    private static final Gson gson = new GsonBuilder().create();
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final String devListURL = "https://raw.githubusercontent.com/Polyfrost/VanillaHUD/main/tablist_uuids.json";

    private static final List<String> fallbackDevUUIDs = Arrays.asList(
            "0b4d470f-f2fb-4874-9334-1eaef8ba4804", // DeDiamondPro
            "c8bf4768-af44-48cb-a259-01e42fb7bc79", // ImToggle
            "0e3ee1e0-f4d2-4550-8fe9-4f7a0d2cd08a", // Mixces
            "0d68ec06-ec8f-4558-959f-7a6d7efd7fa5", // Redth
            "a5331404-0e77-440e-8bef-24c071dac1ae" // Wyvest
    );

    public static List<NetworkPlayerInfo> devInfo = ImmutableList.of();

    public static void asyncFetchFallbackList() {
        Multithreading.submit(() -> devInfo = fallbackDevUUIDs.stream()
                .map(UUID::fromString)
                .map(TabListManager::getProfile)
                .map(NetworkPlayerInfo::new)
                .collect(Collectors.toList()));
    }
    public static GameProfile getProfile(UUID uuid) {
        return mc.getSessionService().fillProfileProperties(new GameProfile(uuid, null), true);
    }

    public static void asyncUpdateList() {
        Multithreading.submit(TabListManager::updateList);
    }
    private static void updateList() {
        try {
            String input = NetworkUtils.getString(devListURL);
            if (input == null) return;
            DevList list = gson.fromJson(input, DevList.class);
            devInfo = list.toNetworkInfoList();
        } catch (Exception exception) {
            new RuntimeException("Failed to load list", exception).printStackTrace();
        }
    }

    private static class DevList extends ArrayList<DevList.DevEntry> {
        private static class DevEntry {
            public String name;
            public String id;
            public UUID getUUID() {
                return UUID.fromString(id);
            }
        }

        public List<NetworkPlayerInfo> toNetworkInfoList() {
            return stream()
                .map(DevList.DevEntry::getUUID)
                .map(TabListManager::getProfile)
                .map(NetworkPlayerInfo::new)
                .collect(Collectors.toList());
        }
    }
}
