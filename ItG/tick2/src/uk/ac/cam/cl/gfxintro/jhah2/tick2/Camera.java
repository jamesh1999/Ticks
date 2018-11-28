package uk.ac.cam.cl.gfxintro.jhah2.tick2;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Stores the position of the camera in spherical coordinates. The camera is always directed towards point 0,0,0.
 *
 * The class facilitates computation of the view and projection matrices for a camera that is rotating around an object
 * located at the origin.
 */
public class Camera {

    // Spherical coordinates for positioning camera
    private float azimuth; // Angle camera makes with +Z
    private float elevation; // Angle camera makes with ground plane
    private float distance; // Distance from camera to origin

    private float fov_y; // Vertical field-of-view of camera in radians
    private float aspect_ratio; // Aspect ratio of camera

    public Camera(double aspect_ratio, float fov_y) {
        this.aspect_ratio = (float) aspect_ratio;
        this.azimuth = (float)Math.PI / 4;
        this.elevation = (float)Math.PI / 4;
        this.distance = 16;
        this.fov_y = fov_y;
    }

    public void rotate(double dx, double dy) {
        final float sensitivity = -.005f;
        azimuth += sensitivity * dx;
        elevation -= sensitivity * dy;
        elevation = (float)(Math.max(0, Math.min(Math.PI / 2 - 0.001, elevation)));
    }

    public void zoom(boolean in) {
        final float sensitivity = 1.05f;
        if (in)
            distance /= sensitivity;
        else
            distance *= sensitivity;
    }

    public Matrix4f getViewMatrix() {
        Vector3f position = getCameraPosition();
        Vector3f origin = new Vector3f(0, 0, 0);
        Vector3f up = new Vector3f(0, 1, 0);

        return new Matrix4f().lookAt(position, origin, up);
    }

    public Vector3f getCameraPosition() {
        //compute camera position from elevation and azimuth
        Vector3f position = new Vector3f((float) (distance * Math.cos(elevation) * Math.sin(azimuth)),
                (float) (distance * Math.sin(elevation)), (float) (distance * Math.cos(elevation) * Math.cos(azimuth)));
        return position;
    }

    public Matrix4f getProjectionMatrix() {
        return new Matrix4f().perspective(fov_y, aspect_ratio, 0.01f, 100f);
    }

    public float getAzimuth() { return azimuth; }
    public float getElevation() { return elevation; }
    public float getDistance() { return distance; }
    public float getAspectRatio() { return aspect_ratio; }
    public void setAzimuth(float azimuth) { this.azimuth = azimuth; }
    public void setElevation(float elevation) { this.elevation = elevation; }
    public void setDistance(float distance) { this.distance = distance; }
    public void setAspectRatio(float aspect_ratio) { this.aspect_ratio = aspect_ratio; }
}