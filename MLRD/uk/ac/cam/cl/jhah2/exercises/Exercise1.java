package uk.ac.cam.cl.jhah2.exercises;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.*;

public class Exercise1 implements IExercise1
{
	private class WordSentiment
	{
		public static final double STRONG = 2;
		public static final double WEAK = 1;

		public static final double POSITIVE = 1;
		//public static final double NEGATIVE = -1.1;
		public static final double NEGATIVE = -1;

		public double intensity = 0;
		public double polarity = 0;

		public boolean modifier = false;
	}

	public static final double BEGIN_PENALTY = 0.6;
	public static final double BEGIN_FRAC = 0.2;

	public static final double END_BONUS = 1.7;
	public static final double END_FRAC = 0.07;

	public static final double IDF_POW = 0.75;

	public static final double MOD_AMNT = 1.2;
	public static final double MOD_DECAY = 0.5;

	private HashMap<String, WordSentiment> words;

	private void importLexicon(Path lexiconFile) throws IOException
	{
		words = new HashMap<>();
		BufferedReader br = Files.newBufferedReader(lexiconFile);
		String line;
		while ((line = br.readLine()) != null)
		{
			String[] parts = line.split(" ");

			String word = null;
			WordSentiment ws = new WordSentiment();
			for (String part : parts)
			{
				String[] kv = part.split("=");

				if (kv[0].equals("word"))
					word = kv[1];
				else if (kv[0].equals("intensity"))
					ws.intensity =
						kv[1].equals("strong")
						? WordSentiment.STRONG
						: WordSentiment.WEAK;
				else if (kv[0].equals("polarity"))
					ws.polarity =
						kv[1].equals("positive")
						? WordSentiment.POSITIVE
						: WordSentiment.NEGATIVE;

				String w = kv[1];

				if (w.charAt(w.length()-1) != 'y' || w.charAt(w.length()-2) != 'l') continue;
				ws.modifier = true;
			}

			words.put(word, ws);
		}
	}

	private Sentiment simpleClassifyFile(Path file) throws IOException
	{
		List<String> reviewWords = Tokenizer.tokenize(file);

		int sentiment = 0;
		for (String w : reviewWords)
			if (words.containsKey(w))
				sentiment += words.get(w).polarity == WordSentiment.POSITIVE ? 1 : -1;

		return sentiment >= 0 ? Sentiment.POSITIVE : Sentiment.NEGATIVE;
	}

	public Map<Path, Sentiment> simpleClassifier(Set<Path> testSet, Path lexiconFile) throws IOException
	{
		importLexicon(lexiconFile);

		HashMap<Path, Sentiment> results = new HashMap<>();

		for (Path path : testSet)
			results.put(path, simpleClassifyFile(path));

		return results;
	}

	public double calculateAccuracy(Map<Path, Sentiment> trueSentiments, Map<Path, Sentiment> predictedSentiments)
	{
		double score = 0.0;

		for (Path p : predictedSentiments.keySet())
		{
			if(trueSentiments.get(p) == predictedSentiments.get(p)) score += 1.0;
			//else
				//System.out.println(p.toString());
		}

		score /= predictedSentiments.keySet().size();

		return score;
	}

	private double improvedClassifyFile(Path file) throws IOException
	{
		List<String> reviewWords = Tokenizer.tokenize(file);

		int reviewCount = reviewWords.size();

		HashMap<String, Double> counts = new HashMap<>();

		for (int i = 0; i < reviewCount; ++i)
		{
			String w = reviewWords.get(i);

			if (!words.containsKey(w)) continue;

			double cnt = 1;
			if (counts.containsKey(w)) cnt += counts.get(w);
			counts.put(w, cnt);
		}

		double sentiment = 0;
		double mod = 1.0;
		for (int i = 0; i < reviewCount; ++i)
		{
			String w = reviewWords.get(i);

			if (!words.containsKey(w)) continue;

			if (words.get(w).modifier)
				mod *= MOD_AMNT * words.get(w).intensity;

			sentiment +=
				words.get(w).polarity
				* words.get(w).intensity
				* (i > reviewCount * BEGIN_FRAC
				? 1.0
				: BEGIN_PENALTY)
				* (i > reviewCount * (1-END_FRAC)
				? END_BONUS
				: 1.0)
				/ Math.pow(counts.get(w), IDF_POW)
				* mod;

			mod = Math.pow(mod, MOD_DECAY);
		}

		return sentiment;
	}

	public Map<Path, Sentiment> improvedClassifier(Set<Path> testSet, Path lexiconFile) throws IOException
	{
		importLexicon(lexiconFile);

		HashMap<Path, Sentiment> results = new HashMap<>();

		for (Path path : testSet)
			results.put(path, improvedClassifyFile(path) >= 4.21 ? Sentiment.POSITIVE : Sentiment.NEGATIVE);

		return results;
	}
}