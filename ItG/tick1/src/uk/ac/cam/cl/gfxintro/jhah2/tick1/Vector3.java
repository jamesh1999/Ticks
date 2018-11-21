package uk.ac.cam.cl.gfxintro.jhah2.tick1;

public class Vector3 {
	public final double x, y, z;

	public Vector3(double uniform) {
		this(uniform, uniform, uniform);
	}

	public Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	// Add two vectors together
	public Vector3 add(Vector3 other) {
		return new Vector3(x + other.x, y + other.y, z + other.z);
	}

	// Add a scalar to a vector
	public Vector3 add(double other) {
		return new Vector3(x + other, y + other, z + other);
	}

	// Subtract two vectors
	public Vector3 subtract(Vector3 other) {
		return new Vector3(x - other.x, y - other.y, z - other.z);
	}

	// Scale a vector by a scalar
	public Vector3 scale(double scalar) {
		return new Vector3(scalar * x, scalar * y, scalar * z);
	}

	// Hadamard product, scales the vector in an element-wise fashion
	public Vector3 scale(Vector3 other) {
		return new Vector3(x * other.x, y * other.y, z * other.z);
	}

	// Dot product of two vectors
	public double dot(Vector3 other) {
		return x * other.x + y * other.y + z * other.z;
	}

	// Cross product of two vectors
	public Vector3 cross(Vector3 other) {
		return new Vector3(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x);
	}

	// Element-wise power function
	public Vector3 power(double e) {
		return new Vector3( Math.pow(x,e), Math.pow(y,e), Math.pow(z,e) );
	}

	// Element-wise inverse (1/v)
	public Vector3 inv() {
		return new Vector3( 1/x, 1/y, 1/z );
	}


	// Magnitude of a vector
	public double magnitude() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	// Normalise a vector
	public Vector3 normalised() {
		double magnitude = this.magnitude();
		return new Vector3(x / magnitude, y / magnitude, z / magnitude);
	}

	// Calculate mirror-like reflection
	public Vector3 reflectIn(Vector3 N) {
		return N.scale(2 * this.dot(N)).subtract(this);
	}

	// Creates a random vector inside the unit sphere
	public static Vector3 randomInsideUnitSphere() {

		double r = Math.random();
		double theta = Math.random() * Math.PI;
		double phi = Math.random() * Math.PI * 2;

		double x = r * Math.sin(theta) * Math.cos(phi);
		double y = r * Math.sin(theta) * Math.sin(phi);
		double z = r * Math.cos(theta);

		return new Vector3(x, y, z);
	}

	// Determine if two vectors are equal
	public boolean equals(Vector3 other) {
		return x == other.x && y == other.y && z == other.z;
	}
}
