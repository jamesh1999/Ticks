package uk.ac.cam.cl.mlrd.exercises.markov_models;

import java.util.HashMap;
import java.util.Map;

public enum DiceRoll {
	START('S'), ONE('1'), TWO('2'), THREE('3'), FOUR('4'), FIVE('5'), SIX('6'), END('E');

	private char name;

	private static Map<Character, DiceRoll> numbers = new HashMap<>();
	static {
		for (DiceRoll feature : DiceRoll.values()) {
			numbers.put(feature.name, feature);
		}
	}

	private DiceRoll(char name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.valueOf(name);
	}

	/**
	 * Get the die roll corresponding to the given character.
	 * 
	 * @param name
	 *            <code>char</code> The char name of the die roll
	 * @return {@link Feature} The corresponding die roll
	 */
	public static DiceRoll valueOf(char name) {
		return numbers.get(name);
	}
}