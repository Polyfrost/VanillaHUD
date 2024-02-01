package org.polyfrost.vanillahud.hooks;

import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.DummyAnimation;

public class TooltipHook {

    public static boolean isScrolling;

    public static int gui$scrollY;

    public static Animation gui$animationY = new DummyAnimation(0f);

    public static void resetScrolling() {
        gui$scrollY = 0;
        gui$animationY = new DummyAnimation(0f);
    }

}