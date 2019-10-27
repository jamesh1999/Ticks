package uk.ac.cam.cl.mlrd.testing;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

//TODO: Replace with your package.
import uk.ac.cam.cl.jhah2.exercises.Exercise7;
import uk.ac.cam.cl.mlrd.exercises.markov_models.DiceRoll;
import uk.ac.cam.cl.mlrd.exercises.markov_models.DiceType;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HiddenMarkovModel;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise7;


public class Exercise7Tester {
	
	static final Path dataDirectory = Paths.get("data/dice_dataset");

	public static void main(String[] args) throws IOException {

		List<Path> sequenceFiles = new ArrayList<Path>();
		try (DirectoryStream<Path> files = Files.newDirectoryStream(dataDirectory)) {
			for (Path item : files) {
				sequenceFiles.add(item);
			}
		} catch (IOException e) {
			throw new IOException("Can't access the dataset", e);
		}

		Collections.shuffle(sequenceFiles, new Random(0));
		int testSize = sequenceFiles.size()/10;
		List<Path> trainingSet = sequenceFiles.subList(testSize*2, sequenceFiles.size());
		
		IExercise7 implementation = (IExercise7) new Exercise7();

		HiddenMarkovModel<DiceRoll, DiceType> model = implementation.estimateHMM(trainingSet);
		System.out.println("Predicted transitions:");
		System.out.println(model.getTransitionMatrix());
		System.out.println();
		System.out.println("Predicted emissions:");
		System.out.println(model.getEmissionMatrix());
		System.out.println();
	}
}