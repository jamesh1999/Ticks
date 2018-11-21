package uk.ac.cam.cl.gfxintro.jhah2.tick1star;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class BumpySphere extends Sphere {

	private float BUMP_FACTOR = 10f;
	private float[][] bumpMap;
	private int bumpMapHeight;
	private int bumpMapWidth;

	public BumpySphere(Vector3 position, double radius, ColorRGB colour, String bumpMapImg) {
		super(position, radius, colour);
		try {
			BufferedImage inputImg = ImageIO.read(new File(bumpMapImg));
			bumpMapHeight = inputImg.getHeight();
			bumpMapWidth = inputImg.getWidth();
			bumpMap = new float[bumpMapHeight][bumpMapWidth];
			for (int row = 0; row < bumpMapHeight; row++) {
				for (int col = 0; col < bumpMapWidth; col++) {
					float height = (float) (inputImg.getRGB(col, row) & 0xFF) / 0xFF;
					bumpMap[row][col] = BUMP_FACTOR * height;
				}
			}
		} catch (IOException e) {
			System.err.println("Error creating bump map");
			e.printStackTrace();
		}
	}

	// Get normal to surface at position
	@Override
	public Vector3 getNormalAt(Vector3 position) {
		
		Vector3 surfaceNormal = position.subtract(getPosition()).normalised();
		Vector3 Pu = Vector3.fromSphericalCoords(1, surfaceNormal.phi() + Math.PI/2, surfaceNormal.theta());
		Vector3 Pv = Vector3.fromSphericalCoords(1, surfaceNormal.phi(), surfaceNormal.theta() + Math.PI/2);

		double u = (surfaceNormal.phi() / Math.PI / 2 + 0.5) * bumpMapWidth;
		double v = surfaceNormal.theta() / Math.PI * bumpMapHeight;

		// Interpolation coefficients
		double uCoeff = u - Math.floor(u);
		double vCoeff = v - Math.floor(v);

		int pu = (int) u;
		int pv = (int) v;

		double Bu = BUMP_FACTOR * bilinearInterp(uCoeff, vCoeff,
			sample(pu, pv)-sample(pu-1, pv),
			sample(pu, pv+1)-sample(pu-1, pv+1),
			sample(pu+1, pv)-sample(pu, pv),
			sample(pu+1, pv+1)-sample(pu, pv+1));
		double Bv = BUMP_FACTOR * bilinearInterp(uCoeff, vCoeff,
			sample(pu, pv)-sample(pu, pv-1),
			sample(pu, pv+1)-sample(pu, pv),
			sample(pu+1, pv)-sample(pu+1, pv-1),
			sample(pu+1, pv+1)-sample(pu+1, pv));

		return surfaceNormal
			.add(surfaceNormal.cross(Pu).scale(Bv))
			.add(surfaceNormal.cross(Pv).scale(-Bu))
			.normalised();
	}

	// Helper functions to wrap pixel coords that are off of the bumpmap
	// Consider introducing a Texture class
	private int wrapW(int x)
	{
		if(x < 0) x += bumpMapWidth;
		if(x >= bumpMapWidth) x -= bumpMapWidth;
		return x;
	}
	private int wrapH(int x)
	{
		if(x < 0) x += bumpMapHeight;
		if(x >= bumpMapHeight) x -= bumpMapHeight;
		return x;
	}
	//Performs bilinear interpolation
	private double bilinearInterp(double x,double y,double p00,double p01,double p10,double p11)
	{
		return y * (x * p11 + (1-x) * p01) + (1 - y) * (x * p10 + (1-x) * p00);
	}
	// Samples bumpmap
	private double sample(int u, int v)
	{
		return bumpMap[wrapH(v)][wrapW(u)];
	}
}
