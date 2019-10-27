package uk.ac.cam.cl.mlrd.exercises.sentiment_detection;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public interface IExercise4 {

	/**
	 * Modify the simple classifier from Exercise1 to include the information about the magnitude of a sentiment.
	 * @param testSet
	 *            {@link Set}<{@link Path}> Paths to reviews to classify
	 * @param lexiconFile
	 *            {@link Path} Path to the lexicon file
	 * @return {@link Map}<{@link Path}, {@link Sentiment}> Map of calculated
	 *         sentiment for each review
	 * @throws IOException 
	 */
	public Map<Path, Sentiment> magnitudeClassifier(Set<Path> testSet, Path lexiconFile) throws IOException;

	/**
	 * Implement the two-sided sign test algorithm to determine if one
	 * classifier is significantly better or worse than another.
	 * The sign for a result should be determined by which
	 * classifier is more correct, or if they are equally correct should be 0.5
	 * positive, 0.5 negative and the ceiling of the least common sign total
	 * should be used to calculate the probability.
	 * 
	 * @param actualSentiments
	 *            {@link Map}<{@link Path}, {@link Sentiment}>
	 * @param classificationA
	 *            {@link Map}<{@link Path}, {@link Sentiment}>
	 * @param classificationB
	 *            {@link Map}<{@link Path}, {@link Sentiment}>
	 * @return <code>double</code>
	 */
	public double signTest(Map<Path, Sentiment> actualSentiments, Map<Path, Sentiment> classificationA,
			Map<Path, Sentiment> classificationB);
}