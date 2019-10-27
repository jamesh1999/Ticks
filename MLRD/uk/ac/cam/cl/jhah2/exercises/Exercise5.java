package uk.ac.cam.cl.jhah2.exercises;

import java.util.*;
import java.nio.file.*;
import java.io.*;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.*;

public class Exercise5 implements IExercise5
{
	public List<Map<Path, Sentiment>> splitCVRandom(Map<Path, Sentiment> dataSet, int seed)
	{
		Random r = new Random(seed);

		List<Map<Path, Sentiment>> result = new ArrayList<>();
		for (int i = 0; i < 10; ++i)
			result.add(new HashMap<Path, Sentiment>());

		for (Path p : dataSet.keySet())
		{
			int fold = r.nextInt() % 10;
			if (fold < 0) fold += 10;
			result.get(fold).put(p, dataSet.get(p));
		}

		return result;
	}

	public List<Map<Path, Sentiment>> splitCVStratifiedRandom(Map<Path, Sentiment> dataSet, int seed)
	{
		List<Map.Entry<Path, Sentiment>> pos = new ArrayList<>();
		List<Map.Entry<Path, Sentiment>> neg = new ArrayList<>();

		for (Map.Entry<Path, Sentiment> entry : dataSet.entrySet())
		{
			if (entry.getValue() == Sentiment.POSITIVE) pos.add(entry);
			else neg.add(entry);
		}

		Collections.shuffle(pos);
		Collections.shuffle(neg);

		List<Map<Path, Sentiment>> result = new ArrayList<>();
		for (int i = 0; i < 10; ++i)
			result.add(new HashMap<Path, Sentiment>());

		for (int i = 0; i < pos.size(); ++i)
		{
			Map.Entry<Path, Sentiment> p = pos.get(i);
			result.get(i % 10).put(p.getKey(), p.getValue());
		}

		for (int i = 0; i < neg.size(); ++i)
		{
			Map.Entry<Path, Sentiment> n = neg.get(i);
			result.get(i % 10).put(n.getKey(), n.getValue());
		}

		return result;
	}

	public double[] crossValidate(List<Map<Path, Sentiment>> folds) throws IOException
	{
		double[] scores = new double[folds.size()];
		Exercise1 simple = new Exercise1();
		Exercise2 nb = new Exercise2();

		for (int i = 0; i < folds.size(); ++i)
		{
			Map<Path, Sentiment> test = folds.get(i);
			Map<Path, Sentiment> training = new HashMap<>();
			Map<Sentiment, Double> classProbs = new HashMap<>();
			classProbs.put(Sentiment.POSITIVE, 0.0);
			classProbs.put(Sentiment.NEGATIVE, 0.0);

			long count = 0;
			for (int j = 0; j < folds.size(); ++j)
			{
				if (j == i) continue;
				Map<Path, Sentiment> fold = folds.get(j);
				for (Path p : fold.keySet())
				{
					training.put(p, fold.get(p));
					classProbs.put(fold.get(p), 1.0 + classProbs.get(fold.get(p)));
					count += 1;
				}
			}

			classProbs.put(Sentiment.POSITIVE, classProbs.get(Sentiment.POSITIVE) / count);
			classProbs.put(Sentiment.NEGATIVE, classProbs.get(Sentiment.POSITIVE) / count);

			Map<String, Map<Sentiment, Double>> probs = nb.calculateSmoothedLogProbs(training);
			scores[i] = simple.calculateAccuracy(test, nb.naiveBayes(test.keySet(), probs, classProbs));
		}

		return scores;
	}

	public double cvAccuracy(double[] scores)
	{
		double tot = 0.0;
		for (int i = 0; i < scores.length; ++i)
			tot += scores[i];
		tot /= scores.length;
		return tot;
	}

	public double cvVariance(double[] scores)
	{
		double tot = 0.0;
		double mean = cvAccuracy(scores);

		for (int i = 0; i < scores.length; ++i)
			tot += Math.pow(scores[i] - mean, 2.0);
		tot /= scores.length;
		return tot;
	}
}