package uk.ac.cam.cl.furthergfx.jhah2.tick;

import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;


/**
 * Extends the basic {@link TickCanvas} to support camera, mouse and keyboard controls and automatic
 * reloading of the fragment shader
 */
public class TickApp extends TickCanvas {

  private static final String ROOT = "resources/";
  private static final String VERTEX_SHADER = "vertex_shader.glsl";
  private static final String FRAGMENT_SHADER = "fragment_shader.glsl";

  private int program = -1;
  private Camera camera;
  private String screenshotTarget;
  private long shaderTimestamp = 0;
  private long then;
  private boolean showStepDepth = false;

  public TickApp() {
    this.camera = new Camera();
    this.then = System.currentTimeMillis();
    this.screenshotTarget = null;
  }

  @Override
  public void init() {
    super.init();
    updateResolutionUniform();

    Texture.load("resources/sky.jpg", 0);
    Texture.load("resources/seabed.jpg", 2);
    Texture.load("resources/seanorm.jpg", 3);
    Texture.load("resources/seabump.jpg", 4);
  }

  @Override
  protected void render() {
    boolean animated = camera.isAnimated();

    maybeReloadFragmentShader();
    updateTimeUniform();
    if (animated) {
      updateCameraUniforms();
    }
    super.render();

    if (!animated && screenshotTarget != null) {
      takeScreenshot(screenshotTarget);
      screenshotTarget = null;
    }
  }

  @Override
  protected void onResized(int width, int height) {
    super.onResized(width, height);
    updateResolutionUniform();
  }

  @Override
  protected void onMouseDrag(double dx, double dy) {
    camera.rotate(dx, dy);
    updateCameraUniforms();
  }

  @Override
  protected void onMouseScroll(double delta) {
    camera.zoom(delta > 0);
    updateCameraUniforms();
  }

