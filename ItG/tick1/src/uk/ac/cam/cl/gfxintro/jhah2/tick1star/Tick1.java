package uk.ac.cam.cl.gfxintro.jhah2.tick1star;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Tick1 {
	// Default input and output files
	public static final String DEFAULT_INPUT = "tick1.xml";
	public static final String DEFAULT_OUTPUT = "output.png";
	
	public static final int DEFAULT_BOUNCES = 2; // Default number of ray bounces

	// Height and width of the output image
	private static final int WIDTH_PX = 4800;
	private static final int HEIGHT_PX = 4000;

	public static void usageError() { // Usage information 
		System.err.println("USAGE: <tick2> [--input INPUT] [--output OUTPUT] [--bounces BOUNCES]");
		System.exit(-1);
	}

	public static void main(String[] args) throws IOException {
		// We should have an even number of arguments - each option and its value
		if (args.length % 2 != 0) {
			usageError();
		}
		
		// Parse the input and output filenames from the arguments
		String inputSceneFile = DEFAULT_INPUT, output = DEFAULT_OUTPUT;
		int bounces = DEFAULT_BOUNCES;
		for (int i = 0; i < args.length; i += 2) {
			switch (args[i]) {
			case "-i":
			case "--input":
				inputSceneFile = args[i + 1];
				break;
			case "-o":
			case "--output":
				output = args[i + 1];
				break;
			case "-b":
			case "--bounces":
				bounces = Integer.parseInt(args[i + 1]);
				break;
			default:
				System.err.println("Unknown option: " + args[i]);
				usageError();
			}
		}

		// Create the scene from the XML file
		System.out.printf( "Loading scene '%s'\n", inputSceneFile );
		Scene scene = new SceneLoader(inputSceneFile).getScene();
		
		// Create the image and colour the pixels
		BufferedImage image = new Renderer(WIDTH_PX, HEIGHT_PX, bounces).render(scene);
		
		// Save the image to disk
		File save = new File(output);
		ImageIO.write(image, "png", save);
	}
}
