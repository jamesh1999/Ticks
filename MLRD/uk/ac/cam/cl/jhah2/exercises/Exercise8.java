package uk.ac.cam.cl.jhah2.exercises;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

public class Exercise8 implements IExercise8 {

	/**
	 * Uses the Viterbi algorithm to calculate the most likely single sequence
	 * of hidden states given the observed sequence and a model.
	 * 
	 * @param model
	 *            {@link HiddenMarkovModel}<{@link DiceRoll}, {@link DiceType}>
	 *            A sequence model.
	 * @param observedSequence
	 *            {@link List}<{@link DiceRoll}> A sequence of observed die
	 *            rolls
	 * @return {@link List}<{@link DiceType}> The most likely single sequence of
	 *         hidden states
	 */
	public List<DiceType> viterbi(HiddenMarkovModel<DiceRoll, DiceType> model, List<DiceRoll> observedSequence)
	{
		List<Map<DiceType, DiceType>> previousStates = new ArrayList<>();
		List<Map<DiceType, Double>> probabilities = new ArrayList<>();

		Map<DiceType, Double> startState = new HashMap<>();
		startState.put(DiceType.START, 0.0);
		probabilities.add(startState);

		for (int i = 1; i < observedSequence.size(); ++i)
		{
			DiceRoll observation = observedSequence.get(i);

			Map<DiceType, DiceType> psi = new HashMap<>();
			Map<DiceType, Double> delta = new HashMap<>();

			for (DiceType die : model.getHiddenStates())
				delta.put(die, -9999999999999999.0);

			for (DiceType dieA : probabilities.get(i-1).keySet())
			{
				double pPrevious = probabilities.get(i-1).get(dieA);
				Map<DiceType, Double> transitions = model.getTransitions(dieA);
				for (DiceType dieB : transitions.keySet())
				{
					double prob = pPrevious + Math.log(transitions.get(dieB)) + Math.log(model.getEmissions(dieB).get(observation));
					if (prob > delta.get(dieB))
					{
						delta.put(dieB, prob);
						psi.put(dieB, dieA);
					}
				}
			}

			probabilities.add(delta);
			previousStates.add(psi);
		}

		// Backtrack
		List<DiceType> ret = new ArrayList<>();
		DiceType current = DiceType.END;
		ret.add(current);
		for (int i = observedSequence.size() - 2; i >= 0; --i)
		{
			current = previousStates.get(i).get(current);
			ret.add(0, current);
		}

		return ret;
	}

	/**
	 * Uses the Viterbi algorithm to predict hidden sequences of all observed
	 * sequences in testFiles.
	 * 
	 * @param model
	 *            The HMM model.
	 * @param testFiles
	 *            A list of files with observed and true hidden sequences.
	 * @return {@link Map}<{@link List}<{@link DiceType}>,
	 *         {@link List}<{@link DiceType}>> A map from a real hidden sequence
	 *         to the equivalent estimated hidden sequence.
	 * @throws IOException
	 */
	public Map<List<DiceType>, List<DiceType>> predictAll(HiddenMarkovModel<DiceRoll, DiceType> model,
			List<Path> testFiles) throws IOException
	{
		List<HMMDataStore<DiceRoll, DiceType>> data = HMMDataStore.loadDiceFiles(testFiles);

		Map<List<DiceType>, List<DiceType>> ret = new HashMap<>();
		for (HMMDataStore<DiceRoll, DiceType> file : data)
		{
			ret.put(file.hiddenSequence, viterbi(model, file.observedSequence));
		}
		
		return ret;
	}

	/**
	 * Calculates the precision of the estimated sequence with respect to the
	 * weighted state, i.e. the proportion of predicted weighted states that
	 * were actually weighted.
	 * 
	 * @param true2PredictedMap
	 *            {@link Map}<{@link List}<{@link DiceType}>,
	 *            {@link List}<{@link DiceType}>> A map from a real hidden
	 *            sequence to the equivalent estimated hidden sequence.
	 * @return <code>double</code> The precision of the estimated sequence with
	 *         respect to the weighted state averaged over all the test
	 *         sequences.
	 */
	public double precision(Map<List<DiceType>, List<DiceType>> true2PredictedMap)
	{
		long t = 0;
		long loaded = 0;

		for (List<DiceType> dice : true2PredictedMap.keySet())
		{
			for (int i = 0; i < dice.size(); ++i)
			{
				if (true2PredictedMap.get(dice).get(i) != DiceType.WEIGHTED) continue;
				loaded++;
				if (dice.get(i) != DiceType.WEIGHTED) continue;
				t++;
			}
		}

		return (double) t / loaded;
	}

	/**
	 * Calculates the recall of the estimated sequence with respect to the
	 * weighted state, i.e. the proportion of actual weighted states that were
	 * predicted weighted.
	 * 
	 * @param true2PredictedMap
	 *            {@link Map}<{@link List}<{@link DiceType}>,
	 *            {@link List}<{@link DiceType}>> A map from a real hidden
	 *            sequence to the equivalent estimated hidden sequence.
	 * @return <code>double</code> The recall of the estimated sequence with
	 *         respect to the weighted state averaged over all the test
	 *         sequences.
	 */
	public double recall(Map<List<DiceType>, List<DiceType>> true2PredictedMap)
	{
		long t = 0;
		long loaded = 0;

		for (List<DiceType> dice : true2PredictedMap.keySet())
		{
			for (int i = 0; i < dice.size(); ++i)
			{
				if (dice.get(i) != DiceType.WEIGHTED) continue;
				loaded++;
				if (true2PredictedMap.get(dice).get(i) != DiceType.WEIGHTED) continue;
				t++;
			}
		}

		return (double) t / loaded;
	}

	/**
	 * Calculates the F1 measure of the estimated sequence with respect to the
	 * weighted state, i.e. the harmonic mean of precision and recall.
	 * 
	 * @param true2PredictedMap
	 *            {@link Map}<{@link List}<{@link DiceType}>,
	 *            {@link List}<{@link DiceType}>> A map from a real hidden
	 *            sequence to the equivalent estimated hidden sequence.
	 * @return <code>double</code> The F1 measure of the estimated sequence with
	 *         respect to the weighted state averaged over all the test
	 *         sequences.
	 */
	public double fOneMeasure(Map<List<DiceType>, List<DiceType>> true2PredictedMap)
	{
		double prec = precision(true2PredictedMap);
		double rec = recall(true2PredictedMap);
		return (2 * prec * rec) / (prec + rec);
	}

}