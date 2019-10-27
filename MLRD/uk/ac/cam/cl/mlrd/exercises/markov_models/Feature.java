package uk.ac.cam.cl.mlrd.exercises.markov_models;

import java.util.HashMap;
import java.util.Map;

public enum Feature {
	START('S'), INSIDE('i'), OUTSIDE('o'), MEMBRANE('M'), END('E');

	private char name;

	private static Map<Character, Feature> features = new HashMap<>();
	static {
		for (Feature feature : Feature.values()) {
			features.put(feature.name, feature);
		}
	}

	private Feature(char name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.valueOf(name);
	}

	/**
	 * Get the feature corresponding to the given character.
	 * 
	 * @param name
	 *            <code>char</code> The char name of the feature
	 * @return {@link Feature} The corresponding feature
	 */
	public static Feature valueOf(char name) {
		return features.get(name);
	}
}