  @Override
  protected void onKeyPressed(int key) {
    switch (key) {
    case Keyboard.KEY_ESCAPE:
      System.exit(0);
      break;
    case Keyboard.KEY_1:
      camera.animateTo(0, 0, 12);
      break;
    case Keyboard.KEY_2:
      camera.animateTo(0, (float) (Math.PI / 2.000 - 0.0001), 16);
      break;
    case Keyboard.KEY_3:
      camera.animateTo((float) Math.PI / 4, (float) Math.PI / 4, 20);
      break;
    case Keyboard.KEY_R:
      showStepDepth = !showStepDepth;
      updateStepCountUniform();
      break;
    case Keyboard.KEY_S:
      String[] options = new String[] { "Task 1", "Task 2", "Task 3", "Task 4", "Task 5", "Task 6" };
      Vector3f[] cameraPositions = new Vector3f[] {
          new Vector3f(0.4849999f, 0.32499993f, 11.81964f),
          new Vector3f(-0.46999985f, 0.4006975f, 36.672264f),
          new Vector3f(0.0f, 1.5706964f, 51.601543f),
          new Vector3f(-3.0596035f, 0.8249996f, 49.393562f),
          new Vector3f(4.1004214f, 0.5000006f, 40.636215f),
      };
      int response = JOptionPane.showOptionDialog(null,
          "This will capture a reference screenhot of your work.  Choose the Tick task you are completing.",
          "Select task", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
      if (response >= 0) {
        screenshotTarget = options[response].replace(" ",  "") + ".png";
        if (response < 5) {
          camera.animateTo(cameraPositions[response].x, cameraPositions[response].y, cameraPositions[response].z);
        }
      }
      break;
    }
  }

  private void maybeReloadFragmentShader() {
    long timestamp = getFragmentShaderTimestamp();
    if (shaderTimestamp != timestamp) {
      program = loadFragmentShader();
      shaderTimestamp = timestamp;
      updateAllUniforms();
    }
  }

  private int loadFragmentShader() {
    try {
      int vShader = loadShader(GL20.GL_VERTEX_SHADER, ROOT + VERTEX_SHADER);
      int fShader = loadShader(GL20.GL_FRAGMENT_SHADER, ROOT + FRAGMENT_SHADER);
      int program = GL20.glCreateProgram();

      GL20.glAttachShader(program, vShader);
      GL20.glAttachShader(program, fShader);
      GL20.glLinkProgram(program);
      GL20.glValidateProgram(program);
      GL20.glUseProgram(program);
      checkError();
      System.out.println("Succesfully loaded '" + FRAGMENT_SHADER + "'");
      return program;
    } catch (RuntimeException e) {
      if (e.getMessage() != null) {
        System.err.println(e.getMessage());
      } else if (e instanceof NullPointerException) {
        System.err.println("File not found: '" + FRAGMENT_SHADER + "'");
      } else {
        System.err.println("Unspecified error while loading '" + FRAGMENT_SHADER + "'");
        e.printStackTrace();
      }
      return -1;
    }
  }

  private long getFragmentShaderTimestamp() {
    try {
      return new File(ROOT + FRAGMENT_SHADER).lastModified();
    } catch (Exception e) {
      return -1;
    }
  }

  private static int loadShader(int shaderType, String name) {
    int shader = GL20.glCreateShader(shaderType);
    GL20.glShaderSource(shader, readFile(name));
    GL20.glCompileShader(shader);
    checkShader(name, shader);
    return shader;
  }

  private static String[] readFile(String filename) {
    List<String> lines = new ArrayList<>();
    try (Stream<String> stream = Files.lines(Paths.get(filename))) {
      stream.forEach(s -> lines.add(s + "\n"));
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return lines.toArray(new String[0]);
  }

  private static void checkShader(String name, int shader) {
    if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
      String infolog = GL20.glGetShaderInfoLog(shader, 4096 /* buffer size */);
      if (infolog != null && !infolog.isEmpty() && !infolog.trim().equals("No errors.")) {
        throw new RuntimeException(name + ": " + infolog.trim());
      }
    }
  }

  private static void checkError() {
    int error = glGetError();
    if (error != GL_NO_ERROR) {
      throw new RuntimeException("OpenGL failed with error code '" + error + "'");
    }
  }

  private void updateAllUniforms() {
    updateResolutionUniform();
    updateCameraUniforms();
    updateTimeUniform();
    updateStepCountUniform();
    updateTextures();
  }

  private void updateResolutionUniform() {
    updateUniformVec2("resolution", Display.getWidth(), Display.getHeight());
  }

  private void updateTimeUniform() {
    long now = System.currentTimeMillis();
    updateUniformFloat("currentTime", ((float) (now - then)) / 1000.0f);
  }

  private void updateCameraUniforms() {
    Vector3f camPos = camera.getPos();
    Vector3f camDir = camPos.normalize(new Vector3f()).negate();
    Vector3f camUp = camera.getUp();

    updateUniformVec3("camPos", camPos);
    updateUniformVec3("camDir", camDir);
    updateUniformVec3("camUp", camUp);
  }

  private void updateTextures() {
    int skyLoc = GL20.glGetUniformLocation(program, "texSky");
    int bedLoc = GL20.glGetUniformLocation(program, "texBed");
    int normLoc = GL20.glGetUniformLocation(program, "texNorm");
    int bumpLoc = GL20.glGetUniformLocation(program, "texBump");

    GL20.glUniform1i(skyLoc,  0);
    GL20.glUniform1i(bedLoc,  2);
    GL20.glUniform1i(normLoc, 3);
    GL20.glUniform1i(bumpLoc, 4);
  }

  private void updateStepCountUniform() {
    updateUniformBool("showStepDepth", showStepDepth);
  }

  private void updateUniformVec3(String uniformName, Vector3f v) {
    int uniform = getUniformLocation(uniformName);
    if (uniform != -1) {
      GL20.glUniform3f(uniform, v.x, v.y, v.z);
    }
  }

  private void updateUniformFloat(String uniformName, float f) {
    int uniform = getUniformLocation(uniformName);
    if (uniform != -1) {
      GL20.glUniform1f(uniform, f);
    }
  }

  private void updateUniformBool(String uniformName, boolean b) {
    int uniform = getUniformLocation(uniformName);
    if (uniform != -1) {
      GL20.glUniform1i(uniform, b ? 1 : 0);
    }
  }

  private void updateUniformVec2(String uniformName, float a, float b) {
    int uniform = getUniformLocation(uniformName);
    if (uniform != -1) {
      GL20.glUniform2f(uniform, a, b);
    }
  }

  private int getUniformLocation(String uniformName) {
    return (program == -1) ? -1 : GL20.glGetUniformLocation(program, uniformName);
  }

  private void takeScreenshot(String path) {
    int bpp = 4;
    int w = 2560;
    int h = 1440;
    GLFrameBuffer fb = new GLFrameBuffer(w, h);

    fb.activate();
    updateUniformVec2("resolution", w, h);
    
    super.render();

    ByteBuffer buffer = BufferUtils.createByteBuffer(w * h * bpp);
    GL11.glReadBuffer(GL11.GL_FRONT);
    GL11.glReadPixels(0, 0, w, h, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    for (int i = 0; i < w; ++i) {
      for (int j = 0; j < h; ++j) {
        int index = (i + w * (h - j - 1)) * bpp;
        int r = buffer.get(index + 0) & 0xFF;
        int g = buffer.get(index + 1) & 0xFF;
        int b = buffer.get(index + 2) & 0xFF;
        image.setRGB(i, j, 0xFF << 24 | r << 16 | g << 8 | b);
      }
    }

    fb.deactivate();
    fb.dispose();
    updateResolutionUniform();
    
    try {
      ImageIO.write(image, "png", new File(path));
    } catch (IOException e) {
      throw new RuntimeException("failed to write output file '" + path + "'");
    }
  }
}
