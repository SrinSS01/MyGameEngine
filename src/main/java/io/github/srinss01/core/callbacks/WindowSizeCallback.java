package io.github.srinss01.core.callbacks;

import io.github.srinss01.core.WindowManager;

import static org.lwjgl.opengl.GL11.glViewport;

@SuppressWarnings("unused")
public class WindowSizeCallback {
    private final WindowManager windowManager;
    public WindowSizeCallback(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    public void callback(long window, int width, int height) {
        WindowManager.WindowSizePoseCache windowWindowSizePoseCache = windowManager.getWindowWindowSizePoseCache();
        windowWindowSizePoseCache.setWidth(width);
        windowWindowSizePoseCache.setHeight(height);
        windowManager.updateProjectionMatrix();
        glViewport(0, 0, width, height);
    }
}
