package org.polyfrost.vanillahud.hook;

public final class TextShadowHook {
    private static int depth;
    private static boolean dropShadow;

    private TextShadowHook() {
    }

    public static void push(boolean shadow) {
        depth++;
        dropShadow = shadow;
    }

    public static void pop() {
        if (depth > 0) depth--;
    }

    public static boolean apply(boolean original) {
        return depth > 0 ? dropShadow : original;
    }
}
