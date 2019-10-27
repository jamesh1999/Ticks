package uk.ac.cam.cl.mlrd.exercises.sentiment_detection;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DataPreparation6 {

	/**
	 * Loads in a set of reviews and matches them with their sentiments.
	 * 
	 * @param reviewsDirectory
	 *            {@link Path} to the directory containing the nuanced review
	 *            files.
	 * @param trueFile
	 *            {@link Path} to the file with correct file-sentiment nuanced
	 *            pairings.
	 * @return A map from a review {@link Path} to its {@link NuancedSentiment}.
	 * @throws IOException
	 */
	public static Map<Path, NuancedSentiment> loadNuancedDataset(Path reviewsDirectory, Path trueFile) throws IOException {
		Map<String, String> trueLabels = DataPreparation1.loadTrueLabels(trueFile);
		Map<Path, NuancedSentiment> dataSet = new HashMap<Path, NuancedSentiment>();
		try (DirectoryStream<Path> files = Files.newDirectoryStream(reviewsDirectory)) {
			for (Path item : files) {
				if (trueLabels.get(item.getFileName().toString()).equals("POS")) {
					dataSet.put(item, NuancedSentiment.POSITIVE);
				} else if (trueLabels.get(item.getFileName().toString()).equals("NEG")) {
					dataSet.put(item, NuancedSentiment.NEGATIVE);
				} else {
					dataSet.put(item, NuancedSentiment.NEUTRAL);
				}
			}
		} catch (IOException e) {
			throw new IOException("Can't read the reviews.", e);
		}
		return dataSet;
	}

	/**
	 * Loads the class predictions for the reviews from Task 1.
	 * 
	 * @param classPredictionsFile
	 *            A csv file with the class predictions.
	 * @return A {@link List} of predictions by individual student: {@link Map}
	 *         from {@Integer} review id-> {@link Sentiment}.
	 * @throws IOException
	 */
	public static List<Map<Integer, Sentiment>> loadClassPredictions(Path classPredictionsFile) throws IOException {
		List<Map<Integer, Sentiment>> classPredictions = new ArrayList<Map<Integer, Sentiment>>();
		try (BufferedReader reader = Files.newBufferedReader(classPredictionsFile)) {
			reader.lines().forEach(new Consumer<String>() {
				@Override
				public void accept(String line) {
					if (!line.startsWith("\"")) {
						Map<Integer, Sentiment> classPrediction = new HashMap<Integer, Sentiment>();
						String[] tokens = line.split(",");
						for (int i = 1; i <= tokens.length; ++i) {
							String token = tokens[i - 1];
							if (token.equals("Positive")) {
								classPrediction.put(i, Sentiment.POSITIVE);
							} else if (token.equals("Negative")) {
								classPrediction.put(i, Sentiment.NEGATIVE);
							}
						}
						classPredictions.add(classPrediction);
					}
				}
			});
		} catch (IOException e) {
			throw new IOException("Can't access the file with class predictions.", e);
		}
		return classPredictions;
	}
}