package uk.ac.cam.cl.gfxintro.jhah2.tick1star;

public class ColorRGB {

	// Vector3 member field
	private Vector3 v;
	
	// RGB Colour components
	public final double r;
	public final double g;
	public final double b;

	public ColorRGB(double uniform) {
		v = new Vector3(uniform);
		r = v.x;
		g = v.y;
		b = v.z;
	}

	public ColorRGB(double red, double green, double blue) {
		v = new Vector3(red, green, blue);
		r = v.x;
		g = v.y;
		b = v.z;
	}

	private ColorRGB(Vector3 vec) { // Private constructor
		v = new Vector3(vec.x, vec.y, vec.z);
		r = v.x;
		g = v.y;
		b = v.z;
	}

	/*
	 *  Add, subtract, scale, equals methods as in Vector3
	 */
	public ColorRGB add(ColorRGB other) {
		return new ColorRGB(v.add(other.v));
	}

	public ColorRGB add(double other) {
		return new ColorRGB( v.add(other) );
	}

	public ColorRGB subtract(ColorRGB other) {
		return new ColorRGB(v.subtract(other.v));
	}
	
	public ColorRGB scale(double scalar) {
		return new ColorRGB(v.scale(scalar));
	}
	
	public ColorRGB scale(ColorRGB other) {
		return new ColorRGB(v.scale(other.v));
	}

	public ColorRGB power(double e) { return new ColorRGB( v.power(e) ); }

	public ColorRGB inv() { return new ColorRGB( v.inv() ); }


	public boolean equals(ColorRGB other) {
		return v.equals(other.v);
	}
	
	/*
	 * Conversion to RGB value
	 */
	private static int convertToByte(double value) { // Private method
		return (int) (255 * Math.max(0, Math.min(1, value)));
	}

	public int toRGB() {
		return convertToByte(r) << 16 | convertToByte(g) << 8 | convertToByte(b) << 0;
	}
}