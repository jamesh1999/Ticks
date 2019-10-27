package uk.ac.cam.cl.jhah2.exercises;

import uk.ac.cam.cl.mlrd.exercises.social_networks.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.io.*;

public class Exercise11 implements IExercise11
{
	public Map<Integer, Double> getNodeBetweenness(Path graphFile) throws IOException
	{
		IExercise10 impl = new Exercise10();
		Map<Integer, Set<Integer>> graph = impl.loadGraph(graphFile);

		Queue<Integer> queue = new ArrayDeque<>();
		Deque<Integer> stack = new ArrayDeque<>();
		Map<Integer, Integer> distance = new HashMap<>();
		Map<Integer, List<Integer>> predecessors = new HashMap<>();
		Map<Integer, Integer> sigma = new HashMap<>();
		Map<Integer, Double> delta = new HashMap<>();


		Map<Integer, Double> betweenness = new HashMap<>();
		for (Integer vtx : graph.keySet())
			betweenness.put(vtx, 0.0);

		for (Integer source : graph.keySet())
		{
			for (Integer vtx : graph.keySet())
			{
				predecessors.put(vtx, new ArrayList<>());
				distance.put(vtx, -1);
				sigma.put(vtx, 0);
				delta.put(vtx, 0.0);
			}
			distance.put(source, 0);
			sigma.put(source, 1);
			queue.add(source);

			Integer vtx;
			while ((vtx = queue.poll()) != null)
			{
				stack.addFirst(vtx);
				for (Integer adj : graph.get(vtx))
				{
					// Discover newly formed paths
					if (distance.get(adj).equals(-1))
					{
						distance.put(adj, distance.get(vtx) + 1);
						queue.add(adj);
					}

					// Count shortest paths
					if (distance.get(adj).equals(distance.get(vtx) + 1))
					{
						sigma.put(adj, sigma.get(adj) + sigma.get(vtx));
						predecessors.get(adj).add(vtx);
					}
				}
			}

			while ((vtx = stack.pollFirst()) != null)
			{
				for (Integer pred : predecessors.get(vtx))
					delta.put(pred, delta.get(pred) + ((double) sigma.get(pred)) / sigma.get(vtx) * (1.0 + delta.get(vtx)));

				if (source.equals(vtx)) continue;

				betweenness.put(vtx, betweenness.get(vtx) + delta.get(vtx));
			}
		}

		// Undirected graph correction
		for (Integer vtx : betweenness.keySet())
			betweenness.put(vtx, betweenness.get(vtx) / 2);

		return betweenness;
	}
}