package uk.ac.cam.cl.furthergfx.jhah2.tick;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class Texture {

  public static int load(String filename, int idx) {
    ByteBuffer buffer = null;
    int tWidth = 0;
    int tHeight = 0;

    // Link the PNG decoder to this stream
    BufferedImage image = loadImageFromFile(filename);

    // Get the width and height of the texture
    tWidth = image.getWidth();
    tHeight = image.getHeight();

    // Decode the PNG file in a ByteBuffer
    buffer = ByteBuffer.allocateDirect(4 * image.getWidth() * image.getHeight());
    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        int argb = image.getRGB(x, y);
        buffer.put((x + y * image.getWidth()) * 4 + 0, (byte) ((argb >> 16) & 0xFF));
        buffer.put((x + y * image.getWidth()) * 4 + 1, (byte) ((argb >> 8) & 0xFF));
        buffer.put((x + y * image.getWidth()) * 4 + 2, (byte) ((argb >> 0) & 0xFF));
        buffer.put((x + y * image.getWidth()) * 4 + 3, (byte) ((argb >> 24) & 0xFF));
      }
    }
    buffer.flip();
    buffer.limit(buffer.capacity());

    // Create a new texture object in memory and bind it
    int texId = GL11.glGenTextures();
    GL13.glActiveTexture(GL13.GL_TEXTURE0 + idx);
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);

    // All RGB bytes are aligned to each other and each component is 1 byte
    GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

    // Upload the texture data and generate mip maps (for scaling)
    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, tWidth, tHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

    // Setup what to do when the texture has to be scaled
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

    return texId;
  }

  private static BufferedImage loadImageFromFile(String path) {
    try {
      return ImageIO.read(new File(path));
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

}
