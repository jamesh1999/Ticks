package uk.ac.cam.cl.gfxintro.jhah2.tick1;

public class Sphere extends SceneObject {

	// Sphere coefficients
	private final double SPHERE_KD = 0.8;
	private final double SPHERE_KS = 1.2;
	private final double SPHERE_ALPHA = 10;
	private final double SPHERE_REFLECTIVITY = 0.9;

	// The world-space position of the sphere
	private Vector3 position;

	public Vector3 getPosition() {
		return position;
	}

	// The radius of the sphere in world units
	private double radius;

	public Sphere(Vector3 position, double radius, ColorRGB colour) {
		this.position = position;
		this.radius = radius;
		this.colour = colour;

		this.phong_kD = SPHERE_KD;
		this.phong_kS = SPHERE_KS;
		this.phong_alpha = SPHERE_ALPHA;
		this.reflectivity = SPHERE_REFLECTIVITY;
	}

	/*
	 * Calculate intersection of the sphere with the ray. If the ray starts inside the sphere,
	 * intersection with the surface is also found.
	 */
	public RaycastHit intersectionWith(Ray ray) {

		// Get ray parameters
		Vector3 O = ray.getOrigin();
		Vector3 D = ray.getDirection();
		
		// Get sphere parameters
		Vector3 C = position;
		double r = radius;

		// Calculate quadratic coefficients
		double a = D.dot(D);
		double b = 2 * D.dot(O.subtract(C));
		double c = (O.subtract(C)).dot(O.subtract(C)) - Math.pow(r, 2);

        // s1,s1 - Position of both intersections (from quadratic formula)
        // Conditions will fail for NaN
        double s1 = (-b - Math.sqrt(b*b - 4*a*c)) / (2 * a);
        if (s1 > 0)
        {
        	Vector3 pos = O.add(D.scale(s1));
        	return new RaycastHit(this, s1, pos, getNormalAt(pos)); 
        }

        double s2 = (-b + Math.sqrt(b*b - 4*a*c)) / (2 * a);
        if (s2 > 0)
        {
        	Vector3 pos = O.add(D.scale(s2));
        	return new RaycastHit(this, s2, pos, getNormalAt(pos)); 
        }

        return new RaycastHit();
	}

	// Get normal to surface at position
	public Vector3 getNormalAt(Vector3 position) {
		return position.subtract(this.position).normalised();
	}
}
