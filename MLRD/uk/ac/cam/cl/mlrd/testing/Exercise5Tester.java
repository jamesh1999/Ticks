package uk.ac.cam.cl.mlrd.testing;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

//TODO: Replace with your package.
import uk.ac.cam.cl.jhah2.exercises.Exercise1;
import uk.ac.cam.cl.jhah2.exercises.Exercise2;
import uk.ac.cam.cl.jhah2.exercises.Exercise4;
import uk.ac.cam.cl.jhah2.exercises.Exercise5;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.DataPreparation1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise4;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise5;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;

public class Exercise5Tester {

	static final Path dataDirectory = Paths.get("data/sentiment_dataset");
	static final Path testDirectory = Paths.get("data/sentiment_test_set");
	static final Path newDataDirectory = Paths.get("data/year_2016_dataset");
	static final int seed = 0;

	public static void main(String[] args) throws IOException {
		// Read in the answer key.
		Path sentimentFile = dataDirectory.resolve("review_sentiment");
		// Get the data set.
		Map<Path, Sentiment> dataSet = DataPreparation1.loadSentimentDataset(dataDirectory.resolve("reviews"),
				sentimentFile);

		IExercise5 implementation = (IExercise5) new Exercise5();

		List<Map<Path, Sentiment>> randomFolds = implementation.splitCVRandom(dataSet, seed);
		double[] randomScores = implementation.crossValidate(randomFolds);
		double randomScore = implementation.cvAccuracy(randomScores);
		double randomVar= implementation.cvVariance(randomScores);
		System.out.println("CV score for folds split randomly: ");
		System.out.println("Average:" + randomScore);
		System.out.println("Variance:" + randomVar);

		List<Map<Path, Sentiment>> randomStratFolds = implementation.splitCVStratifiedRandom(dataSet, seed);
		double[] stratScores = implementation.crossValidate(randomStratFolds);
		double stratScore = implementation.cvAccuracy(stratScores);
		double stratVar= implementation.cvVariance(stratScores);
		System.out.println("CV score for stratified random folds: ");
		System.out.println("Average:" + stratScore);
		System.out.println("Variance:" + stratVar);

		Path oldSentFile = testDirectory.resolve("test_sentiment");
		Map<Path, Sentiment> testSet = DataPreparation1.loadSentimentDataset(testDirectory.resolve("reviews"),
				oldSentFile);
		Path newSentFile = newDataDirectory.resolve("review_sentiment");
		Map<Path, Sentiment> newTestSet = DataPreparation1.loadSentimentDataset(newDataDirectory.resolve("reviews"),
				newSentFile);

		IExercise2 implementation2 = new Exercise2();
		Map<String, Map<Sentiment, Double>> probs = implementation2.calculateSmoothedLogProbs(dataSet);
		Map<Sentiment, Double> classProbs = implementation2.calculateClassProbabilities(dataSet);
		Map<Path, Sentiment> testPredictions = implementation2.naiveBayes(testSet.keySet(), probs, classProbs);
		Map<Path, Sentiment> newPredictions = implementation2.naiveBayes(newTestSet.keySet(), probs, classProbs);

		IExercise1 impl1 = new Exercise1();
		double oldAccuracy = impl1.calculateAccuracy(testSet, testPredictions);
		double newAccuracy = impl1.calculateAccuracy(newTestSet, newPredictions);

		System.out.println("Accuracy on the original test set:");
		System.out.println(oldAccuracy);
		System.out.println();

		System.out.println("Accuracy on the 2016 test set:");
		System.out.println(newAccuracy);
		System.out.println();

		Path lexiconFile = Paths.get("data/sentiment_lexicon");
		Map<Path, Sentiment> a = impl1.simpleClassifier(testSet.keySet(), lexiconFile);
		Map<Path, Sentiment> b = impl1.simpleClassifier(newTestSet.keySet(), lexiconFile);

		System.out.println("Accuracy on the original test set:");
		double aA = impl1.calculateAccuracy(testSet, a);
		System.out.println(aA);
		System.out.println();

		System.out.println("Accuracy on the 2016 test set:");
		double bA = impl1.calculateAccuracy(newTestSet, b);
		System.out.println(bA);
		System.out.println();

		IExercise4 impl4 = (IExercise4) new Exercise4();
		double diffA = impl4.signTest(testSet, testPredictions, a);
		double diffB = impl4.signTest(newTestSet, newPredictions, b);
		System.out.println(diffA);
		System.out.println(diffB);
	}
}