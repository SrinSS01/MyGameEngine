package io.github.srinss01.core;

import io.github.srinss01.core.callbacks.WindowSizeCallback;
import io.github.srinss01.core.callbacks.Keyboard;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.slf4j.Logger;

import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class WindowManager {
    private long window;
    @Getter
    private final WindowSizePoseCache windowWindowSizePoseCache;
    private final String title;
    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 1000.f;

    @Getter
    private final Matrix4f projectionMatrix;

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(WindowManager.class);
    static {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
    }


    WindowManager(int width, int height, String title, boolean vSync) {
        projectionMatrix = new Matrix4f().perspective(FOV, (float) width / (float) height, Z_NEAR, Z_FAR);
        windowWindowSizePoseCache = WindowSizePoseCache.of(width, height);
        this.title = title;
        init();
        // glfw video mode
        var vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // null check
        if (vidMode == null) {
            throw new RuntimeException("Failed to get video mode");
        }
        glfwSetWindowPos(window, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2);
        glfwMakeContextCurrent(window);
        if (vSync) {
            glfwSwapInterval(1);
        }
        GL.createCapabilities();
        glViewport(0, 0, width, height);
        glEnable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
//        glEnable(GL_CULL_FACE);
        glEnable(GL_STENCIL_TEST);
//        glCullFace(GL_BACK);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        LOGGER.info("OpenGL version: {}", glGetString(GL_VERSION));
        LOGGER.info("OpenGL vendor: {}", glGetString(GL_VENDOR));
        LOGGER.info("OpenGL renderer: {}", glGetString(GL_RENDERER));
        glfwShowWindow(window);
    }

    void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        window = glfwCreateWindow(windowWindowSizePoseCache.getWidth(), windowWindowSizePoseCache.getHeight(), title, 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFWKeyCallback.create(Keyboard::callback).set(window);
        WindowSizeCallback windowSizeCallback = new WindowSizeCallback(this);
        GLFWWindowSizeCallback.create(windowSizeCallback::callback).set(window);
    }

    // toggle fullscreen
    void toggleFullscreen() {
        long monitor = glfwGetPrimaryMonitor();
        var vidMode = glfwGetVideoMode(monitor);
        if (vidMode == null) {
            throw new RuntimeException("Failed to get video mode");
        }
        if (glfwGetWindowMonitor(window) != 0) {
            // set window to windowed mode
            int oldWidth = windowWindowSizePoseCache.getOldWidth();
            int oldHeight = windowWindowSizePoseCache.getOldHeight();
            int oldX = windowWindowSizePoseCache.getOldX();
            int oldY = windowWindowSizePoseCache.getOldY();
            glfwSetWindowMonitor(window, 0, oldX, oldY, oldWidth, oldHeight, 0);
        } else {
            // save old window dimensions
            int[] width = new int[1];
            int[] height = new int[1];
            int[] x = new int[1];
            int[] y = new int[1];
            glfwGetWindowPos(window, x, y);
            glfwGetWindowSize(window, width, height);
            windowWindowSizePoseCache.setOldWidth(width[0]);
            windowWindowSizePoseCache.setOldHeight(height[0]);
            windowWindowSizePoseCache.setOldX(x[0]);
            windowWindowSizePoseCache.setOldY(y[0]);
            // set window to monitor
            glfwSetWindowMonitor(window, monitor, 0, 0, vidMode.width(), vidMode.height(), vidMode.refreshRate());
        }
    }

    // poll events
    void pollEvents() {
        glfwPollEvents();
    }

    // clear screen
    void clearScreen() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    // swap buffers
    void swapBuffers() {
        glfwSwapBuffers(window);
    }

    // close
    void close() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    // window should close
    boolean shouldClose() {
        return glfwWindowShouldClose(window);
    }

    // close window
    void closeWindow() {
        glfwSetWindowShouldClose(window, true);
    }

    public void updateProjectionMatrix() {
        float aspectRatio = (float) windowWindowSizePoseCache.getWidth() / windowWindowSizePoseCache.getHeight();
        projectionMatrix.identity().perspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
    }

    @Getter @Setter
    public static class WindowSizePoseCache {
        private int width;
        private int height;

        private int oldWidth;
        private int oldHeight;

        private int oldX;
        private int oldY;

        private WindowSizePoseCache(int width, int height) {
            this.width = width;
            this.height = height;
            this.oldHeight = height;
            this.oldWidth = width;
        }

        public static WindowSizePoseCache of(int width, int height) {
            return new WindowSizePoseCache(width, height);
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void setWidth(int width) {
            this.width = width;
        }
    }
}
