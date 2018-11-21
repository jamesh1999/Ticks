package uk.ac.cam.cl.gfxintro.jhah2.tick1star;

public abstract class SceneObject {
	
	// The diffuse colour of the object
	protected ColorRGB colour;

	// Coefficients for calculating Phong illumination
	protected double phong_kD, phong_kS, phong_alpha;

	// How reflective this object is
	protected double reflectivity;

	protected SceneObject() {
		colour = new ColorRGB(1);
		phong_kD = phong_kS = phong_alpha = reflectivity = 0;
	}

	// Intersect this object with ray
	public abstract RaycastHit intersectionWith(Ray ray);

	// Get normal to object at position
	public abstract Vector3 getNormalAt(Vector3 position);

	public ColorRGB getColour() {
		return colour;
	}

	public void setColour(ColorRGB colour) {
		this.colour = colour;
	}

	public double getPhong_kD() {
		return phong_kD;
	}

	public double getPhong_kS() {
		return phong_kS;
	}

	public double getPhong_alpha() {
		return phong_alpha;
	}

	public double getReflectivity() {
		return reflectivity;
	}

	public void setReflectivity(double reflectivity) {
		this.reflectivity = reflectivity;
	}
}
