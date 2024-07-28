package org.polyfrost.overflowanimations.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;

public class OldAnimationsSettings extends Config {

    public int tabMode = 1;

    @Exclude
    public static final OldAnimationsSettings INSTANCE = new OldAnimationsSettings();

    public OldAnimationsSettings() {
        super(new Mod("", ModType.PVP, ""), "");
    }
}