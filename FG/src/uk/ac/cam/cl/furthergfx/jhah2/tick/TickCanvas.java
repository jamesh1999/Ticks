package uk.ac.cam.cl.furthergfx.jhah2.tick;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.PixelFormat;

/**
 * A standalone GLFW windowed app which renders a black quad using the GLFW default shaders. Based
 * on 'HelloGL.java':
 * https://github.com/AlexBenton/Teaching/tree/master/AdvGraph1617/OpenGL%20Demos/com/bentonian/gldemos/hellogl/HelloGL.java
 */
public class TickCanvas {
  protected int vao; // Vertex Array Object
  protected int vbo; // Vertex Buffer Object

  /**
   * Set up GLFW and OpenGL
   */
  public void init() {
    setupDisplay();
    setupOpenGL();
    setupGeometry();
  }

  /**
   * Loop until window is closed
   */
  public void run() {
    while (!Display.isCloseRequested()) {
      pollInputs();
      render();
    }
  }

  /**
   * Clean up and release resources
   */
  public void shutdown() {
    GL15.glDeleteBuffers(vbo);
    GL30.glDeleteVertexArrays(vao);
    Display.destroy();
  }

  /**
   * Set up Display
   */
  protected void setupDisplay() {
    PixelFormat pixelFormat = new PixelFormat(8,8,0,8);
    ContextAttribs contextAtrributes = new ContextAttribs(3, 3)
            .withForwardCompatible(true)
            .withProfileCore(true);
    try {
      Display.setDisplayMode(new DisplayMode(800,600));
      Display.setTitle("Further Graphics - Tick");
      Display.setResizable(true);
      Display.create(pixelFormat, contextAtrributes);
      Display.makeCurrent();
      Display.setSwapInterval(1);
      Display.update();
      Display.swapBuffers();
    } catch (LWJGLException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  /**
   * Set up OpenGL itself
   */
  protected void setupOpenGL() {
    GL11.glClearColor(0.2f, 0.4f, 0.6f, 0.0f);
    GL11.glClearDepth(1.0f);
    GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
  }

  /**
   * Capture user inputs
   */
  protected void pollInputs() {
    if (Display.wasResized()){
      onResized(Display.getWidth(), Display.getHeight());
    }

    if (Keyboard.next()){
      if(!Keyboard.getEventKeyState()){ // process event on key release
        onKeyPressed(Keyboard.getEventKey());
      }
    }

    while (Mouse.next()){
     if (Mouse.getEventDWheel() != 0){
        onMouseScroll(Mouse.getEventDWheel());
      }
    }

    if (Mouse.isButtonDown(0)){
      onMouseDrag(Mouse.getDX(), -Mouse.getDY());
    }
  }
  
  protected void render() {
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    GL30.glBindVertexArray(vao);
    GL11.glDrawArrays(GL11.GL_TRIANGLES, 0 /* start */, 6 /* num vertices */);
    Display.update();
  }

  protected void onMouseScroll(double delta) {
  }

  protected void onMouseDrag(double dx, double dy) {
  }

  protected void onKeyPressed(int key) {
  }

  protected void onResized(int width, int height) {
    GL11.glViewport(0, 0, width, height);
  }

  /**
   * Set up minimal geometry to render a quad
   */
  private void setupGeometry() {

    // Fill a Java FloatBuffer object with two triangles forming a quad from (-1, -1) to (1, 1)
    float[] coords = new float[] { 
        -1, -1, 1f,
        1, -1, 1,
        -1, 1, 1,
        1, -1, 1,
        1, 1, 1,
        -1, 1, 1f
    };
    FloatBuffer fbo = BufferUtils.createFloatBuffer(coords.length);
    fbo.put(coords); // Copy the vertex coords into the floatbuffer
    fbo.flip(); // Mark the floatbuffer ready for reads

    // Store the FloatBuffer's contents in a Vertex Buffer Object
    vbo = GL15.glGenBuffers(); // Get an OGL name for the VBO
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo); // Activate the VBO
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fbo, GL15.GL_STATIC_DRAW); // Send VBO data to GPU

    // Bind the VBO in a Vertex Array Object
    vao = GL30.glGenVertexArrays(); // Get an OGL name for the VAO
    GL30.glBindVertexArray(vao); // Activate the VAO
    GL20.glEnableVertexAttribArray(0); // Enable the VAO's first attribute (0)
    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0); // Link VBO to VAO attrib 0
  }
}
