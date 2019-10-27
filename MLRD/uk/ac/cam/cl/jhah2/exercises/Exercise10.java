package uk.ac.cam.cl.jhah2.exercises;

import uk.ac.cam.cl.mlrd.exercises.social_networks.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.io.*;

public class Exercise10 implements IExercise10
{
	public Map<Integer, Set<Integer>> loadGraph(Path graphFile) throws IOException
	{
		Map<Integer, Set<Integer>> graph = new HashMap<>();

		try (BufferedReader reader = Files.newBufferedReader(graphFile)) {
			reader.lines().forEach(new Consumer<String>() {
				@Override
				public void accept(String line) {
					String[] tokens = line.split(" ");
					
					if (!graph.containsKey(Integer.parseInt(tokens[0])))
						graph.put(Integer.parseInt(tokens[0]), new HashSet<>());
					if (!graph.containsKey(Integer.parseInt(tokens[1])))
						graph.put(Integer.parseInt(tokens[1]), new HashSet<>());

					graph.get(Integer.parseInt(tokens[0])).add(Integer.parseInt(tokens[1]));
					graph.get(Integer.parseInt(tokens[1])).add(Integer.parseInt(tokens[0]));
				}
			});
		} catch (IOException e) {
			throw new IOException("Can't access the file " + graphFile, e);
		}
		return graph;
	}

	public Map<Integer, Integer> getConnectivities(Map<Integer, Set<Integer>> graph)
	{
		Map<Integer, Integer> counts = new HashMap<>();
		for (Integer vertex : graph.keySet())
		{
			counts.put(vertex, graph.get(vertex).size());
		}

		return counts;
	}

	public int getDiameter(Map<Integer, Set<Integer>> graph)
	{
		int maxDist = 0;

		// Start from each node
		for (Integer start : graph.keySet())
		{
			Set<Integer> visited = new HashSet<>();
			Queue<Integer> vertexQ = new ArrayDeque<>();
			Queue<Integer> distQ = new ArrayDeque<>();

			visited.add(start);
			vertexQ.add(start);
			distQ.add(0);

			Integer vtx;
			while ((vtx = vertexQ.poll()) != null)
			{
				int dist = distQ.remove();
				if (dist > maxDist) maxDist = dist;

				for (Integer adj : graph.get(vtx))
				{
					if (visited.contains(adj)) continue;
					visited.add(adj);
					vertexQ.add(adj);
					distQ.add(dist + 1);
				}
			}
		}

		return maxDist;
	}
}