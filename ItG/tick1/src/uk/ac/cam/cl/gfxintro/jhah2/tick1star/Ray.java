package uk.ac.cam.cl.gfxintro.jhah2.tick1star;

public class Ray {

	// Ray parameters
	private Vector3 origin, direction;

	public Ray(Vector3 origin, Vector3 direction) {
		this.origin = origin;
		this.direction = direction;
	}

	public Vector3 getOrigin() {
		return origin;
	}

	public Vector3 getDirection() {
		return direction;
	}
	
	// Determine position for certain scalar parameter distance i.e. (origin + direction * distance)
	public Vector3 evaluateAt(double distance) {
		return origin.add(direction.scale(distance));
	}
}
