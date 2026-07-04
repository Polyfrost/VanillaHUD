package org.polyfrost.vanillahud.render;

import org.polyfrost.vanillahud.config.HudElement;

import java.lang.reflect.Method;

public final class HudTransforms {
    private static final float HIDDEN_OFFSET = -100000.0F;

    private HudTransforms() {
    }

    public static void push(Object graphics, HudElement element, float defaultX, float defaultY) {
        int width = invokeInt(graphics, "guiWidth");
        int height = invokeInt(graphics, "guiHeight");
        float targetX = element.enabled ? element.scaledX(width) : HIDDEN_OFFSET;
        float targetY = element.enabled ? element.scaledY(height) : HIDDEN_OFFSET;
        float scale = element.enabled ? element.scale : 0.01F;
        pushRaw(graphics, targetX - defaultX, targetY - defaultY, scale);
    }

    public static void pushOffset(Object graphics, float xOffset, float yOffset, float scale) {
        pushRaw(graphics, xOffset, yOffset, scale);
    }

    public static void pop(Object graphics) {
        Object pose = invoke(graphics, "pose");
        if (!tryInvoke(pose, "popMatrix")) {
            invoke(pose, "popPose");
        }
    }

    private static void pushRaw(Object graphics, float xOffset, float yOffset, float scale) {
        Object pose = invoke(graphics, "pose");
        if (!tryInvoke(pose, "pushMatrix")) {
            invoke(pose, "pushPose");
        }
        if (!tryInvoke(pose, "translate", new Class<?>[]{float.class, float.class}, xOffset, yOffset)) {
            invoke(pose, "translate", new Class<?>[]{float.class, float.class, float.class}, xOffset, yOffset, 0.0F);
        }
        if (scale != 1.0F) {
            if (!tryInvoke(pose, "scale", new Class<?>[]{float.class, float.class}, scale, scale)) {
                invoke(pose, "scale", new Class<?>[]{float.class, float.class, float.class}, scale, scale, 1.0F);
            }
        }
    }

    private static int invokeInt(Object target, String name) {
        return ((Number) invoke(target, name)).intValue();
    }

    private static Object invoke(Object target, String name) {
        return invoke(target, name, new Class<?>[0]);
    }

    private static Object invoke(Object target, String name, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = target.getClass().getMethod(name, parameterTypes);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke " + name + " on " + target.getClass().getName(), exception);
        }
    }

    private static boolean tryInvoke(Object target, String name, Object... args) {
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        return tryInvoke(target, name, parameterTypes, args);
    }

    private static boolean tryInvoke(Object target, String name, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = target.getClass().getMethod(name, parameterTypes);
            method.invoke(target, args);
            return true;
        } catch (NoSuchMethodException exception) {
            return false;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke " + name + " on " + target.getClass().getName(), exception);
        }
    }
}
