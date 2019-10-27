package uk.ac.cam.cl.mlrd.exercises.social_networks;

import java.nio.file.Path;
import java.util.Map;
import java.io.IOException;

public interface IExercise11 {

    /**
     * Load the graph file. Use Brandes' algorithm to calculate the betweenness
     * centrality for each node in the graph.
     * 
     * @param graphFile
     *            {@link Path} the path to the network specification
     * @return {@link Map}<{@link Integer}, {@link Double}> For
     *         each node, its betweenness centrality
     */
    public Map<Integer, Double> getNodeBetweenness(Path graphFile) throws IOException;
}