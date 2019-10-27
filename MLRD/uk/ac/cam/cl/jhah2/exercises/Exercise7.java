package uk.ac.cam.cl.jhah2.exercises;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

public class Exercise7 implements IExercise7
{
	public HiddenMarkovModel<DiceRoll, DiceType> estimateHMM(Collection<Path> sequenceFiles) throws IOException
	{
		List<HMMDataStore<DiceRoll, DiceType>> data = HMMDataStore.loadDiceFiles(sequenceFiles);

		Map<DiceType, Map<DiceType, Double>> transition = new HashMap<>();
		Map<DiceType, Map<DiceRoll, Double>> emission = new HashMap<>();

		for (DiceType t : DiceType.values())
		{
			transition.put(t, new HashMap<DiceType, Double>());
			emission.put(t, new HashMap<DiceRoll, Double>());

			for (DiceType u : DiceType.values())
				transition.get(t).put(u, 0.0);
			for (DiceRoll r : DiceRoll.values())
				emission.get(t).put(r, 0.0);
		}

		for (HMMDataStore<DiceRoll, DiceType> chain : data)
		{
			for (int i = 0; i < chain.hiddenSequence.size(); ++i)
			{
				DiceType current = chain.hiddenSequence.get(i);
				DiceRoll roll = chain.observedSequence.get(i);

				emission.get(current).put(roll, 1.0 + emission.get(current).get(roll));

				if (i+1 == chain.hiddenSequence.size()) continue;

				DiceType next = chain.hiddenSequence.get(i+1);
				transition.get(current).put(next, 1.0 + transition.get(current).get(next));
			}
		}

		// Divide by totals to get probs
		for (DiceType t : transition.keySet())
		{
			double tot = 0.0;
			for (double val : transition.get(t).values())
				tot += val;

			if (tot == 0.0) continue;

			for (DiceType n : transition.get(t).keySet())
				transition.get(t).put(n, transition.get(t).get(n) / tot);
		}

		for (DiceType t : emission.keySet())
		{
			double tot = 0.0;
			for (double val : emission.get(t).values())
				tot += val;

			if (tot == 0.0) continue;

			for (DiceRoll r : emission.get(t).keySet())
				emission.get(t).put(r, emission.get(t).get(r) / tot);
		}

		return new HiddenMarkovModel<DiceRoll, DiceType>(transition, emission);
	}
}