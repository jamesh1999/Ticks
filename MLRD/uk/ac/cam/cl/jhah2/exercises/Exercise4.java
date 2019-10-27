package uk.ac.cam.cl.jhah2.exercises;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.math.BigInteger;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.*;

public class Exercise4 implements IExercise4
{
	private class WordSentiment
	{
		public static final double STRONG = 2;
		public static final double WEAK = 1;

		public static final double POSITIVE = 1;
		public static final double NEGATIVE = -1;

		public double intensity = 0;
		public double polarity = 0;

		public boolean modifier = false;
	}

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

	private Sentiment magnitudeClassifyFile(Path file) throws IOException
	{
		List<String> reviewWords = Tokenizer.tokenize(file);

		int sentiment = 0;
		for (String w : reviewWords)
			if (words.containsKey(w))
				sentiment += words.get(w).polarity * words.get(w).intensity;

		return sentiment >= 0 ? Sentiment.POSITIVE : Sentiment.NEGATIVE;
	}

	public Map<Path, Sentiment> magnitudeClassifier(Set<Path> testSet, Path lexiconFile) throws IOException
	{
		importLexicon(lexiconFile);

		HashMap<Path, Sentiment> results = new HashMap<>();

		for (Path path : testSet)
			results.put(path, magnitudeClassifyFile(path));

		return results;
	}

	public double signTest(Map<Path, Sentiment> actualSentiments, Map<Path, Sentiment> classificationA,
			Map<Path, Sentiment> classificationB)
	{
		int plus = 0;
		int nul = 0;
		int minus = 0;

		for (Path p : actualSentiments.keySet())
		{
			if(classificationA.get(p) == classificationB.get(p)) nul += 1;
			else if(actualSentiments.get(p) == classificationA.get(p)) plus += 1;
			else minus += 1;
		}

		int k = ((nul + 1) >> 1) + (plus > minus ? minus : plus);
		int n = 2*((nul + 1) >> 1) + plus + minus;

		BigInteger tot = BigInteger.ZERO;
		for (int i = 0; i <= k; ++i)
		{
			tot = tot.add(choose(n, i));
		}

		int thresh = 53;

		tot = tot.shiftRight(n > thresh+1 ? n-thresh-1 : 0);

		double score = tot.doubleValue();
		score /= Math.pow(2, n > thresh+1 ? thresh : n-1);

		return score;
	}

	private BigInteger fact(int n)
	{
		if (n == 0) return BigInteger.ONE;
		return fact(n-1).multiply(BigInteger.valueOf(n));
	}

	private BigInteger choose(int n, int r)
	{
		return fact(n).divide(fact(r).multiply(fact(n-r)));
	}
}