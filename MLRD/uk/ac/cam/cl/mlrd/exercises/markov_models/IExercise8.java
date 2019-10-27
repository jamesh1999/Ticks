package uk.ac.cam.cl.mlrd.exercises.markov_models;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface IExercise8 {

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
	public List<DiceType> viterbi(HiddenMarkovModel<DiceRoll, DiceType> model, List<DiceRoll> observedSequence);

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
			List<Path> testFiles) throws IOException;

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
	public double precision(Map<List<DiceType>, List<DiceType>> true2PredictedMap);

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
	public double recall(Map<List<DiceType>, List<DiceType>> true2PredictedMap);

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
	public double fOneMeasure(Map<List<DiceType>, List<DiceType>> true2PredictedMap);

}