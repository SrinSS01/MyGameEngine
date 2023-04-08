package io.github.srinss01.core;

import io.github.srinss01.core.callbacks.Keyboard;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

public class GameEngine {
    private final WindowManager windowManager;

    //    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GameEngine.class);
    public GameEngine() {
        windowManager = new WindowManager(800, 600, "My Game", true);
    }

    public void run() {
        // vertices for a cube
        float[] vertices = {
                -0.5f, -0.5f, -0.5f,         0.0f, 0.0f,
                0.5f, -0.5f, -0.5f,         1.0f, 0.0f,
                0.5f,  0.5f, -0.5f,         1.0f, 1.0f,
                0.5f,  0.5f, -0.5f,         1.0f, 1.0f,
                -0.5f,  0.5f, -0.5f,         0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,         0.0f, 0.0f,

                -0.5f, -0.5f,  0.5f,         0.0f, 0.0f,
                0.5f, -0.5f,  0.5f,         1.0f, 0.0f,
                0.5f,  0.5f,  0.5f,         1.0f, 1.0f,
                0.5f,  0.5f,  0.5f,         1.0f, 1.0f,
                -0.5f,  0.5f,  0.5f,         0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f,         0.0f, 0.0f,

                -0.5f,  0.5f,  0.5f,        1.0f, 0.0f,
                -0.5f,  0.5f, -0.5f,        1.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,        0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,        0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f,        0.0f, 0.0f,
                -0.5f,  0.5f,  0.5f,        1.0f, 0.0f,

                0.5f,  0.5f,  0.5f,         1.0f, 0.0f,
                0.5f,  0.5f, -0.5f,         1.0f, 1.0f,
                0.5f, -0.5f, -0.5f,         0.0f, 1.0f,
                0.5f, -0.5f, -0.5f,         0.0f, 1.0f,
                0.5f, -0.5f,  0.5f,         0.0f, 0.0f,
                0.5f,  0.5f,  0.5f,         1.0f, 0.0f,

                -0.5f, -0.5f, -0.5f,         0.0f, 1.0f,
                0.5f, -0.5f, -0.5f,         1.0f, 1.0f,
                0.5f, -0.5f,  0.5f,         1.0f, 0.0f,
                0.5f, -0.5f,  0.5f,         1.0f, 0.0f,
                -0.5f, -0.5f,  0.5f,         0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f,         0.0f, 1.0f,

                -0.5f,  0.5f, -0.5f,         0.0f, 1.0f,
                0.5f,  0.5f, -0.5f,         1.0f, 1.0f,
                0.5f,  0.5f,  0.5f,         1.0f, 0.0f,
                0.5f,  0.5f,  0.5f,         1.0f, 0.0f,
                -0.5f,  0.5f,  0.5f,         0.0f, 0.0f,
                -0.5f,  0.5f, -0.5f,         0.0f, 1.0f
        };

        int vao = glGenVertexArrays();
        int vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        final Shader triangleShader = new Shader("triangle.vs", "triangle.fs");

        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        int width, height, nrChannels;
        try (
            MemoryStack stack = MemoryStack.stackPush();
            InputStream sample = GameEngine.class.getResourceAsStream("../resources/textures/sample.jpg")
        ) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            IntBuffer nrChannelsBuffer = stack.mallocInt(1);

            if (sample == null) {
                throw new RuntimeException("Failed to load image");
            }
            byte[] bytes = sample.readAllBytes();
            ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            stbi_set_flip_vertically_on_load(true);
            ByteBuffer data = stbi_load_from_memory(buffer, widthBuffer, heightBuffer, nrChannelsBuffer, 3);
            MemoryUtil.memFree(buffer);
            if (data == null) {
                throw new RuntimeException("Failed to load image: " + stbi_failure_reason());
            }
            width = widthBuffer.get();
            height = heightBuffer.get();
            nrChannels = nrChannelsBuffer.get();
            if (nrChannels == 3) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, data);
            } else if (nrChannels == 4) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
            } else {
                throw new RuntimeException("Image is not RGB or RGBA");
            }
            glGenerateMipmap(GL_TEXTURE_2D);
            stbi_image_free(data);
            glBindTexture(GL_TEXTURE_2D, 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Matrix4f projection = windowManager.getProjectionMatrix();
        Matrix4f model = new Matrix4f().identity();
        Matrix4f view = new Matrix4f().identity().translate(0f, 0f, -3f);
        while (!windowManager.shouldClose()) {
            if (Keyboard.isKeyPressed(GLFW_KEY_ESCAPE)) {
                windowManager.closeWindow();
            }
            if (Keyboard.isKeyPressed(GLFW_KEY_F11)) {
                windowManager.toggleFullscreen();
                Keyboard.setKeyPressed(GLFW_KEY_F11, false);
            }
            windowManager.pollEvents();
            windowManager.clearScreen();

            glBindVertexArray(vao);
            triangleShader.use();

            model.identity().rotate((float) glfwGetTime(), 1f, 1f, 0f);

            triangleShader.setUniformMatrix4f("uProjection", projection);
            triangleShader.setUniformMatrix4f("uModel", model);
            triangleShader.setUniformMatrix4f("uView", view);

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture);
            triangleShader.setUniform1i("uTexture", 0);

            glDrawArrays(GL_TRIANGLES, 0, 36);
            glBindVertexArray(0);
            triangleShader.unbind();

            windowManager.swapBuffers();
        }

        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        glDeleteTextures(texture);
        windowManager.close();
    }
}
