package uk.ac.cam.cl.mlrd.exercises.social_networks;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.io.IOException;

public interface IExercise12 {

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
    public List<Set<Integer>> GirvanNewman(Map<Integer, Set<Integer>> graph, int minimumComponents);

    /**
     * Find the number of edges in the graph.
     * 
     * @param graph
     *        {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *        loaded graph
     * @return {@link Integer}> Number of edges.
     */
    public int getNumberOfEdges(Map<Integer, Set<Integer>> graph);

    /**
     * Find the number of components in the graph using a DFS.
     * 
     * @param graph
     *        {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *        loaded graph
     * @return {@link List}<{@link Set}<{@link Integer}>>
     *        List of components for the graph.
     */
    public List<Set<Integer>> getComponents(Map<Integer, Set<Integer>> graph);

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
    public Map<Integer, Map<Integer, Double>> getEdgeBetweenness(Map<Integer, Set<Integer>> graph);
}
