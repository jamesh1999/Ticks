package uk.ac.cam.cl.furthergfx.jhah2.tick;

import org.joml.Vector3f;

/**
 * Stores the position of the camera in spherical coordinates. The camera is
 * always directed towards point 0,0,0.  Animates transitions between positions.
 */
public class Camera {
  
  private static final int NUM_ANIMATION_STEPS = 33;
  
  // Spherical coordinates for positioning camera
  private float azimuth; // Angle camera makes with +Z
  private float elevation; // Angle camera makes with ground plane
  private float distance; // Distance from camera to origin

  private float targetAzimuth;
  private float targetElevation;
  private float targetDistance;
  private int numAnimationStepsRemaining;

  public Camera() {
    this.azimuth = 0;
    this.elevation = 0;
    this.distance = 8;
    this.numAnimationStepsRemaining = 0;
  }

  public void rotate(double dx, double dy) {
    final float sensitivity = -.005f;
    azimuth += sensitivity * dx;
    elevation -= sensitivity * dy;
    elevation = (float) (Math.max(0, Math.min(Math.PI / 2 - 0.001, elevation)));
  }

  public void zoom(boolean in) {
    final float sensitivity = 1.05f;
    if (in) {
      distance /= sensitivity;
    } else {
      distance *= sensitivity;
    }
  }

  public Vector3f getPos() {
    // compute camera position from elevation and azimuth
    Vector3f position = new Vector3f(
        (float) (distance * Math.cos(elevation) * Math.sin(azimuth)),
        (float) (distance * Math.sin(elevation)), 
        (float) (distance * Math.cos(elevation) * Math.cos(azimuth)));
    return position;
  }

  public Vector3f getUp() {
    Vector3f dir = getPos().normalize(new Vector3f());
    Vector3f Y = new Vector3f(0, 1, 0);
    Vector3f X = Y.cross(dir, new Vector3f());
    return dir.cross(X, new Vector3f()).normalize();
  }
  
  public float getDistance() {
    return distance;
  }
  
  public void setAzimuth(float azimuth) {
    this.azimuth = azimuth;
  }

  public void setElevation(float elevation) {
    this.elevation = elevation;
  }

  public void setDistance(float distance) {
    this.distance = distance;
  }
  
  public void animateTo(float azimuth, float elevation, float distance) {
    targetAzimuth = azimuth;
    targetElevation = elevation;
    targetDistance = distance;
    numAnimationStepsRemaining = NUM_ANIMATION_STEPS;
  }
  
  public boolean isAnimated() {
    if (numAnimationStepsRemaining > 0) {
      azimuth += (targetAzimuth - azimuth) / numAnimationStepsRemaining;
      elevation += (targetElevation - elevation) / numAnimationStepsRemaining;
      distance += (targetDistance - distance) / numAnimationStepsRemaining;
      numAnimationStepsRemaining--;
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return "[" + azimuth + ", " + elevation + ", " + distance + "]";
  }
}