package uk.ac.cam.cl.mlrd.testing;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

//TODO: Replace with your package.
import uk.ac.cam.cl.jhah2.exercises.Exercise1;
import uk.ac.cam.cl.jhah2.exercises.Exercise2;
import uk.ac.cam.cl.jhah2.exercises.Exercise4;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.DataPreparation1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise4;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.utils.DataSplit;

public class Exercise4Tester {
	static final Path dataDirectory = Paths.get("data/sentiment_dataset");

	public static void main(String[] args) throws Exception {

		Path sentimentFile = dataDirectory.resolve("review_sentiment");
		// Loading the dataset.
		Map<Path, Sentiment> dataSet = DataPreparation1.loadSentimentDataset(dataDirectory.resolve("reviews"), sentimentFile);
		DataSplit<Sentiment> split = new DataSplit<Sentiment>(dataSet, 0);

		IExercise4 implementation = (IExercise4) new Exercise4();

		Path lexiconFile = Paths.get("data/sentiment_lexicon");
		Map<Path, Sentiment> magnitudePredictions = implementation.magnitudeClassifier(split.validationSet.keySet(), lexiconFile);

		IExercise1 implementation1 = (IExercise1) new Exercise1();
		Map<Path, Sentiment> simplePredictions = implementation1.simpleClassifier(split.validationSet.keySet(), lexiconFile);

		double magnitudeAccuracy = implementation1.calculateAccuracy(split.validationSet, magnitudePredictions);
		System.out.println("Magnitude classifier accuracy:");
		System.out.println(magnitudeAccuracy);
		System.out.println();

		double simpleAccuracy = implementation1.calculateAccuracy(split.validationSet, simplePredictions);
		System.out.println("Simple classifier accuracy:");
		System.out.println(simpleAccuracy);
		System.out.println();

		double signResultMagSimple = implementation.signTest(split.validationSet, magnitudePredictions, simplePredictions);

		IExercise2 implementation2 = (IExercise2) new Exercise2();

		Map<String, Map<Sentiment, Double>> logProbs = implementation2.calculateSmoothedLogProbs(split.trainingSet);
		Map<Sentiment, Double> classProbs = implementation2.calculateClassProbabilities(split.trainingSet);
		Map<Path, Sentiment> nbPredictions = implementation2.naiveBayes(split.validationSet.keySet(), logProbs,classProbs);

		double nbAccuracy = implementation1.calculateAccuracy(split.validationSet, nbPredictions);
		System.out.println("Naive Bayes classifier accuracy:");
		System.out.println(nbAccuracy);
		System.out.println();

		double signResultNBMag = implementation.signTest(split.validationSet, nbPredictions, magnitudePredictions);
		System.out.println("Sign test results:");
		System.out.println("NB vs Magnitude:");
		System.out.println(signResultNBMag);
		System.out.println();
		System.out.println("Magnitude vs simple:");
		System.out.println(signResultMagSimple);
		System.out.println();
	}
}