package uk.ac.cam.cl.gfxintro.jhah2.tick1star;

import java.util.LinkedList;
import java.util.List;

public class Scene {

	// A list of 3D objects to be rendered
	private List<SceneObject> objects;

	// A list of point light sources
	private List<PointLight> pointLights;

	// The color of the ambient light in the scene
	private ColorRGB ambientLight;

	public Scene() {
		objects = new LinkedList<SceneObject>();
		pointLights = new LinkedList<PointLight>();
		ambientLight = new ColorRGB(1);
	}

	public void addObject(SceneObject object) {
		objects.add(object);
	}

	// Find the closest intersection of given ray with an object in the scene
	public RaycastHit findClosestIntersection(Ray ray) {
		RaycastHit closestHit = new RaycastHit(); // initially no intersection

		// Loop over objects and find closest intersection
		for (SceneObject object : objects) {
			RaycastHit trialHit = object.intersectionWith(ray);
			if (trialHit.getDistance() < closestHit.getDistance()) {
				closestHit = trialHit;
			}
		}
		return closestHit;
	}

	public ColorRGB getAmbientLighting() {
		return ambientLight;
	}

	public void setAmbientLight(ColorRGB ambientLight) {
		this.ambientLight = ambientLight;
	}

	public PointLight getPointLight() {
		return pointLights.get(0);
	}

	public List<PointLight> getPointLights() {
		return pointLights;
	}

	public void addPointLight(PointLight pointLight) {
		pointLights.add(pointLight);
	}

}
