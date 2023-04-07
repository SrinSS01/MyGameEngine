package io.github.srinss01.core.callbacks;

import io.github.srinss01.core.WindowManager;

import static org.lwjgl.opengl.GL11.glViewport;

@SuppressWarnings("unused")
public class FramebufferSizeCallbackImpl {
    private final WindowManager windowManager;
    public FramebufferSizeCallbackImpl(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    public void callback(long window, int width, int height) {
        WindowManager.Dimensions windowDimensions = windowManager.getWindowDimensions();
        windowDimensions.setWidth(width);
        windowDimensions.setHeight(height);
        windowManager.updateProjectionMatrix();
        glViewport(0, 0, width, height);
    }
}
