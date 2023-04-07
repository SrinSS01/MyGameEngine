package io.github.srinss01.core;

import io.github.srinss01.core.callbacks.WindowSizeCallback;
import io.github.srinss01.core.callbacks.Keyboard;
import lombok.Getter;
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
    private final Dimensions windowDimensions;
    private final String title;
    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;

    @Getter
    private final Matrix4f projectionMatrix = new Matrix4f();

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(WindowManager.class);
    static {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
    }


    WindowManager(int width, int height, String title, boolean vSync) {
        windowDimensions = Dimensions.of(width, height);
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
        glEnable(GL_CULL_FACE);
        glEnable(GL_STENCIL_TEST);
        glCullFace(GL_BACK);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        LOGGER.info("OpenGL version: {}", glGetString(GL_VERSION));
        glfwShowWindow(window);
    }

    void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        window = glfwCreateWindow(windowDimensions.getWidth(), windowDimensions.getHeight(), title, 0, 0);
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
            int oldWidth = windowDimensions.getOldWidth();
            int oldHeight = windowDimensions.getOldHeight();
            glfwSetWindowMonitor(window, 0, (vidMode.width() - oldWidth) / 2, (vidMode.height() - oldHeight) / 2, oldWidth, oldHeight, 0);
        } else {
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

    public void draw() {
        //TODO: draw something
    }

    public void updateProjectionMatrix() {
        float aspectRatio = (float) windowDimensions.getWidth() / windowDimensions.getHeight();
        projectionMatrix.setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
    }

    @Getter
    public static class Dimensions {
        private int width;
        private int height;

        private int oldWidth;
        private int oldHeight;

        private Dimensions(int width, int height) {
            this.width = width;
            this.height = height;
            this.oldHeight = height;
            this.oldWidth = width;
        }

        public static Dimensions of(int width, int height) {
            return new Dimensions(width, height);
        }

        public void setHeight(int height) {
            oldHeight = this.height;
            this.height = height;
        }

        public void setWidth(int width) {
            oldWidth = this.width;
            this.width = width;
        }
    }
}
