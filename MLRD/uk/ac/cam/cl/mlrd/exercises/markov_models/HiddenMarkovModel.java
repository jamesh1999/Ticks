package uk.ac.cam.cl.mlrd.exercises.markov_models;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class HiddenMarkovModel<T, U> {

	protected Map<U, Map<U, Double>> transitions;
	protected Map<U, Map<T, Double>> emissions;

	/**
	 * Constructs a HiddenMarkovModel object from a transition matrix and
	 * emission matrix. Not all transitions or emissions have to be defined, but
	 * there must be at least one transition and emission from each hidden
	 * states, and probabilities must be positive and total more than zero and
	 * less than {@link Double#MAX_VALUE}. There should be no null values
	 * anywhere in the parameters.
	 * 
	 * @param transitionMatrix
	 *            {@link Map}<{@link U}, {@link Map}<{@link U}, {@link Double}>>
	 *            The transition matrix
	 * @param emissionMatrix
	 *            {@link Map}<{@link U}, {@link Map}<{@link T}, {@link Double}>>
	 *            The emission matrix
	 */
	public HiddenMarkovModel(Map<U, Map<U, Double>> transitionMatrix, Map<U, Map<T, Double>> emissionMatrix) {

		Objects.requireNonNull(transitionMatrix, "transition matrix cannot be null");
		Objects.requireNonNull(emissionMatrix, "emission matrix cannot be null");
		if (transitionMatrix.containsValue(null)) {
			throw new IllegalArgumentException("transition matrix must not contain null values");
		}
		if (emissionMatrix.containsValue(null)) {
			throw new IllegalArgumentException("emission matrix must not contain null values");
		}
		if (transitionMatrix.containsKey(null)) {
			throw new IllegalArgumentException("transition matrix must not contain null keys");
		}
		if (emissionMatrix.containsKey(null)) {
			throw new IllegalArgumentException("emission matrix must not contain null keys");
		}

		transitions = new HashMap<U, Map<U, Double>>();
		emissions = new HashMap<U, Map<T, Double>>();

		Set<U> states = new HashSet<U>();
		states.addAll(transitionMatrix.keySet());
		states.addAll(emissionMatrix.keySet());

		if (states.isEmpty()) {
			throw new IllegalArgumentException("some internal state must exist");
		}
		if (states.size() != transitionMatrix.size()) {
			throw new IllegalArgumentException("transitions must be defined for all hidden states");
		}
		if (states.size() != emissionMatrix.size()) {
			throw new IllegalArgumentException("emissions must be defined for all hidden states");
		}

		for (U state : states) {
			Map<U, Double> stateTransitions = transitionMatrix.get(state);
			if (stateTransitions.containsValue(null)) {
				throw new IllegalArgumentException("transition matrix submatrices must not contain null values");
			}
			if (stateTransitions.containsKey(null)) {
				throw new IllegalArgumentException("transition matrix submatrices must not contain null keys");
			}

			Map<U, Double> transitionProbs = new HashMap<U, Double>();
			for (Entry<U, Double> transitionProb : stateTransitions.entrySet()) {
				double prob = transitionProb.getValue();
				if (prob >= 0.0) {
					transitionProbs.put(transitionProb.getKey(), prob);
				} else {
					throw new IllegalArgumentException("probabilities cannot be negative");
				}
			}
			transitions.put(state, transitionProbs);

			Map<T, Double> stateEmissions = emissionMatrix.get(state);
			if (stateEmissions.containsValue(null)) {
				throw new IllegalArgumentException("emission matrix submatrices must not contain null values");
			}
			if (stateEmissions.containsKey(null)) {
				throw new IllegalArgumentException("emission matrix submatrices must not contain null keys");
			}

			Map<T, Double> emissionProbs = new HashMap<T, Double>();
			for (Entry<T, Double> emissionProb : stateEmissions.entrySet()) {
				double prob = emissionProb.getValue();
				if (prob >= 0.0) {
					emissionProbs.put(emissionProb.getKey(), prob);
				} else {
					throw new IllegalArgumentException("probabilities cannot be negative");
				}
			}
			emissions.put(state, emissionProbs);
		}
	}

	/**
	 * Get a set of all possible hidden states.
	 * 
	 * @return {@link Set}<{@link U}> The hidden states
	 */
	public Set<U> getHiddenStates() {
		return Collections.unmodifiableSet(transitions.keySet());
	}

	/**
	 * Get a set of all transitions from a given state and their
	 * probabilities.
	 * 
	 * @param fromState
	 *            {@link U} The state to get transitions from
	 * @return {@link Map}<{@link U}, {@link Double}> Transitions and
	 *         probabilities
	 */
	public Map<U, Double> getTransitions(U fromState) {
		Objects.requireNonNull(fromState, "state should not be null");
		return Collections.unmodifiableMap(transitions.get(fromState));
	}

	/**
	 * Get the transition matrix (non-modifiable).
	 * 
	 * @return {@link Map}<{@link U}, {@link Map}<{@link U}, {@link Double}>>
	 *         Transition matrix
	 */
	public Map<U, Map<U, Double>> getTransitionMatrix() {
		Map<U, Map<U, Double>> transitionMap = new HashMap<U, Map<U, Double>>();
		for (U state : transitions.keySet()) {
			transitionMap.put(state, getTransitions(state));
		}
		return Collections.unmodifiableMap(transitionMap);
	}

	/**
	 * Get a set of all emissions from a given state and their
	 * probabilities.
	 * 
	 * @param fromState
	 *            {@link U} The state to get emissions from
	 * @return {@link Map}<{@link T}, {@link Double}> All emissions and
	 *         probabilities
	 */
	public Map<T, Double> getEmissions(U fromState) {
		Objects.requireNonNull(fromState, "state should not be null");
		return Collections.unmodifiableMap(emissions.get(fromState));
	}

	/**
	 * Get the emission matrix (non-modifiable).
	 * 
	 * @return {@link Map}<{@link U}, {@link Map}<{@link T}, {@link Double}>>
	 *         Emission matrix
	 */
	public Map<U, Map<T, Double>> getEmissionMatrix() {
		Map<U, Map<T, Double>> emissionMap = new HashMap<U, Map<T, Double>>();
		for (U state : transitions.keySet()) {
			emissionMap.put(state, getEmissions(state));
		}
		return Collections.unmodifiableMap(emissionMap);
	}
}