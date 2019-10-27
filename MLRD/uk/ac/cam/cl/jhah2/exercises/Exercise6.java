package uk.ac.cam.cl.jhah2.exercises;

import java.util.*;
import java.nio.file.*;
import java.io.*;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.*;

public class Exercise6 implements IExercise6
{
	public Map<NuancedSentiment, Double> calculateClassProbabilities(Map<Path, NuancedSentiment> trainingSet) throws IOException
	{
		// Class counters
		int pos = 0;
		int neutral = 0;
		int neg = 0;
		for (NuancedSentiment s : trainingSet.values())
		{
			switch(s)
			{
			case POSITIVE:
				pos += 1;
				break;
			case NEUTRAL:
				neutral += 1;
				break;
			case NEGATIVE:
				neg += 1;
				break;
			}
		}

		// Add class probabilities to map
		Map<NuancedSentiment, Double> results = new HashMap<>();
		results.put(NuancedSentiment.POSITIVE, (double) pos / trainingSet.size());
		results.put(NuancedSentiment.NEUTRAL, (double) neutral / trainingSet.size());
		results.put(NuancedSentiment.NEGATIVE, (double) neg / trainingSet.size());

		return results;
	}
	
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
			throws IOException
	{
		int posWords = 0;
		int neutWords = 0;
		int negWords = 0;

		Map<String, Map<NuancedSentiment, Double>> results = new HashMap<String, Map<NuancedSentiment, Double>>();

		for (Map.Entry<Path, NuancedSentiment> kvp : trainingSet.entrySet())
		{
			List<String> words = Tokenizer.tokenize(kvp.getKey());
			switch (kvp.getValue())
			{
				case POSITIVE:
				posWords += words.size();
				break;
				case NEUTRAL:
				neutWords += words.size();
				break;
				case NEGATIVE:
				negWords += words.size();
				break;
			}

			for (String w : words)
			{
				if (!results.containsKey(w))
				{
					results.put(w, new HashMap<NuancedSentiment, Double>());
					results.get(w).put(NuancedSentiment.POSITIVE, 1.0); // Initial value is 1
					results.get(w).put(NuancedSentiment.NEUTRAL, 1.0); // Initial value is 1
					results.get(w).put(NuancedSentiment.NEGATIVE, 1.0);
					posWords += 1; // Keep track of 'extra' words
					neutWords += 1;
					negWords += 1;
				}

				double cur = results.get(w).get(kvp.getValue());
				results.get(w).put(kvp.getValue(), cur + 1);
			}
		}

		for (String w : results.keySet())
		{
			results.get(w).put(NuancedSentiment.POSITIVE, Math.log(results.get(w).get(NuancedSentiment.POSITIVE) / posWords));
			results.get(w).put(NuancedSentiment.NEUTRAL, Math.log(results.get(w).get(NuancedSentiment.NEUTRAL) / neutWords));
			results.get(w).put(NuancedSentiment.NEGATIVE, Math.log(results.get(w).get(NuancedSentiment.NEGATIVE) / negWords));
		}

		return results;
	}
	
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
	private NuancedSentiment naiveBayesPath(Path review, Map<String, Map<NuancedSentiment, Double>> tokenLogProbs,
			Map<NuancedSentiment, Double> classProbabilities) throws IOException
	{
		double pPos = Math.log(classProbabilities.get(NuancedSentiment.POSITIVE));
		double pNeut = Math.log(classProbabilities.get(NuancedSentiment.NEUTRAL));
		double pNeg = Math.log(classProbabilities.get(NuancedSentiment.NEGATIVE));
		for (String w : Tokenizer.tokenize(review))
		{
			if (tokenLogProbs.get(w) == null) continue;
			pPos += tokenLogProbs.get(w).get(NuancedSentiment.POSITIVE);
			pNeut += tokenLogProbs.get(w).get(NuancedSentiment.NEUTRAL);
			pNeg += tokenLogProbs.get(w).get(NuancedSentiment.NEGATIVE);
		}

		if (pPos > pNeg && pPos > pNeut) return NuancedSentiment.POSITIVE;
		if (pNeut > pNeg) return NuancedSentiment.NEUTRAL;
		return NuancedSentiment.NEGATIVE;
	}

