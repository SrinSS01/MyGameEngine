package io.github.srinss01.core;

import io.github.srinss01.core.callbacks.Keyboard;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F11;

public class GameEngine {
    private final WindowManager windowManager;
//    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GameEngine.class);
    public GameEngine() {
        windowManager = new WindowManager(800, 600, "My Game", true);
    }

    public void run() {
        while (!windowManager.shouldClose()) {
            windowManager.pollEvents();
            windowManager.clearScreen();
            windowManager.swapBuffers();
            if (Keyboard.isKeyPressed(GLFW_KEY_ESCAPE)) {
                windowManager.closeWindow();
            }
            if (Keyboard.isKeyPressed(GLFW_KEY_F11)) {
                windowManager.toggleFullscreen();
                Keyboard.setKeyPressed(GLFW_KEY_F11, false);
            }
        }
        windowManager.close();
    }
}
