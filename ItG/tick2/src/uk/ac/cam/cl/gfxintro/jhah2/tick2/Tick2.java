package uk.ac.cam.cl.gfxintro.jhah2.tick2;

public class Tick2 {
    public static final String DEFAULT_INPUT = "resources/mtsthelens.png";

    public static void usageError() {
        System.err.println("USAGE: <tick3> --input INPUT [--output OUTPUT] [--azimuth <degrees>]");
        System.exit(-1);
    }

    public static void main(String[] args) {
        // We should have an even number of arguments
        if (args.length % 2 != 0)
            usageError();

        String input = DEFAULT_INPUT, output = null;
        float camera_azimuth = (float)Math.PI/4.f;
        for (int i = 0; i < args.length; i += 2) {
            switch (args[i]) {
            case "-i":   // The file with the height map
            case "--input":
                input = args[i + 1];
                break;
            case "-o":   // If specified, save screenshot and quit
            case "--output":
                output = args[i + 1];
                break;
            case "-a":   // Set the camera to a specific azimuth in degrees (from 0 to 360)
            case "--azimuth":
                camera_azimuth = new Float(args[i+1]).floatValue() / 180.f * (float)Math.PI;
                break;
            default:
                System.err.println("unknown option: " + args[i]);
                usageError();
            }
        }

        if (input == null) {
            System.err.println("required arguments not present");
            usageError();
        }

        OpenGLApplication app = null;
        try {
            app = new OpenGLApplication(input);
            app.initializeOpenGL();
            app.setCameraAzimuth(camera_azimuth);

            if (output != null) {
                app.takeScreenshot(output);
            } else {
                app.run();
            }
        } catch( RuntimeException ex ) {
            System.err.println( "RuntimeException: " + ex.getMessage() );
        } finally {
            if (app != null)
                app.stop();
        }
    }
}
