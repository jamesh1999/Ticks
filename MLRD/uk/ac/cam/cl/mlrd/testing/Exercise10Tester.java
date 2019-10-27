package uk.ac.cam.cl.mlrd.testing;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.io.IOException;

//TODO: Replace with your package.
import uk.ac.cam.cl.jhah2.exercises.Exercise10;
import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise10;

public class Exercise10Tester {
    static final Path networkFile = Paths.get("data/network_files/simple_network.edges");

    public static void main(String[] args) throws IOException {
	IExercise10 implementation = (IExercise10) new Exercise10();
	Map<Integer, Set<Integer>> network;
	try {
	    network = implementation.loadGraph(networkFile);
	} catch (IOException e) {
	    throw new IOException("Can't access the edgelist file",e);
	}
	System.out.println("Network loaded.");
	System.out.println();

	Map<Integer, Integer> connectivities = implementation.getConnectivities(network);
	List<Entry<Integer, Integer>> sortedConnectivities = new ArrayList<>(connectivities.entrySet());
	sortedConnectivities.sort(new Comparator<Entry<Integer, Integer>>() {
		@Override
		    public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
		    return o2.getValue() - o1.getValue();
		}
	    });
	System.out.println("Network connectivities:");
	System.out.println(sortedConnectivities);
	System.out.println();
	
	int diameter = implementation.getDiameter(network);
	System.out.println("Network diameter: " + diameter);
	System.out.println();
    }
}