	public 	Map<Path, NuancedSentiment> nuancedClassifier(Set<Path> testSet,
			Map<String, Map<NuancedSentiment, Double>> tokenLogProbs, Map<NuancedSentiment, Double> classProbabilities)
			throws IOException
	{
		Map<Path, NuancedSentiment> results = new HashMap<Path, NuancedSentiment>();

		for (Path p : testSet)
			results.put(p, naiveBayesPath(p, tokenLogProbs, classProbabilities));

		return results;
	}

	public double nuancedAccuracy(Map<Path, NuancedSentiment> trueSentiments,
			Map<Path, NuancedSentiment> predictedSentiments)
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

	public Map<Integer, Map<Sentiment, Integer>> agreementTable(Collection<Map<Integer, Sentiment>> predictedSentiments)
	{
		Map<Integer, Map<Sentiment, Integer>> result = new HashMap<>();
		for (int i = 1; i <= 4; ++i)
		{
			Map<Sentiment, Integer> val = new HashMap<>();
			val.put(Sentiment.POSITIVE, 0);
			val.put(Sentiment.NEGATIVE, 0);
			result.put(i, val);
		}

		for (Map<Integer, Sentiment> guesses : predictedSentiments)
		{
			for (int i = 1; i <= 4; ++i)
				result.get(i).put(guesses.get(i), 1 + result.get(i).get(guesses.get(i)));
		}

		return result;
	}

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
	public double kappa(Map<Integer, Map<Sentiment, Integer>> agreementTable)
	{
		System.out.println(agreementTable);
		double pE = 0.0;

		double pos = 0.0;
		for (int i : agreementTable.keySet())
		{
			Map<Sentiment, Integer> classCnts = agreementTable.get(i);

			if (!classCnts.containsKey(Sentiment.POSITIVE)) classCnts.put(Sentiment.POSITIVE, 0);
			if (!classCnts.containsKey(Sentiment.NEGATIVE)) classCnts.put(Sentiment.NEGATIVE, 0);

			pos += (double) classCnts.get(Sentiment.POSITIVE) / (classCnts.get(Sentiment.POSITIVE) + classCnts.get(Sentiment.NEGATIVE));
		}
		pos /= agreementTable.keySet().size();
		pE += pos*pos;

		double neg = 0.0;
		for (int i : agreementTable.keySet())
		{
			Map<Sentiment, Integer> classCnts = agreementTable.get(i);
			neg += (double) classCnts.get(Sentiment.NEGATIVE) / (classCnts.get(Sentiment.POSITIVE) + classCnts.get(Sentiment.NEGATIVE));
		}
		neg /= agreementTable.keySet().size();
		pE += neg*neg;

		double pA = 0.0;
		for (int i : agreementTable.keySet())
		{
			double reviewVal = 0.0;
			Map<Sentiment, Integer> classCnts = agreementTable.get(i);
			int tot = classCnts.get(Sentiment.POSITIVE) + classCnts.get(Sentiment.NEGATIVE);

			reviewVal += classCnts.get(Sentiment.POSITIVE) * (classCnts.get(Sentiment.POSITIVE) - 1.0);
			reviewVal += classCnts.get(Sentiment.NEGATIVE) * (classCnts.get(Sentiment.NEGATIVE) - 1.0);

			reviewVal /= tot * (tot - 1.0);

			pA += reviewVal;
		}
		pA /= agreementTable.keySet().size();

		System.out.println(pA);
		System.out.println(pE);

		double k = (pA - pE) / (1.0 - pE);
		return k;
	}
}