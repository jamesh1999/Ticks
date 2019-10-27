package uk.ac.cam.cl.mlrd.testing;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: Replace with your package.
import uk.ac.cam.cl.jhah2.exercises.Exercise6;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.DataPreparation6;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise6;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.NuancedSentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.utils.DataSplit3Way;

public class Exercise6Tester {

	static final Path dataDirectory = Paths.get("data/nuanced_sentiment_dataset");

	public static void main(String[] args) throws IOException {

		Path sentimentFile = dataDirectory.resolve("review_sentiment");
		Map<Path, NuancedSentiment> dataSet = DataPreparation6.loadNuancedDataset(dataDirectory.resolve("reviews"),
				sentimentFile);
		DataSplit3Way<NuancedSentiment> split = new DataSplit3Way<NuancedSentiment>(dataSet, 0);

		IExercise6 implementation = (IExercise6) new Exercise6();

		Map<NuancedSentiment, Double> classProbabilities = implementation.calculateClassProbabilities(split.trainingSet);
		Map<String, Map<NuancedSentiment, Double>> logProbs = implementation.calculateNuancedLogProbs(split.trainingSet);
		Map<Path, NuancedSentiment> predictions = implementation.nuancedClassifier(split.validationSet.keySet(), logProbs, classProbabilities);
		System.out.println("Multiclass predictions:");
		System.out.println(predictions);
		System.out.println();

		double accuracy = implementation.nuancedAccuracy(split.validationSet, predictions);
		System.out.println("Multiclass prediction accuracy:");
		System.out.println(accuracy);
		System.out.println();

		
		Path classPredictionsFile = Paths.get("data/class_predictions.csv");
		List<Map<Integer, Sentiment>> classPredictions = DataPreparation6.loadClassPredictions(classPredictionsFile);
		
		Map<Integer, Map<Sentiment, Integer>> agreementTable = implementation.agreementTable(classPredictions);
		System.out.println("Agreement table:");
		System.out.println(agreementTable);
		System.out.println();

		double kappaAll = implementation.kappa(agreementTable);
		System.out.println("Overall kappa value:");
		System.out.println(kappaAll);
		System.out.println();

		Map<Integer, Map<Sentiment, Integer>> table12 = new HashMap<Integer, Map<Sentiment, Integer>>();
		table12.put(1, agreementTable.get(1));
		table12.put(2, agreementTable.get(2));
		double kappa12 = implementation.kappa(table12);
		System.out.println("Kappa value for reviews 1 and 2:");
		System.out.println(kappa12);
		System.out.println();

		Map<Integer, Map<Sentiment, Integer>> table34 = new HashMap<Integer, Map<Sentiment, Integer>>();
		table34.put(3, agreementTable.get(3));
		table34.put(4, agreementTable.get(4));
		double kappa34 = implementation.kappa(table34);
		System.out.println("Kappa value for reviews 3 and 4:");
		System.out.println(kappa34);
		System.out.println();
	}
}