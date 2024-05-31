package codes.biscuit.skyblockaddons;

import codes.biscuit.skyblockaddons.config.ConfigValues;
import codes.biscuit.skyblockaddons.utils.Utils;

public class SkyblockAddons {
    public static SkyblockAddons getInstance() {
        return new SkyblockAddons();
    }

    public Utils getUtils() {
        return new Utils();
    }

    public ConfigValues getConfigValues() {
        return new ConfigValues();
    }
}
