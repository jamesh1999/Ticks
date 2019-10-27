package uk.ac.cam.cl.mlrd.exercises.sentiment_detection;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface IExercise5 {

	/**
	 * Split the given data randomly into 10 folds.
	 * 
	 * @param dataSet
	 *            {@link Map}<{@link Path}, {@link Sentiment}> All review paths
	 * 
	 * @param seed
	 *            A seed for the random shuffling.
	 * @return {@link List}<{@link Map}<{@link Path}, {@link Sentiment}>> A set
	 *         of folds with even numbers of each sentiment
	 */
	public List<Map<Path, Sentiment>> splitCVRandom(Map<Path, Sentiment> dataSet, int seed);

	/**
	 * Split the given data randomly into 10 folds but so that class proportions
	 * are preserved in each fold.
	 * 
	 * @param dataSet
	 *            {@link Map}<{@link Path}, {@link Sentiment}> All review paths
	 * @param seed
	 *            A seed for the random shuffling.
	 * @return {@link List}<{@link Map}<{@link Path}, {@link Sentiment}>> A set
	 *         of folds with even numbers of each sentiment
	 */
	public List<Map<Path, Sentiment>> splitCVStratifiedRandom(Map<Path, Sentiment> dataSet, int seed);

	/**
	 * Run cross-validation on the dataset according to the folds.
	 * 
	 * @param folds
	 *            {@link List}<{@link Map}<{@link Path}, {@link Sentiment}>> A
	 *            set of folds.
	 * @return Scores for individual cross-validation runs.
	 * @throws IOException
	 */
	public double[] crossValidate(List<Map<Path, Sentiment>> folds) throws IOException;

	/**
	 * Calculate the average of the scores.
	 * 
	 * @param scores
	 *            A double array with results of individual cross-validation
	 *            runs.
	 * @return The average cross-validation score.
	 */
	public double cvAccuracy(double[] scores);

	/**
	 * Calculate the variance of the scores.
	 * 
	 * @param scores
	 *            A double array with results of individual cross-validation
	 *            runs.
	 * @return The variance of cross-validation scores.
	 */
	public double cvVariance(double[] scores);
}