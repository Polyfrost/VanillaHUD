package org.polyfrost.vanillahud.hud;

import org.polyfrost.oneconfig.api.hud.v1.HudManager;

public final class HudInternals {
    private HudInternals() {
    }

    public static boolean systemReposition() {
        return HudManager.systemReposition;
    }

    public static void systemReposition(boolean value) {
        HudManager.systemReposition = value;
    }
}
