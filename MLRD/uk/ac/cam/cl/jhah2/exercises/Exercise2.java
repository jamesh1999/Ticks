package uk.ac.cam.cl.jhah2.exercises;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.*;

public class Exercise2 implements IExercise2
{
	public Map<Sentiment, Double> calculateClassProbabilities(Map<Path, Sentiment> trainingSet) throws IOException
	{

		// Count positive reviews
		int pos = 0;
		for (Sentiment s : trainingSet.values())
			if (s == Sentiment.POSITIVE) pos += 1;

		// Add class probabilities to map
		Map<Sentiment, Double> results = new HashMap<>();
		results.put(Sentiment.POSITIVE, (double) pos / trainingSet.size());
		results.put(Sentiment.NEGATIVE, (double) (trainingSet.size() - pos) / trainingSet.size());

		return results;
	}

	public Map<String, Map<Sentiment, Double>> calculateUnsmoothedLogProbs(Map<Path, Sentiment> trainingSet)
			throws IOException
	{
		int posWords = 0;
		int negWords = 0;

		Map<String, Map<Sentiment, Double>> results = new HashMap<String, Map<Sentiment, Double>>();

		// Count word occurances for each category plus total words
		for (Map.Entry<Path, Sentiment> kvp : trainingSet.entrySet())
		{
			List<String> words = Tokenizer.tokenize(kvp.getKey());
			if (kvp.getValue() == Sentiment.POSITIVE)
				posWords += words.size();
			else 
				negWords += words.size();

			for (String w : words)
			{
				if (!results.containsKey(w))
				{
					results.put(w, new HashMap<Sentiment, Double>());
					results.get(w).put(Sentiment.POSITIVE, 0.0);
					results.get(w).put(Sentiment.NEGATIVE, 0.0);
				}

				double cur = results.get(w).get(kvp.getValue());
				results.get(w).put(kvp.getValue(), cur + 1);
			}
		}

		// Log and divide through by word count
		for (String w : results.keySet())
		{
			results.get(w).put(Sentiment.POSITIVE, Math.log(results.get(w).get(Sentiment.POSITIVE) / posWords));
			results.get(w).put(Sentiment.NEGATIVE, Math.log(results.get(w).get(Sentiment.NEGATIVE) / negWords));
		}

		return results;
	}

	public Map<String, Map<Sentiment, Double>> calculateSmoothedLogProbs(Map<Path, Sentiment> trainingSet)
			throws IOException
	{
		int posWords = 0;
		int negWords = 0;

		Map<String, Map<Sentiment, Double>> results = new HashMap<String, Map<Sentiment, Double>>();

		for (Map.Entry<Path, Sentiment> kvp : trainingSet.entrySet())
		{
			List<String> words = Tokenizer.tokenize(kvp.getKey());
			if (kvp.getValue() == Sentiment.POSITIVE)
				posWords += words.size();
			else 
				negWords += words.size();

			for (String w : words)
			{
				if (!results.containsKey(w))
				{
					results.put(w, new HashMap<Sentiment, Double>());
					results.get(w).put(Sentiment.POSITIVE, 1.0); // Initial value is 1
					results.get(w).put(Sentiment.NEGATIVE, 1.0);
					posWords += 1; // Keep track of 'extra' words
					negWords += 1;
				}

				double cur = results.get(w).get(kvp.getValue());
				results.get(w).put(kvp.getValue(), cur + 1);
			}
		}

		for (String w : results.keySet())
		{
			results.get(w).put(Sentiment.POSITIVE, Math.log(results.get(w).get(Sentiment.POSITIVE) / posWords));
			results.get(w).put(Sentiment.NEGATIVE, Math.log(results.get(w).get(Sentiment.NEGATIVE) / negWords));
		}

		return results;
	}

	private Sentiment naiveBayesPath(Path review, Map<String, Map<Sentiment, Double>> tokenLogProbs,
			Map<Sentiment, Double> classProbabilities) throws IOException
	{
		double pPos = Math.log(classProbabilities.get(Sentiment.POSITIVE));
		double pNeg = Math.log(classProbabilities.get(Sentiment.NEGATIVE));
		for (String w : Tokenizer.tokenize(review))
		{
			if (tokenLogProbs.get(w) == null) continue;
			pPos += tokenLogProbs.get(w).get(Sentiment.POSITIVE);
			pNeg += tokenLogProbs.get(w).get(Sentiment.NEGATIVE);
		}

		return pPos > pNeg ? Sentiment.POSITIVE : Sentiment.NEGATIVE;
	}

	public Map<Path, Sentiment> naiveBayes(Set<Path> testSet, Map<String, Map<Sentiment, Double>> tokenLogProbs,
			Map<Sentiment, Double> classProbabilities) throws IOException
	{
		Map<Path, Sentiment> results = new HashMap<Path, Sentiment>();

		for (Path p : testSet)
			results.put(p, naiveBayesPath(p, tokenLogProbs, classProbabilities));

		return results;
	}
}