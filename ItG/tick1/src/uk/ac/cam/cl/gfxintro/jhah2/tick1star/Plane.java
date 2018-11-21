package uk.ac.cam.cl.gfxintro.jhah2.tick1star;

public class Plane extends SceneObject {
	
	// Plane constants
	private final double PLANE_KD = 0.6;
	private final double PLANE_KS = 0.0;
	private final double PLANE_ALPHA = 0.0;
	private final double PLANE_REFLECTIVITY = 0.1;

	// A point in the plane
	private Vector3 point;

	// The normal of the plane
	private Vector3 normal;

	public Plane(Vector3 point, Vector3 normal, ColorRGB colour) {
		this.point = point;
		this.normal = normal;
		this.colour = colour;

		this.phong_kD = PLANE_KD;
		this.phong_kS = PLANE_KS;
		this.phong_alpha = PLANE_ALPHA;
		this.reflectivity = PLANE_REFLECTIVITY;
	}
	
	// Intersect this plane with ray
	@Override
	public RaycastHit intersectionWith(Ray ray) {
		// Get ray parameters
		Vector3 O = ray.getOrigin();
		Vector3 D = ray.getDirection();
		
		// Get plane parameters
		Vector3 Q = this.point;
		Vector3 N = this.normal;

		double s = Q.subtract(O).dot(N) / D.dot(N);
		if (s > 0.0)
		{
        	Vector3 pos = O.add(D.scale(s));
        	return new RaycastHit(this, s, pos, getNormalAt(pos)); 
        }

		return new RaycastHit(); 
}

	// Get normal to the plane
	@Override
	public Vector3 getNormalAt(Vector3 position) {
		return normal; // normal is the same everywhere on the plane
	}
}
