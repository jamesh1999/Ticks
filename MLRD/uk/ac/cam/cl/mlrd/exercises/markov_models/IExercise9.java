package uk.ac.cam.cl.mlrd.exercises.markov_models;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IExercise9 {

	/**
	 * Loads the sequences of visible and hidden states from the sequence files
	 * (visible amino acids on first line and hidden features on second) and
	 * uses them to estimate the parameters of the Hidden Markov Model that
	 * generated them.
	 * 
	 * @param bioDataFiles
	 *            {@link Collection}<{@link Path}> The files containing amino
	 *            acid sequences
	 * @return {@link HiddenMarkovModel}<{@link AminoAcid}, {@link Feature}> The
	 *         estimated model
	 * @throws IOException
	 */
	public HiddenMarkovModel<AminoAcid, Feature> estimateHMM(List<HMMDataStore<AminoAcid, Feature>> sequencePairs)
			throws IOException;

	/**
	 * Uses the Viterbi algorithm to calculate the most likely single sequence
	 * of hidden states given the observed sequence.
	 * 
	 * @param model
	 *            A pre-trained HMM.
	 * @param observedSequence
	 *            {@link List}<{@link AminoAcid}> A sequence of observed amino
	 *            acids
	 * @return {@link List}<{@link Feature}> The most likely single sequence of
	 *         hidden states
	 */
	public List<Feature> viterbi(HiddenMarkovModel<AminoAcid, Feature> model, List<AminoAcid> observedSequence);

	/**
	 * Uses the Viterbi algorithm to predict hidden sequences of all observed
	 * sequences in testSequencePairs.
	 * 
	 * @param model
	 *            The HMM model.
	 * @param testSequencePair
	 *            A list of {@link HMMDataStore}s with observed and true hidden
	 *            sequences.
	 * @return {@link Map}<{@link List}<{@link Feature}>,
	 *         {@link Feature}<{@link Feature}>> A map from a real hidden
	 *         sequence to the equivalent estimated hidden sequence.
	 * @throws IOException
	 */
	Map<List<Feature>, List<Feature>> predictAll(HiddenMarkovModel<AminoAcid, Feature> model,
			List<HMMDataStore<AminoAcid, Feature>> testSequencePairs) throws IOException;

	/**
	 * Calculates the precision of the estimated sequence with respect to the
	 * membrane state, i.e. the proportion of predicted membrane states that
	 * were actually in the membrane.
	 * 
	 * @param true2PredictedMap
	 *            {@link Map}<{@link List}<{@link Feature}>,
	 *            {@link List}<{@link Feature}>> A map from a real hidden
	 *            sequence to the equivalent estimated hidden sequence.
	 * @return <code>double</code> The precision of the estimated sequence with
	 *         respect to the membrane state averaged over all the test
	 *         sequences.
	 */
	double precision(Map<List<Feature>, List<Feature>> true2PredictedMap);

	/**
	 * Calculate the recall for the membrane state.
	 * 
	 * @param true2PredictedMap
	 *            {@link Map}<{@link List}<{@link Feature}>,
	 *            {@link List}<{@link Feature}>> A map from a real hidden
	 *            sequence to the equivalent estimated hidden sequence.
	 * @return The recall for the membrane state.
	 */
	double recall(Map<List<Feature>, List<Feature>> true2PredictedMap);

	/**
	 * Calculate the F1 score for the membrane state.
	 * 
	 * @param true2PredictedMap
	 */
	double fOneMeasure(Map<List<Feature>, List<Feature>> true2PredictedMap);

}