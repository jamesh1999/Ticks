package uk.ac.cam.cl.jhah2.exercises;

import uk.ac.cam.cl.mlrd.exercises.social_networks.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.io.*;

public class Exercise12 implements IExercise12
{
	/**
     * Compute graph clustering using the Girvan-Newman method. Stop algorithm when the
     * minimum number of components has been reached (your answer may be higher than 
     * the minimum).
     * 
     * @param graph
     *        {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *        loaded graph
     * @param minimumComponents {@link int} The minimum number of components to reach.
     * @return {@link List}<{@link Set}<{@link Integer}>>
     *        List of components for the graph.
     */
    public List<Set<Integer>> GirvanNewman(Map<Integer, Set<Integer>> graph, int minimumComponents)
    {
    	List<Set<Integer>> components = getComponents(graph);
    	while (components.size() < minimumComponents)
    	{
    		Map<Integer, Map<Integer, Double>> betweenness = getEdgeBetweenness(graph);

    		double maxBetweenness = 0.0;
    		for (Integer a : betweenness.keySet())
    			for (Integer b : betweenness.get(a).keySet())
    			{
    				if (betweenness.get(a).get(b) > maxBetweenness)
    					maxBetweenness = betweenness.get(a).get(b);
    			}

    		for (Integer a : betweenness.keySet())
    		{
    			for (Integer b : betweenness.get(a).keySet())
    			{
    				if (Math.abs(betweenness.get(a).get(b) - maxBetweenness) < 1e-06)
    					graph.get(a).remove(b);
    			}
    		}

    		components = getComponents(graph);
    	}
    	return components;
    }

    public int getNumberOfEdges(Map<Integer, Set<Integer>> graph)
    {
    	int tot = 0;
    	for (Integer vtx : graph.keySet())
    		tot += graph.get(vtx).size();

    	return tot / 2;
    }

    public List<Set<Integer>> getComponents(Map<Integer, Set<Integer>> graph)
    {
    	List<Set<Integer>> components = new ArrayList<>();
    	Set<Integer> visited = new HashSet<>();

    	for (Integer vtx : graph.keySet())
    	{
    		if (visited.contains(vtx)) continue;

    		Set<Integer> component = new HashSet<>();

    		Deque<Integer> stack = new ArrayDeque<>();
    		stack.addFirst(vtx);

    		Integer next;
    		while ((next = stack.pollFirst()) != null)
    		{
    			visited.add(next);
    			component.add(next);
    			for (Integer adj : graph.get(next))
    			{
    				if (visited.contains(adj)) continue;
    				stack.addFirst(adj);
    			}
    		}

    		components.add(component);
    	}

    	return components;
    }

    /**
     * Calculate the edge betweenness.
     * 
     * @param graph
     *         {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *         loaded graph
     * @return {@link Map}<{@link Integer}, 
     *         {@link Map}<{@link Integer},{@link Double}>> Edge betweenness for
     *         each pair of vertices in the graph
     */
    public Map<Integer, Map<Integer, Double>> getEdgeBetweenness(Map<Integer, Set<Integer>> graph)
    {
    	Queue<Integer> queue = new ArrayDeque<>();
		Deque<Integer> stack = new ArrayDeque<>();
		Map<Integer, Integer> distance = new HashMap<>();
		Map<Integer, List<Integer>> predecessors = new HashMap<>();
		Map<Integer, Integer> sigma = new HashMap<>();
		Map<Integer, Double> delta = new HashMap<>();


		Map<Integer, Map<Integer, Double>> betweenness = new HashMap<>();
		for (Integer vtx : graph.keySet())
			betweenness.put(vtx, new HashMap<>());

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
				{
					if (!betweenness.get(pred).containsKey(vtx)) betweenness.get(pred).put(vtx, 0.0);
					betweenness.get(pred).put(vtx, betweenness.get(pred).get(vtx) + ((double) sigma.get(pred)) / sigma.get(vtx) * (1.0 + delta.get(vtx)));
					delta.put(pred, delta.get(pred) + ((double) sigma.get(pred)) / sigma.get(vtx) * (1.0 + delta.get(vtx)));
				}
			}
		}

		return betweenness;
    }
}