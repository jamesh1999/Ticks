package uk.ac.cam.cl.mlrd.exercises.social_networks;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.io.IOException;

public interface IExercise10 {

	/**
	 * Load the graph file. Each line in the file corresponds to an edge; the
	 * first column is the source node and the second column is the target. As
	 * the graph is undirected, your program should add the source as a
	 * neighbour of the target as well as the target a neighbour of the source.
	 * 
	 * @param graphFile
	 *            {@link Path} the path to the network specification
	 * @return {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> For
	 *         each node, all the nodes neighbouring that node
	 */
	public Map<Integer, Set<Integer>> loadGraph(Path graphFile) throws IOException;

	/**
	 * Find the number of neighbours for each point in the graph.
	 * 
	 * @param graph
	 *            {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
	 *            loaded graph
	 * @return {@link Map}<{@link Integer}, {@link Integer}> For each node, the
	 *         number of neighbours it has
	 */
	public Map<Integer, Integer> getConnectivities(Map<Integer, Set<Integer>> graph);

	/**
	 * Find the maximal shortest distance between any two nodes in the network 
	 * using a breadth-first search.
	 * 
	 * @param graph
	 *            {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
	 *            loaded graph
	 * @return <code>int</code> The diameter of the network
	 */
	public int getDiameter(Map<Integer, Set<Integer>> graph);
}