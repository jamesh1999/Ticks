package uk.ac.cam.cl.mlrd.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//TODO: Replace with your package.
import uk.ac.cam.cl.jhah2.exercises.Exercise9;
import uk.ac.cam.cl.mlrd.exercises.markov_models.AminoAcid;
import uk.ac.cam.cl.mlrd.exercises.markov_models.Feature;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HMMDataStore;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HiddenMarkovModel;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise9;

public class Exercise9Tester {

	static final Path dataFile = Paths.get("data/bio_dataset.txt");

	public static void main(String[] args) throws IOException {

		List<HMMDataStore<AminoAcid, Feature>> sequencePairs = HMMDataStore.loadBioFile(dataFile);

		// Use for testing the code
		/*Collections.shuffle(sequencePairs, new Random(0));
		int testSize = sequencePairs.size() / 10;
		List<HMMDataStore<AminoAcid, Feature>> devSet = sequencePairs.subList(0, testSize);
		List<HMMDataStore<AminoAcid, Feature>> testSet = sequencePairs.subList(testSize, 2 * testSize);
		List<HMMDataStore<AminoAcid, Feature>> trainingSet = sequencePairs.subList(testSize * 2, sequencePairs.size());*/
		// But:
		// TODO: Replace with cross-validation for the tick.

		IExercise9 implementation = (IExercise9) new Exercise9();

		Collections.shuffle(sequencePairs, new Random(0));
		int nFolds = 10;
		List<List<HMMDataStore<AminoAcid, Feature>>> folds = new ArrayList<>();
		for (int i = 0; i < nFolds; ++i)
			folds.add(new ArrayList<>());
		for (int i = 0; i < sequencePairs.size(); ++i)
			folds.get(i * 10 / sequencePairs.size()).add(sequencePairs.get(i));

		List<HMMDataStore<AminoAcid, Feature>> trainingSet = new ArrayList<>();
		List<HMMDataStore<AminoAcid, Feature>> testSet = new ArrayList<>();

		double accuracy = 0.0;
		double recall = 0.0;
		double f1Score = 0.0;

		for (int i = 0; i < nFolds; ++i)
		{
			trainingSet.clear();
			testSet.clear();

			// Populate sets
			for (int j = 0; j < nFolds; ++j)
			{
				List<HMMDataStore<AminoAcid, Feature>> dest = null;
				if (i == j) // Test fold
					dest = testSet;
				else
					dest = trainingSet;

				for (HMMDataStore<AminoAcid, Feature> file : folds.get(j))
					dest.add(file);
			}

			HiddenMarkovModel<AminoAcid, Feature> model = implementation.estimateHMM(trainingSet);
			Map<List<Feature>, List<Feature>> true2PredictedSequences = implementation.predictAll(model, testSet);

			accuracy += implementation.precision(true2PredictedSequences);
			recall += implementation.recall(true2PredictedSequences);
			f1Score += implementation.fOneMeasure(true2PredictedSequences);
		}
		
		System.out.println("Prediction precision:");
		System.out.println(accuracy / nFolds);
		System.out.println();

		System.out.println("Prediction recall:");
		System.out.println(recall / nFolds);
		System.out.println();

		System.out.println("Prediction F1 score:");
		System.out.println(f1Score / nFolds);
		System.out.println();
		

		/*HiddenMarkovModel<AminoAcid, Feature> model = implementation.estimateHMM(trainingSet);
		System.out.println("Predicted transitions:");
		System.out.println(model.getTransitionMatrix());
		System.out.println();
		System.out.println("Predicted emissions:");
		System.out.println(model.getEmissionMatrix());
		System.out.println();

		HMMDataStore<AminoAcid, Feature> data = devSet.get(0);
		List<Feature> predicted = implementation.viterbi(model, data.observedSequence);
		System.out.println("True hidden sequence:");
		System.out.println(data.hiddenSequence);
		System.out.println();

		System.out.println("Predicted hidden sequence:");
		System.out.println(predicted);
		System.out.println();

		Map<List<Feature>, List<Feature>> true2PredictedSequences = implementation.predictAll(model, devSet);
		double accuracy = implementation.precision(true2PredictedSequences);
		System.out.println("Prediction precision:");
		System.out.println(accuracy);
		System.out.println();

		double recall = implementation.recall(true2PredictedSequences);
		System.out.println("Prediction recall:");
		System.out.println(recall);
		System.out.println();

		double f1Score = implementation.fOneMeasure(true2PredictedSequences);
		System.out.println("Prediction F1 score:");
		System.out.println(f1Score);
		System.out.println();*/
	}
}