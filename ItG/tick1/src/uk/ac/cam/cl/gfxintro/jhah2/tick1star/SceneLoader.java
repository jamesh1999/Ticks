package uk.ac.cam.cl.gfxintro.jhah2.tick1star;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SceneLoader {
	// Loads our scene from an XML file
	
	private Scene scene;

	public SceneLoader(String filename) {
		scene = new Scene();

		Element document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(filename))
					.getDocumentElement();
		} catch (ParserConfigurationException e) {
			assert false;
		} catch (IOException e) {
			throw new RuntimeException("error reading file:\n" + e.getMessage());
		} catch (SAXException e) {
			throw new RuntimeException("error loading XML.");
		}

		if (document.getNodeName() != "scene")
			throw new RuntimeException("scene file does not contain a scene element");

		NodeList elements = document.getElementsByTagName("*");
		for (int i = 0; i < elements.getLength(); ++i) {
			Element element = (Element) elements.item(i);
			switch (element.getNodeName()) {

			case "sphere":
				Sphere sphere = new Sphere(getPosition(element), getDouble(element, "radius", 1), getColour(element));
				scene.addObject(sphere);
				break;
				
			case "bumpy-sphere":
				BumpySphere bumpySphere = new BumpySphere(getPosition(element), getDouble(element, "radius", 1), getColour(element), getString(element, "bump-map"));
				scene.addObject(bumpySphere);
				break;

			case "plane":
				Plane plane = new Plane(getPosition(element), getNormal(element), getColour(element));
				scene.addObject(plane);
				break;

			case "point-light":
				PointLight light = new PointLight(getPosition(element), getColour(element),
						getDouble(element, "intensity", 100));
				scene.addPointLight(light);
				break;

			case "ambient-light":
				scene.setAmbientLight(getColour(element));
				break;

			default:
				throw new RuntimeException("unknown object tag: " + element.getNodeName());
			}
		}
	}

	public Scene getScene() {
		return scene;
	}

	private Vector3 getPosition(Element tag) {
		double x = getDouble(tag, "x", 0);
		double y = getDouble(tag, "y", 0);
		double z = getDouble(tag, "z", 0);
		return new Vector3(x, y, z);
	}

	private Vector3 getNormal(Element tag) {
		double x = getDouble(tag, "nx", 0);
		double y = getDouble(tag, "ny", 0);
		double z = getDouble(tag, "nz", 0);
		return new Vector3(x, y, z).normalised();
	}

	private ColorRGB getColour(Element tag) {

		String hexString = tag.getAttribute("colour");
		double red = Integer.parseInt(hexString.substring(1, 3), 16) / 255.0;
		double green = Integer.parseInt(hexString.substring(3, 5), 16) / 255.0;
		double blue = Integer.parseInt(hexString.substring(5, 7), 16) / 255.0;

		return new ColorRGB(red, green, blue);
	}

	private double getDouble(Element tag, String attribute, double fallback) {
		try {
			return Double.parseDouble(tag.getAttribute(attribute));
		} catch (NumberFormatException e) {
			return fallback;
		}
	}
	
	private String getString(Element tag, String attribute){
		return tag.getAttribute(attribute);
	}

}
