package uk.ac.cam.cl.gfxintro.jhah2.tick2;

import org.joml.Vector3f;

public class Tick2Tests {

    private boolean failed, failed_test;

    public Tick2Tests() {
        failed = failed_test = false;
    }

    private void start(String test) {
        System.out.print(test + "...");
        failed_test = false;
    }

    private void fail() {
        failed_test = true;
    }

    private void finish() {
        if (failed_test) {
            System.out.println("(failed)");
            failed = true;
        } else
            System.out.println("OK");
    }

    private void check(boolean condition) {
        if (!condition)
            fail();
    }

    private static int mesh_width = 128;
    private static int mesh_height = 128;

    private int getVertexIndex( int col, int row ) {
        return 3 * (mesh_width * row + col);
    }

    private boolean isAlmostTheSame( float a, float b ) {
        return Math.abs( a-b ) < 0.00001;
    }




    private void runTests() {

        try {
            OpenGLApplication app = new OpenGLApplication("resources/mtsthelens.png");
            float[][] hm = app.getHeightmap();

            float[] vertices = app.initializeVertexPositions( hm );
            start("Testing for the right count of vertices");
            {
                if( vertices.length != mesh_width*mesh_height*3 )
                    fail();
            }
            finish();

            start("Testing whether (x,z) vertex coordinates of the corners have the right values");
            {
                float MAP_SIZE = 10;

                if(     !isAlmostTheSame( vertices[getVertexIndex(0,0)], -MAP_SIZE/2) ||   // top-left, x
                        !isAlmostTheSame( vertices[getVertexIndex(0,0)+2], -MAP_SIZE/2) || // top-left, z
                        !isAlmostTheSame( vertices[getVertexIndex(mesh_width-1,0)], MAP_SIZE/2) ||  // top-right, x
                        !isAlmostTheSame( vertices[getVertexIndex(mesh_width-1,0)+2], -MAP_SIZE/2) || // top-right, z
                        !isAlmostTheSame( vertices[getVertexIndex(0,mesh_height-1)], -MAP_SIZE/2) ||   // bottom-left, x
                        !isAlmostTheSame( vertices[getVertexIndex(0,mesh_height-1)+2], MAP_SIZE/2) || // bottom-left, z
                        !isAlmostTheSame( vertices[getVertexIndex(mesh_width-1,mesh_height-1)], MAP_SIZE/2) ||  // bottom-right, x
                        !isAlmostTheSame( vertices[getVertexIndex(mesh_width-1,mesh_height-1)+2], MAP_SIZE/2)  // bottom-right, z
                        )
                    fail();
            }
            finish();


            int[] indices = app.initializeVertexIndices( hm );
            start("Testing for the right count of indices");
            {
                if( indices.length != (128-1)*(128-1)*2*3 )
                    fail();
            }
            finish();

            float[] normals = app.initializeVertexNormals( hm );
            start("Testing for the right count of normals");
            {
                if( normals.length != 128*128*3 )
                    fail();
            }
            finish();

            start("Testing whether indices are within the range");
            {
                for( int i = 0; i < indices.length; i++ ) {
                    if( indices[i] < 0 || indices[i] >= vertices.length )
                        fail();
                }

            }
            finish();

            start("Testing whether all triangles are facing up (right order or indices)");
            {
                for( int i = 0; i < indices.length / 3; i += 3 ) {
                    Vector3f a = new Vector3f( vertices[indices[i]*3], vertices[indices[i]*3+1], vertices[indices[i]*3+2] );
                    Vector3f b = new Vector3f( vertices[indices[i+1]*3], vertices[indices[i+1]*3+1], vertices[indices[i+1]*3+2] );
                    Vector3f c = new Vector3f( vertices[indices[i+2]*3], vertices[indices[i+2]*3+1], vertices[indices[i+2]*3+2] );

                    Vector3f v1 = a.sub(b);
                    Vector3f v2 = c.sub(b);
                    Vector3f cp = v2.cross(v1);
                    if( cp.length() < 1e-4 )
                        fail();

                    if( cp.y <= 0 )
                        fail();
                }
            }
            finish();

            start("Testing whether normals at the edge of the surface are directed upwards");
            {
                final int width = 128, height = 128;
                for( int i = 0; i < width*3; i += 3 ) {
                    Vector3f n = new Vector3f( normals[i], normals[i+1], normals[i+2] );
                    if( n.sub(0,1,0).length() >= 1e-4 )
                        fail();
                }
                for( int i = 0; i < height; i++ ) {
                    Vector3f n = new Vector3f( normals[i*3*width], normals[i*3*width+1], normals[i*3*width+2] );
                    if( n.sub(0,1,0).length() >= 1e-4 )
                        fail();
                    n = new Vector3f( normals[(i*width+width-1)*3], normals[(i*width+width-1)*3+1], normals[(i*width+width-1)*3+2] );
                    if( n.sub(0,1,0).length() >= 1e-4 )
                        fail();
                }
                for( int i = (height-1)*width*3; i < width*height*3; i += 3 ) {
                    Vector3f n = new Vector3f( normals[i], normals[i+1], normals[i+2] );
                    if( n.sub(0,1,0).length() >= 1e-4 )
                        fail();
                }
            }
            finish();

            start("Testing whether all normals have the right orientation");
            {
                for( int i = 0; i < normals.length/3; i += 3 ) {
                    Vector3f n = new Vector3f( normals[i], normals[i+1], normals[i+2] );

                    if( n.length() < 1e-4 )
                        fail();

                    if( n.y <= 0 )
                        fail();
                }
            }
            finish();

        }
        catch( Exception ex )
        {
            System.out.println( "Something went wrong with the unit tester:" + ex.getMessage() );
            failed = true;
        }

    }

    public void test() {
        failed = false;
        runTests();
        if (failed) {
            System.err.println("Unit testing has failed");
            System.exit(1);
        } else {
            System.out.println("Unit testing completed successfully");
        }
    }

    public static void main(String[] args) {
        new Tick2Tests().test();
    }
}