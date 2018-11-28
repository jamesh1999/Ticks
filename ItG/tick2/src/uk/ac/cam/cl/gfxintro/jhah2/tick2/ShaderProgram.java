package uk.ac.cam.cl.gfxintro.jhah2.tick2;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;


/**
 * Combines vertex and fragment Shaders into a single program that can be used for rendering.
 */
public class ShaderProgram {
    Shader vertex_shader;
    Shader fragment_shader;
    String output_variable;
    private int program = 0;

    public ShaderProgram(Shader vertex_shader, Shader fragment_shader, String output_variable) {
        this.vertex_shader = vertex_shader;
        this.fragment_shader = fragment_shader;
        this.output_variable = output_variable;

        createProgram( output_variable );
    }

    public void createProgram( String output_variable )
    {
        if( program != 0 )
            glDeleteProgram( program );

        program = glCreateProgram();
        glAttachShader(program, vertex_shader.getHandle());
        glAttachShader(program, fragment_shader.getHandle());
        glBindFragDataLocation(program, 0, output_variable);
        glLinkProgram(program);
        glUseProgram(program);
    }

    public void reloadIfNeeded() {
        boolean vertex_shader_reloaded = vertex_shader.reloadIfNeeded();
        boolean fragment_shader_reloaded = fragment_shader.reloadIfNeeded();

        if( vertex_shader_reloaded || fragment_shader_reloaded )
            createProgram( output_variable );
    }

    public int getHandle() {
        return program;
    }
}
