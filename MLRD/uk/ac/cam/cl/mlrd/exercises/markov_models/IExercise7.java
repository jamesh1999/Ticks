package uk.ac.cam.cl.mlrd.exercises.markov_models;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public interface IExercise7 {

	/**
	 * Loads the sequences of visible and hidden states from the sequence files
	 * (visible dice rolls on first line and hidden dice types on second) and uses
	 * them to estimate the parameters of the Hidden Markov Model that generated
	 * them.
	 * 
	 * @param sequenceFiles
	 *            {@link Collection}<{@link Path}> The files containing dice roll
	 *            sequences
	 * @return {@link HiddenMarkovModel}<{@link DiceRoll}, {@link DiceType}> The
	 *         estimated model
	 * @throws IOException 
	 */
	public HiddenMarkovModel<DiceRoll, DiceType> estimateHMM(Collection<Path> sequenceFiles) throws IOException;
}