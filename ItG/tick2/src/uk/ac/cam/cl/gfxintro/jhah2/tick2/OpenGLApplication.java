package uk.ac.cam.cl.gfxintro.jhah2.tick2;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class OpenGLApplication {

    // Vertical field of view
    private static final float FOV_Y = (float) Math.toRadians(50);
    private static final float HEIGHTMAP_SCALE = 3.0f;

    // Width and height of renderer in pixels
    protected static int WIDTH = 800, HEIGHT = 600;

    // Size of height map in world units
    private static float MAP_SIZE = 10;
    private Camera camera;
    private Texture terrainTexture;
    private long window;

    private ShaderProgram shaders;
    private float[][] heightmap;
    private int no_of_triangles;
    private int vertexArrayObj;

    // Callbacks for input handling
    private GLFWCursorPosCallback cursor_cb;
    private GLFWScrollCallback scroll_cb;
    private GLFWKeyCallback key_cb;

    // Filenames for vertex and fragment shader source code
    private final String VSHADER_FN = "resources/vertex_shader.glsl";
    private final String FSHADER_FN = "resources/fragment_shader.glsl";

    public OpenGLApplication(String heightmapFilename) {

        // Load heightmap data from file into CPU memory
        initializeHeightmap(heightmapFilename);
    }

    // OpenGL setup - do not touch this method!
    public void initializeOpenGL() {

        if (glfwInit() != true)
            throw new RuntimeException("Unable to initialize the graphics runtime.");

        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Ensure that the right version of OpenGL is used (at least 3.2)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // Use CORE OpenGL profile without depreciated functions
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE); // Make it forward compatible

        window = glfwCreateWindow(WIDTH, HEIGHT, "Tick 3", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the application window.");

        GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (mode.width() - WIDTH) / 2, (mode.height() - HEIGHT) / 2);
        glfwMakeContextCurrent(window);
        createCapabilities();

        // Enable v-sync
        glfwSwapInterval(1);

        // Cull back-faces of polygons
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        // Do depth comparisons when rendering
        glEnable(GL_DEPTH_TEST);

        // Create camera, and setup input handlers
        camera = new Camera((double) WIDTH / HEIGHT, FOV_Y);
        initializeInputs();

        // Create shaders and attach to a ShaderProgram
        Shader vertShader = new Shader(GL_VERTEX_SHADER, VSHADER_FN);
        Shader fragShader = new Shader(GL_FRAGMENT_SHADER, FSHADER_FN);
        shaders = new ShaderProgram(vertShader, fragShader, "colour");

        // Initialize mesh data in CPU memory
        float vertPositions[] = initializeVertexPositions( heightmap );
        int indices[] = initializeVertexIndices( heightmap );
        float vertNormals[] = initializeVertexNormals( heightmap );
        no_of_triangles = indices.length;

        // Load mesh data onto GPU memory
        loadDataOntoGPU( vertPositions, indices, vertNormals );

        // Load texture image and create OpenGL texture object
        terrainTexture = new Texture();
        terrainTexture.load("resources/texture.png");
    }

    private void initializeInputs() {

        // Callback for: when dragging the mouse, rotate the camera
        cursor_cb = new GLFWCursorPosCallback() {
            private double prevMouseX, prevMouseY;

            public void invoke(long window, double mouseX, double mouseY) {
                boolean dragging = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;
                if (dragging) {
                    camera.rotate(mouseX - prevMouseX, mouseY - prevMouseY);
                }
                prevMouseX = mouseX;
                prevMouseY = mouseY;
            }
        };

        // Callback for: when scrolling, zoom the camera
        scroll_cb = new GLFWScrollCallback() {
            public void invoke(long window, double dx, double dy) {
                camera.zoom(dy > 0);
            }
        };

        // Callback for keyboard controls: "W" - wireframe, "P" - points, "S" - take screenshot
        key_cb = new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_W && action == GLFW_PRESS) {
                    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                    glDisable(GL_CULL_FACE);
                } else if (key == GLFW_KEY_P && action == GLFW_PRESS) {
                    glPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
                } else if (key == GLFW_KEY_S && action == GLFW_RELEASE) {
                    takeScreenshot("screenshot.png");
                } else if (action == GLFW_RELEASE) {
                    glEnable(GL_CULL_FACE);
                    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                }
            }
        };

        GLFWFramebufferSizeCallback fbs_cb = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                glViewport( 0, 0, width, height );
                camera.setAspectRatio( width/height );
            }
        };

        // Set callbacks on the window
        glfwSetCursorPosCallback(window, cursor_cb);
        glfwSetScrollCallback(window, scroll_cb);
        glfwSetKeyCallback(window, key_cb);
        glfwSetFramebufferSizeCallback(window, fbs_cb);
    }

    /**
     * Create an array of vertex psoutions.
     *
     * @param heightmap 2D array with the heightmap
     * @return Vertex positions in the format { x0, y0, z0, x1, y1, z1, ... }
     */
    public float[] initializeVertexPositions( float[][] heightmap ) {
      //generate and upload vertex data

        int heightmap_width_px = heightmap[0].length;
        int heightmap_height_px = heightmap.length;

        float start_x = -MAP_SIZE / 2;
        float start_z = -MAP_SIZE / 2;
        float delta_x = MAP_SIZE / (heightmap_width_px-1);
        float delta_z = MAP_SIZE / (heightmap_height_px-1);

        // TODO: create float array for vertPositions of the right size

        float[] vertices = new float[3 * heightmap_height_px * heightmap_width_px];
        for (int row = 0; row < heightmap_height_px; row++) {
            for (int col = 0; col < heightmap_width_px; col++) {
                vertices[3 * (heightmap_width_px * row + col)] = start_x + delta_x * col;
                vertices[3 * (heightmap_width_px * row + col) + 1] = heightmap[row][col];
                vertices[3 * (heightmap_width_px * row + col) + 2] = start_z + delta_z * row;
            }
        }
        return vertices;
    }

    public int[] initializeVertexIndices( float[][] heightmap ) {

        int heightmap_width_px = heightmap[0].length;
        int heightmap_height_px = heightmap.length;


        int[] indices = new int[6 * (heightmap_width_px-1) * (heightmap_height_px-1)];
        for (int row = 0; row < heightmap_height_px - 1; row++) {
            for (int col = 0; col < heightmap_width_px - 1; col++) {
                indices[6 * ((heightmap_width_px-1) * row + col)  ] = row     * heightmap_width_px +     col;
                indices[6 * ((heightmap_width_px-1) * row + col)+1] = (row+1) * heightmap_width_px +     col;
                indices[6 * ((heightmap_width_px-1) * row + col)+2] = (row+1) * heightmap_width_px + (col+1);
                indices[6 * ((heightmap_width_px-1) * row + col)+3] = row     * heightmap_width_px +     col;
                indices[6 * ((heightmap_width_px-1) * row + col)+4] = (row+1) * heightmap_width_px + (col+1);
                indices[6 * ((heightmap_width_px-1) * row + col)+5] = row     * heightmap_width_px + (col+1);
            }
        }
        return indices;
    }

    public float[] initializeVertexNormals( float[][] heightmap ) {

        int heightmap_width_px = heightmap[0].length;
        int heightmap_height_px = heightmap.length;

        int num_verts = heightmap_width_px * heightmap_height_px;
        float[] normals = new float[3 * num_verts];
        // TODO: Initialize the array of normal vectors with the values (0, 1, 0)
        for (int i = 0; i < heightmap_width_px; ++i)
        {
            normals[3 * i] = 0.0f;
            normals[3 * i + 1] = 1.0f;
            normals[3 * i + 2] = 0.0f;

            normals[3 * (heightmap_width_px * (heightmap_height_px-1) + i)] = 0.0f;
            normals[3 * (heightmap_width_px * (heightmap_height_px-1) + i) + 1] = 1.0f;
            normals[3 * (heightmap_width_px * (heightmap_height_px-1) + i) + 2] = 0.0f;
        }
        for (int i = 0; i < heightmap_height_px; ++i)
        {
            normals[3 * i * heightmap_width_px] = 0.0f;
            normals[3 * i * heightmap_width_px + 1] = 1.0f;
            normals[3 * i * heightmap_width_px + 2] = 0.0f;

            normals[3 * (heightmap_width_px * i + heightmap_width_px-1)] = 0.0f;
            normals[3 * (heightmap_width_px * i + heightmap_width_px-1) + 1] = 1.0f;
            normals[3 * (heightmap_width_px * i + heightmap_width_px-1) + 2] = 0.0f;
        }



        float delta_x = MAP_SIZE / (heightmap_width_px-1);
        float delta_z = MAP_SIZE / (heightmap_height_px-1);

        for (int row = 1; row < heightmap_height_px - 1; row++) {
            for (int col = 1; col < heightmap_width_px - 1; col++) {

                Vector3f tx = new Vector3f(2 * delta_x, heightmap[row][col+1] - heightmap[row][col-1], 0);
                Vector3f tz = new Vector3f(0, heightmap[row+1][col] - heightmap[row-1][col], 2 * delta_z);
                Vector3f d = tz.cross(tx);
                Vector3f n = d.normalize();

                normals[3 * (heightmap_width_px * row + col)] = n.x;
                normals[3 * (heightmap_width_px * row + col) + 1] = n.y;
                normals[3 * (heightmap_width_px * row + col) + 2] = n.z;
            }
        }

        return normals;
    }

    public float[][] getHeightmap() {
        return heightmap;
    }

    public void initializeHeightmap(String heightmapFilename) {

        try {
            BufferedImage heightmapImg = ImageIO.read(new File(heightmapFilename));
            int heightmap_width_px = heightmapImg.getWidth();
            int heightmap_height_px = heightmapImg.getHeight();

            heightmap = new float[heightmap_height_px][heightmap_width_px];

            for (int row = 0; row < heightmap_height_px; row++) {
                for (int col = 0; col < heightmap_width_px; col++) {
                    float height = (float) (heightmapImg.getRGB(col, row) & 0xFF) / 0xFF;
                    heightmap[row][col] = HEIGHTMAP_SCALE * (float) Math.pow(height, 2.2);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading heightmap");
        }
    }


    public void loadDataOntoGPU( float[] vertPositions, int[] indices, float[] vertNormals ) {

        int shaders_handle = shaders.getHandle();

        vertexArrayObj = glGenVertexArrays(); // Get a OGL "name" for a vertex-array object
        glBindVertexArray(vertexArrayObj); // Create a new vertex-array object with that name

        // ---------------------------------------------------------------
        // LOAD VERTEX POSITIONS
        // ---------------------------------------------------------------

        // Construct the vertex buffer in CPU memory
        FloatBuffer vertex_buffer = BufferUtils.createFloatBuffer(vertPositions.length);
        vertex_buffer.put(vertPositions); // Put the vertex array into the CPU buffer
        vertex_buffer.flip(); // "flip" is used to change the buffer from write to read mode

        int vertex_handle = glGenBuffers(); // Get an OGL name for a buffer object
        glBindBuffer(GL_ARRAY_BUFFER, vertex_handle); // Bring that buffer object into existence on GPU
        glBufferData(GL_ARRAY_BUFFER, vertex_buffer, GL_STATIC_DRAW); // Load the GPU buffer object with data

        // Get the locations of the "position" vertex attribute variable in our ShaderProgram
        int position_loc = glGetAttribLocation(shaders_handle, "position");

        // If the vertex attribute does not exist, position_loc will be -1
        if (position_loc == -1)
            throw new RuntimeException( "'position' variable not found in the shader file");

        // Specifies where the data for "position" variable can be accessed
        glVertexAttribPointer(position_loc, 3, GL_FLOAT, false, 0, 0);

        // Enable that vertex attribute variable
        glEnableVertexAttribArray(position_loc);

        // ---------------------------------------------------------------
        // LOAD VERTEX NORMALS
        // ---------------------------------------------------------------

        // Construct the normal buffer on CPU
        FloatBuffer normal_buffer = BufferUtils.createFloatBuffer(vertNormals.length);
        normal_buffer.put(vertNormals);
        normal_buffer.flip();

        int normal_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, normal_handle);
        glBufferData(GL_ARRAY_BUFFER, normal_buffer, GL_STATIC_DRAW);

        // Get the locations of the "normal" vertex attribute variable
        position_loc = glGetAttribLocation(shaders_handle, "normal");
        if (position_loc == -1)
            throw new RuntimeException( "'normal' variable not found in the shader file");

        // Specifies where the data for "normal" variable can be accessed
        glVertexAttribPointer(position_loc, 3, GL_FLOAT, false, 0, 0);
        // Enable that vertex attribute variable
        glEnableVertexAttribArray(position_loc);

        // ---------------------------------------------------------------
        // LOAD VERTEX INDICES
        // ---------------------------------------------------------------

        IntBuffer index_buffer = BufferUtils.createIntBuffer(indices.length);
        index_buffer.put(indices).flip();
        int index_handle = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, index_handle);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, index_buffer, GL_STATIC_DRAW);

        glBindVertexArray(0); // Unbind the current vertex array

        // Finally, check for OpenGL errors
        checkError();
    }


    public void run() {

        while (glfwWindowShouldClose(window) != true) {
            render();
        }
    }

    public void render() {

        shaders.reloadIfNeeded(); // If shaders modified on disk, reload them

        // Step 1: Clear the buffer

        glClearColor(1.0f, 1.0f, 1.0f, 1.0f); // Set the background colour to white
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Step 2: Pass a new model-view-projection matrix to the vertex shader

        Matrix4f mvp_matrix; // Model-view-projection matrix
        mvp_matrix = new Matrix4f(camera.getProjectionMatrix()).mul(camera.getViewMatrix());

        int mvp_location = glGetUniformLocation(shaders.getHandle(), "mvp_matrix");
        FloatBuffer mvp_buffer = BufferUtils.createFloatBuffer(16);
        mvp_matrix.get(mvp_buffer); // Put 16 floating point numbers with the matrix to the FloatBuffer
        glUniformMatrix4fv(mvp_location, false, mvp_buffer);

        // Step 3: Draw our VertexArray as triangles

        // TODO: bind texture
        glBindTexture(GL_TEXTURE_2D, terrainTexture.getTexId());

        glBindVertexArray(vertexArrayObj); // Bind the existing VertexArray object
        glDrawElements(GL_TRIANGLES, no_of_triangles, GL_UNSIGNED_INT, 0); // Draw it as triangles
        glBindVertexArray(0);              // Remove the binding

        // TODO: Unbind texture
        glBindTexture(GL_TEXTURE_2D, 0);
        checkError();

        // Step 4: Swap the draw and back buffers to display the rendered image

        glfwSwapBuffers(window);
        glfwPollEvents();
        checkError();
    }

    public void takeScreenshot(String output_path) {
        int bpp = 4;

        // Take screenshot of the fixed size irrespective of the window resolution
        int screenshot_width = 800;
        int screenshot_height = 600;

        //camera.setAzimuth( (float)(3.*Math.PI/4.) );

        int fbo = glGenFramebuffers();
        glBindFramebuffer( GL_FRAMEBUFFER, fbo );

        int rgb_rb = glGenRenderbuffers();
        glBindRenderbuffer( GL_RENDERBUFFER, rgb_rb );
        glRenderbufferStorage( GL_RENDERBUFFER, GL_RGBA, screenshot_width, screenshot_height );
        glFramebufferRenderbuffer( GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, rgb_rb );

        int depth_rb = glGenRenderbuffers();
        glBindRenderbuffer( GL_RENDERBUFFER, depth_rb );
        glRenderbufferStorage( GL_RENDERBUFFER, GL_DEPTH_COMPONENT, screenshot_width, screenshot_height );
        glFramebufferRenderbuffer( GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depth_rb );
        checkError();

        float old_ar = camera.getAspectRatio();
        camera.setAspectRatio( (float)screenshot_width  / screenshot_height );
        glViewport(0,0, screenshot_width, screenshot_height );
        render();
        camera.setAspectRatio( old_ar );

        glReadBuffer(GL_COLOR_ATTACHMENT0);
        ByteBuffer buffer = BufferUtils.createByteBuffer(screenshot_width * screenshot_height * bpp);
        glReadPixels(0, 0, screenshot_width, screenshot_height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        checkError();

        glBindFramebuffer( GL_FRAMEBUFFER, 0 );
        glDeleteRenderbuffers( rgb_rb );
        glDeleteRenderbuffers( depth_rb );
        glDeleteFramebuffers( fbo );
        checkError();

        BufferedImage image = new BufferedImage(screenshot_width, screenshot_height, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < screenshot_width; ++i) {
            for (int j = 0; j < screenshot_height; ++j) {
                int index = (i + screenshot_width * (screenshot_height - j - 1)) * bpp;
                int r = buffer.get(index + 0) & 0xFF;
                int g = buffer.get(index + 1) & 0xFF;
                int b = buffer.get(index + 2) & 0xFF;
                image.setRGB(i, j, 0xFF << 24 | r << 16 | g << 8 | b);
            }
        }
        try {
            ImageIO.write(image, "png", new File(output_path));
        } catch (IOException e) {
            throw new RuntimeException("failed to write output file - ask for a demonstrator");
        }
    }

    public void stop() {
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void checkError() {
        int error = glGetError();
        if (error != GL_NO_ERROR)
            throw new RuntimeException("OpenGL produced an error (code " + error + ") - ask for a demonstrator");
    }

    /* Set the starting azimuth of the camera */
    public void setCameraAzimuth(float camera_azimuth) {
        camera.setAzimuth( camera_azimuth );
    }
}
