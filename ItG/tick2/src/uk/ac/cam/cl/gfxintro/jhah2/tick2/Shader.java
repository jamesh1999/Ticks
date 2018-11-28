package uk.ac.cam.cl.gfxintro.jhah2.tick2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;

/**
 * Shader class loads and compiles shaders so they can be attached to a ShaderProgram.
 */
public class Shader {
    private int shaderID = 0;
    private int type;
    private String filename;
    private long shaderTimestamp = 0;

    public Shader(int type, String filename) {
        this.type = type;
        this.filename = filename;
        load( type, filename );
    }

    /**
     * Load shaderID from file and compile it
     * @param type GL_VERTEX_SHADER or GL_FRAGMENT_SHADER
     * @param filename name of the text file with the GLSL shaderID
     */
    public void load(int type, String filename) {

        // Read the shaderID source code from file
        String shaderSource = null;
        try {
            List<String> shaderSourceLines = Files.readAllLines(Paths.get(filename));
            shaderSource = String.join("\n", shaderSourceLines);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load shaderID: " + filename);
        }

        // Create and compile the shaderID
        shaderID = glCreateShader(type);
        glShaderSource(shaderID, shaderSource);
        glCompileShader(shaderID);

        // Check in case there was an error during compilation
        int status = glGetShaderi(shaderID, GL_COMPILE_STATUS );
        if (status == 0) {
            String error = glGetShaderInfoLog(shaderID);
            System.out.println(error);
            glDeleteShader(shaderID);
            throw new RuntimeException("shader compilation failed: consult the log above");
        }
    }

    /**
     * Reload shader if the file has been modified
     * @return true if the shader was reloaded
     */
    public boolean reloadIfNeeded() {
        long timestamp = getFragmentShaderTimestamp();
        if (shaderTimestamp != timestamp) {

            int old_shaderID = shaderID;

            try {
                load(type, filename);
            } catch (RuntimeException e) {
                // Recover (to the old shader) if the new shader cannot be compiled
                shaderID = old_shaderID;
                shaderTimestamp = timestamp;
                return false;
            }
            shaderTimestamp = timestamp;
            if( old_shaderID != 0 ) // Free existing shaderID
                glDeleteShader(old_shaderID);

            return true;
        }
        return false;
    }

    private long getFragmentShaderTimestamp() {
        try {
            return new File(filename).lastModified();
        } catch (Exception e) {
            return -1;
        }
    }

    public int getHandle() {
        return shaderID;
    }
}
