package uk.ac.cam.cl.mlrd.exercises.sentiment_detection;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface IExercise6 {

	/**
	 * Calculate the probability of a document belonging to a given class based
	 * on the training data.
	 * 
	 * @param trainingSet
	 *            {@link Map}<{@link Path}, {@link NuancedSentiment}> Training review
	 *            paths
	 * @return {@link Map}<{@link NuancedSentiment}, {@link Double}> Class
	 *         probabilities.
	 * @throws IOException
	 */
	public Map<NuancedSentiment, Double> calculateClassProbabilities(Map<Path, NuancedSentiment> trainingSet) throws IOException;
	
	/**
	 * Modify your smoothed Naive Bayes to calculate log probabilities for three classes.
	 * 
	 * @param trainingSet
	 *            {@link Map}<{@link Path}, {@link NuancedSentiment}> Training review
	 *            paths
	 * @return {@link Map}<{@link String}, {@link Map}<{@link NuancedSentiment},
	 *         {@link Double}>> Estimated log probabilities
	 * @throws IOException
	 */
	public Map<String, Map<NuancedSentiment, Double>> calculateNuancedLogProbs(Map<Path, NuancedSentiment> trainingSet)
			throws IOException;
	
	/**
	 * Modify your Naive Bayes classifier so that it can classify reviews which
	 * may also have neutral sentiment.
	 * 
	 * @param testSet
	 *            {@link Set}<{@link Path}> Test review paths
	 * @param tokenLogProbs
	 *            {@link Map}<{@link String}, {@link Map}<{@link NuancedSentiment}, {@link Double}> tokenLogProbs
	 * @param classProbabilities
	 * 			{@link Map}<{@link NuancedSentiment}, {@link Double}> classProbabilities
	 * @return {@link Map}<{@link Path}, {@link NuancedSentiment}> Predicted sentiments
	 * @throws IOException 
	 */
	public 	Map<Path, NuancedSentiment> nuancedClassifier(Set<Path> testSet,
			Map<String, Map<NuancedSentiment, Double>> tokenLogProbs, Map<NuancedSentiment, Double> classProbabilities)
			throws IOException;
	/**
	 * Calculate the proportion of predicted sentiments that were correct.
	 * 
	 * @param trueSentiments
	 *            {@link Map}<{@link Path}, {@link NuancedSentiment}> Map of
	 *            correct sentiment for each review
	 * @param predictedSentiments
	 *            {@link Map}<{@link Path}, {@link NuancedSentiment}> Map of
	 *            calculated sentiment for each review
	 * @return <code>double</code> The overall accuracy of the predictions
	 */
	public double nuancedAccuracy(Map<Path, NuancedSentiment> trueSentiments,
			Map<Path, NuancedSentiment> predictedSentiments);

	/**
	 * Given some predictions about the sentiment in reviews, generate an
	 * agreement table which for each review contains the number of predictions
	 * that predicted each sentiment.
	 * 
	 * @param predictedSentiments
	 *            {@link Collection}<{@link Map}<{@link Integer},
	 *            {@link Sentiment}>> Different predictions for the
	 *            sentiment in each of a set of reviews 1, 2, 3, 4.
	 * @return {@link Map}<{@link Integer}, {@link Map}<{@link Sentiment},
	 *         {@link Integer}>> For each review, the number of predictions that
	 *         predicted each sentiment
	 */
	public Map<Integer, Map<Sentiment, Integer>> agreementTable(Collection<Map<Integer, Sentiment>> predictedSentiments);

	/**
	 * Using your agreement table, calculate the kappa value for how much
	 * agreement there was; 1 should mean total agreement and -1 should mean total disagreement.
	 * 
	 * @param agreementTable
	 *            {@link Map}<{@link Integer}, {@link Map}<{@link Sentiment},
	 *            {@link Integer}>> For each review (1, 2, 3, 4) the number of predictions
	 *            that predicted each sentiment
	 * @return <code>double</code> The kappa value, between -1 and 1
	 */
	public double kappa(Map<Integer, Map<Sentiment, Integer>> agreementTable);
}