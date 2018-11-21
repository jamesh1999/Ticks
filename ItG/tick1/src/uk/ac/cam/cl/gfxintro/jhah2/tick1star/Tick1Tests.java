package uk.ac.cam.cl.gfxintro.jhah2.tick1star;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Tick1Tests {
	private boolean failed, failed_test;

	public Tick1Tests() {
		failed = failed_test = false;
	}

	private void start(String test) {
		System.out.print(test + "...");
		failed_test = false;
	}

	private void fail() {
		failed_test = true;
	}

	private void finish() {
		if (failed_test) {
			System.out.println("(failed)");
			failed = true;
		} else
			System.out.println("OK");
	}

	private void check(boolean condition) {
		if (!condition)
			fail();
	}

	class IlluminateWrapper {
		public boolean failed;
		private Renderer renderer;
		private Method invoke_target;

		public IlluminateWrapper() {
			failed = false;
			renderer = new Renderer(0, 0, 1);

			try {
				invoke_target = Renderer.class.getDeclaredMethod("illuminate", Scene.class, SceneObject.class,
						Vector3.class, Vector3.class, Vector3.class);
				invoke_target.setAccessible(true);
				// check we can call it
				Scene test = new Scene();
				test.addPointLight(new PointLight(new Vector3(0), new ColorRGB(0), 0));
				illuminate(test, new Sphere(new Vector3(0), 0, new ColorRGB(0)), new Vector3(0), new Vector3(0),
						new Vector3(0));
			} catch (NoSuchMethodException e) {
				failed = true;
			}
		}

		public ColorRGB illuminate(Scene scene, SceneObject object, Vector3 P, Vector3 N, Vector3 O) {
			ColorRGB result = new ColorRGB(0);
			try {
				result = (ColorRGB) invoke_target.invoke(renderer, scene, object, P, N, O);
			} catch (IllegalAccessException | InvocationTargetException e) {
				failed = true;
			}
			return result;
		}
	}

	private void runTestsPart1() {
		System.err.println("Testing part 1 code...");
		
		// Geometry tests
		start("sphere intersection: miss by not intersecting");
		{
			Sphere s = new Sphere(new Vector3(0), 1, new ColorRGB(0));
			Ray r = new Ray(new Vector3(0, 0, -2), new Vector3(1, 0, 0));
			check(s.intersectionWith(r).getObjectHit() == null);
		}
		finish();

		start("sphere intersection: miss by facing away from the target");
		{
			Sphere s = new Sphere(new Vector3(0), 1, new ColorRGB(0));
			Ray r = new Ray(new Vector3(0, 0, -2), new Vector3(0, 0, -1));
			check(s.intersectionWith(r).getObjectHit() == null);
		}
		finish();

		start("sphere intersection: intersect at the edge");
		{
			Sphere s = new Sphere(new Vector3(0), 1, new ColorRGB(0));
			Ray r = new Ray(new Vector3(-1, 0, -2), new Vector3(0, 0, 1));
			check(s.intersectionWith(r).getDistance() == 2);
		}
		finish();

		start("sphere intersection: find first intersection");
		{
			Sphere s = new Sphere(new Vector3(0), 1, new ColorRGB(0));
			Ray r = new Ray(new Vector3(0, 0, -2), new Vector3(0, 0, 1));
			check(s.intersectionWith(r).getDistance() == 1);
		}
		finish();

		start("sphere intersection: intersect with correct object");
		{
			Sphere s = new Sphere(new Vector3(0), 1, new ColorRGB(0));
			Ray r = new Ray(new Vector3(0, 0, -2), new Vector3(0, 0, 1));
			check(s.intersectionWith(r).getObjectHit() == s);
		}
		finish();

		start("sphere intersection: intersect at correct location");
		{
			Sphere s = new Sphere(new Vector3(0), 1, new ColorRGB(0));
			Ray r = new Ray(new Vector3(0, 0, -2), new Vector3(0, 0, 1));
			check(s.intersectionWith(r).getLocation().equals(new Vector3(0, 0, -1)));
		}
		finish();

		start("sphere intersection: compute correct normal");
		{
			Sphere s = new Sphere(new Vector3(0), 1, new ColorRGB(0));
			Ray r = new Ray(new Vector3(0, 0, -2), new Vector3(0, 0, 1));
			check(s.intersectionWith(r).getNormal().equals(new Vector3(0, 0, -1)));
		}
		finish();

		// Illumination tests
		IlluminateWrapper wrapper = new IlluminateWrapper();
		start("illumination: illuminate() exists and works");
		{
			check(!wrapper.failed);
		}
		finish();

		start("illumination: no lighting");
		{
			Scene test = new Scene();
			test.addPointLight(new PointLight(new Vector3(0), new ColorRGB(0), 0));
			test.setAmbientLight(new ColorRGB(0, 0, 0));
			Sphere o = new Sphere(new Vector3(0, 0, 2), 1, new ColorRGB(1, 0, 0));
			Vector3 N = new Vector3(0, 0, -1);
			Vector3 P = new Vector3(0, 0, 1);
			Vector3 O = new Vector3(0, 0, 0);
			check(wrapper.illuminate(test, o, P, N, O).equals(new ColorRGB(0, 0, 0)));
		}
		finish();

		start("illumination: multiple lights");
		{
			PointLight l1 = new PointLight(new Vector3(0, 0, 0), new ColorRGB(1, 0, 0), 4 * Math.PI);
			PointLight l2 = new PointLight(new Vector3(0, 0, 0), new ColorRGB(0, 1, 0), 4 * Math.PI);

			Scene s1 = new Scene();
			s1.setAmbientLight(new ColorRGB(0));
			s1.addPointLight(l1);

			Scene s2 = new Scene();
			s2.setAmbientLight(new ColorRGB(0));
			s2.addPointLight(l2);

			Scene s = new Scene();
			s.setAmbientLight(new ColorRGB(0));
			s.addPointLight(l1);
			s.addPointLight(l2);

			Sphere o = new Sphere(new Vector3(0, 0, 2), 1, new ColorRGB(1, 1, 1));
			Vector3 N = new Vector3(0, 0, -1);
			Vector3 P = new Vector3(0, 0, 1);
			Vector3 O = new Vector3(0, 0, 0);
			ColorRGB reference = wrapper.illuminate(s1, o, P, N, O).add(wrapper.illuminate(s2, o, P, N, O));
			ColorRGB result = wrapper.illuminate(s, o, P, N, O);
			check(reference.equals(result));
		}
		finish();

		start("illumination: ambient lighting");
		{
			Scene test = new Scene();
			test.addPointLight(new PointLight(new Vector3(0), new ColorRGB(0), 0));
			test.setAmbientLight(new ColorRGB(1, 1, 1));
			Sphere o = new Sphere(new Vector3(0, 0, 2), 1, new ColorRGB(1, 0, 0));
			Vector3 N = new Vector3(0, 0, -1);
			Vector3 P = new Vector3(0, 0, 1);
			Vector3 O = new Vector3(0, 0, 0);
			check(wrapper.illuminate(test, o, P, N, O).equals(new ColorRGB(1, 0, 0)));
		}
		finish();

		start("illumination: diffuse lighting");
		{
			// set up a scene such that there is no specular highlight
			Scene test = new Scene();
			test.addPointLight(new PointLight(new Vector3(1, 0, -1), new ColorRGB(1, 1, 1), 4 * Math.PI));
			test.setAmbientLight(new ColorRGB(0, 0, 0));
			Sphere o = new Sphere(new Vector3(1, 0, 1), 1, new ColorRGB(1, 0, 0));
			Vector3 N = new Vector3(0, 0, -1);
			Vector3 P = new Vector3(1, 0, 0);
			Vector3 O = new Vector3(0, 0, 0);
			ColorRGB colour = wrapper.illuminate(test, o, P, N, O);
			check(colour.equals(new ColorRGB(o.getPhong_kD(), 0, 0)));
		}
		finish();

		start("illumination: specular lighting");
		{
			// set up a scene such that there is no diffuse term
			Scene test = new Scene();
			test.addPointLight(new PointLight(new Vector3(2, 0, 0), new ColorRGB(1, 1, 1), 4 * Math.PI));
			test.setAmbientLight(new ColorRGB(0, 0, 0));
			Sphere o = new Sphere(new Vector3(1, 0, 1), 1, new ColorRGB(0, 0, 0));
			Vector3 N = new Vector3(0, 0, -1);
			Vector3 P = new Vector3(1, 0, 0);
			Vector3 O = new Vector3(0, 0, 0);
			ColorRGB colour = wrapper.illuminate(test, o, P, N, O);
			check(colour.r == o.getPhong_kS() && colour.r == colour.g && colour.g == colour.b);
		}
		finish();
		System.err.println("Part 1 testing complete.\n");
	}

	private void runTestsPart2() {
		System.err.println("Testing Part 2 code...");
		
		// Geometry tests
		start("plane intersection: miss by not intersecting");
		{
			Plane p = new Plane(new Vector3(0, 0, 1), new Vector3(0, 0, -1), new ColorRGB(0));
			Ray r = new Ray(new Vector3(0), new Vector3(1, 0, 0));
			check(p.intersectionWith(r).getObjectHit() == null);
		}
		finish();

		start("plane intersection: miss by facing away from the target");
		{
			Plane p = new Plane(new Vector3(0, 0, 1), new Vector3(0, 0, -1), new ColorRGB(0));
			Ray r = new Ray(new Vector3(0), new Vector3(0, 0, -1));
			check(p.intersectionWith(r).getObjectHit() == null);
		}
		finish();

		start("plane intersection: do not intersect when parallel");
		{
			Plane p = new Plane(new Vector3(0, 0, 0.0001), new Vector3(0, 0, -1), new ColorRGB(0));
			Ray r = new Ray(new Vector3(0), new Vector3(1, 1, 0));
			check(p.intersectionWith(r).getObjectHit() == null);
		}
		finish();

		start("plane intersection: intersect when not quite parallel");
		{
			Plane p = new Plane(new Vector3(0, 0, 1), new Vector3(0, 0, -1), new ColorRGB(0));
			Ray r = new Ray(new Vector3(0), new Vector3(1, 1, 0.0001));
			check(p.intersectionWith(r).getObjectHit() != null);
		}
		finish();

		start("plane intersection: intersect with correct object");
		{
			Plane p = new Plane(new Vector3(0, 0, 1), new Vector3(0, 0, -1), new ColorRGB(0));
			Ray r = new Ray(new Vector3(0), new Vector3(0, 0, 1));
			check(p.intersectionWith(r).getObjectHit() == p);
		}
		finish();

		start("plane intersection: intersect at correct distance");
		{
			Plane p = new Plane(new Vector3(0, 0, 1), new Vector3(0, 0, -1), new ColorRGB(0));
			Ray r = new Ray(new Vector3(0), new Vector3(0, 0, 1));
			check(p.intersectionWith(r).getDistance() == 1);
		}
		finish();

		start("plane intersection: intersect at correct location");
		{
			Plane p = new Plane(new Vector3(0, 0, 1), new Vector3(0, 0, -1), new ColorRGB(0));
			Ray r = new Ray(new Vector3(0), new Vector3(0, 0, 1));
			check(p.intersectionWith(r).getLocation() != null
					&& p.intersectionWith(r).getLocation().equals(new Vector3(0, 0, 1)));
		}
		finish();

		start("plane intersection: compute correct normal");
		{
			Plane p = new Plane(new Vector3(0, 0, 1), new Vector3(0, 0, -1), new ColorRGB(0));
			Ray r = new Ray(new Vector3(0), new Vector3(0, 0, 1));
			check(p.intersectionWith(r).getNormal() != null
					&& p.intersectionWith(r).getNormal().equals(new Vector3(0, 0, -1)));
		}
		finish();

		//Illumination tests
		IlluminateWrapper wrapper = new IlluminateWrapper();
		start("illumination: illuminate() exists and works");
		{
			check(!wrapper.failed);
		}
		finish();

		if (!wrapper.failed) {
			start("illumination: no lighting");
			{
				Scene test = new Scene();
				test.setAmbientLight(new ColorRGB(0));
				Sphere o = new Sphere(new Vector3(0, 0, 2), 1, new ColorRGB(1, 1, 1));
				Vector3 N = new Vector3(0, 0, -1);
				Vector3 P = new Vector3(0, 0, 1);
				Vector3 O = new Vector3(0, 0, 0);
				check(wrapper.illuminate(test, o, P, N, O).equals(new ColorRGB(0)));
			}
			finish();

			start("illumination: shadows");
			{
				Scene test = new Scene();
				test.setAmbientLight(new ColorRGB(0));
				test.addPointLight(new PointLight(new Vector3(0, 0, 4), new ColorRGB(1), 1000));
				test.addObject(new Plane(new Vector3(0, 0, 3), new Vector3(0, 0, -1), new ColorRGB(0)));
				Sphere o = new Sphere(new Vector3(0, 0, 2), 1, new ColorRGB(1, 1, 1));

				Vector3 N = new Vector3(0, 0, -1);
				Vector3 P = new Vector3(0, 0, 1);
				Vector3 O = new Vector3(0, 0, 0);
				check(wrapper.illuminate(test, o, P, N, O).equals(new ColorRGB(0)));
			}
			finish();

			start("tracing: reflections");
			{
				Scene test = new Scene();
				test.setAmbientLight(new ColorRGB(1));
				test.addObject(new Plane(new Vector3(0, 0, -1), new Vector3(0, 0, 1), new ColorRGB(1)));
				Sphere o = new Sphere(new Vector3(0, 0, 2), 1, new ColorRGB(0, 0, 0));
				test.addObject(o);

				Ray r = new Ray(new Vector3(0), new Vector3(0, 0, 1));
				ColorRGB result = new Renderer(0, 0, 2).trace(test, r, 1);
				check(result.equals(new ColorRGB(o.getReflectivity())));
			}
			finish();
		}
		System.err.println("Part 2 testing complete.\n");
	}

	public void test(boolean testAll) {
		failed = false;
		runTestsPart1();
		if (testAll) {
			runTestsPart2();
			System.err.println("Tested both part 1 and part 2 code.");
		} else {
			System.err.println("Tested only part 1 code.");
		}
		if (failed) {
			System.err.println("FAIL: a test failed");
			System.exit(1);
		} else if (!testAll){
			System.err.println("PASS: Part 1 tests passed!");
		} else {
			System.err.println("PASS: all tests passed!");
		}
	}

	public static void main(String[] args) {
		boolean testAll = false;
		if (args.length > 0) {
			testAll = args[0].equals("--all") || args[0].equals("-a");
		}
		new Tick1Tests().test(testAll);
	}
}