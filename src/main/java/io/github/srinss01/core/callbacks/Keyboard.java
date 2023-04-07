package io.github.srinss01.core.callbacks;

import static org.lwjgl.glfw.GLFW.*;

public class Keyboard {
    static final boolean[] keys = new boolean[GLFW_KEY_LAST];

    public static boolean isKeyPressed(int keyCode) {
        return keys[keyCode];
    }

    public static void setKeyPressed(int keyCode, boolean isPressed) {
        keys[keyCode] = isPressed;
    }

    public static void callback(long window, int key, int scancode, int action, int mods) {
        if (key == -1) {
            return;
        }
        setKeyPressed(key, action == GLFW_PRESS);
    }
}
