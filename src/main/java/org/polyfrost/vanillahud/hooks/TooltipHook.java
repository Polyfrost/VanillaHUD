package org.polyfrost.vanillahud.hooks;

import org.polyfrost.oneconfig.gui.animations.DummyAnimation;
import org.polyfrost.polyui.animate.Animation;

public class TooltipHook {

    public static boolean isScrolling;

    public static int gui$scrollY;

    public static Animation gui$animationY;

    public static void resetScrolling() {
        gui$scrollY = 0;
    }

}