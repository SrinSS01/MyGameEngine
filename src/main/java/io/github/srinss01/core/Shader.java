package io.github.srinss01.core;

import org.joml.Matrix4f;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final int program;

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Shader.class);
    public Shader(String vsPath, String fsPath) {
        try {
            program = glCreateProgram();
            String vertexShaderSource = parseShader(vsPath);
            String fragmentShaderSource = parseShader(fsPath);
            int vertex = glCreateShader(GL_VERTEX_SHADER);
            int fragment = glCreateShader(GL_FRAGMENT_SHADER);
            glShaderSource(vertex, vertexShaderSource);
            glShaderSource(fragment, fragmentShaderSource);
            glCompileShader(vertex);
            checkCompileError(vertex);
            glCompileShader(fragment);
            checkCompileError(fragment);
            glAttachShader(program, vertex);
            glAttachShader(program, fragment);
            glLinkProgram(program);
            checkProgramError(GL_LINK_STATUS);
            glValidateProgram(program);
            checkProgramError(GL_VALIDATE_STATUS);
            glDeleteShader(vertex);
            glDeleteShader(fragment);
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Error loading shader: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // parse the shader files and return the string
    private String parseShader(String path) throws IOException, URISyntaxException {
        URL resource = Shader.class.getResource("../resources/shaders/" + path);
        if (resource == null) {
            throw new IOException("Could not find shader file: " + path);
        }
        return Files.readString(Path.of(resource.toURI()));
    }

    public void use() {
        glUseProgram(program);
    }

    public void unbind() {
        glUseProgram(0);
    }

    // compile error checking
    public void checkCompileError(int shader) {
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            LOGGER.error("Error compiling {} shader: {}", shader == GL_VERTEX_SHADER? "Vertex": "Fragment", glGetShaderInfoLog(shader));
        }
    }

    // link error checking
    public void checkProgramError(int status) {
        if (glGetProgrami(program, status) == 0) {
            LOGGER.error("Error linking shader program: {}", glGetProgramInfoLog(program));
        }
    }

    public void setUniform1i(String name, int i) {
        glUniform1i(glGetUniformLocation(program, name), i);
    }

    public void setUniformMatrix4f(String name, Matrix4f mat) {
        glUniformMatrix4fv(glGetUniformLocation(program, name), false, mat.get(new float[16]));
    }
}
