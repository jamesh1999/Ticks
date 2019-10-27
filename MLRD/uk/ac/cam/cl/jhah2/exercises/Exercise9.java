package uk.ac.cam.cl.jhah2.exercises;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

public class Exercise9 implements IExercise9
{
	public HiddenMarkovModel<AminoAcid, Feature> estimateHMM(List<HMMDataStore<AminoAcid, Feature>> sequencePairs) throws IOException
	{
		Map<Feature, Map<Feature, Double>> transition = new HashMap<>();
		Map<Feature, Map<AminoAcid, Double>> emission = new HashMap<>();

		for (Feature t : Feature.values())
		{
			transition.put(t, new HashMap<Feature, Double>());
			emission.put(t, new HashMap<AminoAcid, Double>());

			for (Feature u : Feature.values())
				transition.get(t).put(u, 0.0);
			for (AminoAcid r : AminoAcid.values())
				emission.get(t).put(r, 0.0);
		}

		for (HMMDataStore<AminoAcid, Feature> chain : sequencePairs)
		{
			for (int i = 0; i < chain.hiddenSequence.size(); ++i)
			{
				Feature current = chain.hiddenSequence.get(i);
				AminoAcid roll = chain.observedSequence.get(i);

				emission.get(current).put(roll, 1.0 + emission.get(current).get(roll));

				if (i+1 == chain.hiddenSequence.size()) continue;

				Feature next = chain.hiddenSequence.get(i+1);
				transition.get(current).put(next, 1.0 + transition.get(current).get(next));
			}
		}

		// Divide by totals to get probs
		for (Feature t : transition.keySet())
		{
			double tot = 0.0;
			for (double val : transition.get(t).values())
				tot += val;

			if (tot == 0.0) continue;

			for (Feature n : transition.get(t).keySet())
				transition.get(t).put(n, transition.get(t).get(n) / tot);
		}

		for (Feature t : emission.keySet())
		{
			double tot = 0.0;
			for (double val : emission.get(t).values())
				tot += val;

			if (tot == 0.0) continue;

			for (AminoAcid r : emission.get(t).keySet())
				emission.get(t).put(r, emission.get(t).get(r) / tot);
		}

		return new HiddenMarkovModel<AminoAcid, Feature>(transition, emission);
	}

	public List<Feature> viterbi(HiddenMarkovModel<AminoAcid, Feature> model, List<AminoAcid> observedSequence)
	{
		List<Map<Feature, Feature>> previousStates = new ArrayList<>();
		List<Map<Feature, Double>> probabilities = new ArrayList<>();

		Map<Feature, Double> startState = new HashMap<>();
		startState.put(Feature.START, 0.0);
		probabilities.add(startState);

		for (int i = 1; i < observedSequence.size(); ++i)
		{
			AminoAcid observation = observedSequence.get(i);

			Map<Feature, Feature> psi = new HashMap<>();
			Map<Feature, Double> delta = new HashMap<>();

			for (Feature die : model.getHiddenStates())
				delta.put(die, -9999999999999999.0);

			for (Feature dieA : probabilities.get(i-1).keySet())
			{
				double pPrevious = probabilities.get(i-1).get(dieA);
				Map<Feature, Double> transitions = model.getTransitions(dieA);
				for (Feature dieB : transitions.keySet())
				{
					double prob = pPrevious + Math.log(transitions.get(dieB)) + Math.log(model.getEmissions(dieB).get(observation));
					if (prob > delta.get(dieB))
					{
						delta.put(dieB, prob);
						psi.put(dieB, dieA);
					}
				}
			}

			probabilities.add(delta);
			previousStates.add(psi);
		}

		// Backtrack
		List<Feature> ret = new ArrayList<>();
		Feature current = Feature.END;
		ret.add(current);
		for (int i = observedSequence.size() - 2; i >= 0; --i)
		{
			current = previousStates.get(i).get(current);
			ret.add(0, current);
		}

		return ret;
	}

	public Map<List<Feature>, List<Feature>> predictAll(HiddenMarkovModel<AminoAcid, Feature> model,
			List<HMMDataStore<AminoAcid, Feature>> testSequencePairs) throws IOException
	{
		Map<List<Feature>, List<Feature>> ret = new HashMap<>();
		for (HMMDataStore<AminoAcid, Feature> file : testSequencePairs)
		{
			ret.put(file.hiddenSequence, viterbi(model, file.observedSequence));
		}
		
		return ret;
	}

	public double precision(Map<List<Feature>, List<Feature>> true2PredictedMap)
	{
		long t = 0;
		long loaded = 0;

		for (List<Feature> dice : true2PredictedMap.keySet())
		{
			for (int i = 0; i < dice.size(); ++i)
			{
				if (true2PredictedMap.get(dice).get(i) != Feature.MEMBRANE) continue;
				loaded++;
				if (dice.get(i) != Feature.MEMBRANE) continue;
				t++;
			}
		}

		return (double) t / loaded;
	}

	public double recall(Map<List<Feature>, List<Feature>> true2PredictedMap)
	{
		long t = 0;
		long loaded = 0;

		for (List<Feature> dice : true2PredictedMap.keySet())
		{
			for (int i = 0; i < dice.size(); ++i)
			{
				if (dice.get(i) != Feature.MEMBRANE) continue;
				loaded++;
				if (true2PredictedMap.get(dice).get(i) != Feature.MEMBRANE) continue;
				t++;
			}
		}

		return (double) t / loaded;
	}

	public double fOneMeasure(Map<List<Feature>, List<Feature>> true2PredictedMap)
	{
		double prec = precision(true2PredictedMap);
		double rec = recall(true2PredictedMap);
		return (2 * prec * rec) / (prec + rec);
	}
}