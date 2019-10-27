package uk.ac.cam.cl.mlrd.exercises.markov_models;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HMMDataStore<T, U> {

	public List<T> observedSequence;
	public List<U> hiddenSequence;

	public HMMDataStore(List<T> observed, List<U> hidden) {
		this.observedSequence = observed;
		this.hiddenSequence = hidden;
	}

	/**
	 * Loads a file with a dice roll sequence.
	 * 
	 * @param sequenceFile
	 *            A file with the dice sequence.
	 * @return {@link HMMDataStore}<{@link DiceRoll}, {@link DiceType}> A
	 *         matched pair of observed and hidden sequences.
	 * @throws IOException
	 */
	public static HMMDataStore<DiceRoll, DiceType> loadDiceFile(Path sequenceFile) throws IOException {
		List<DiceRoll> rolls;
		List<DiceType> dice;
		try (BufferedReader reader = Files.newBufferedReader(sequenceFile)) {
			rolls = reader.readLine().chars().mapToObj(i -> DiceRoll.valueOf((char) i)).collect(Collectors.toList());
			dice = reader.readLine().chars().mapToObj(i -> DiceType.valueOf((char) i)).collect(Collectors.toList());
		} catch (IOException e) {
			throw new IOException("Can't load the data file.", e);
		}
		rolls.add(0, DiceRoll.START);
		rolls.add(DiceRoll.END);
		dice.add(0, DiceType.START);
		dice.add(DiceType.END);
		return new HMMDataStore<DiceRoll, DiceType>(rolls, dice);
	}

	/**
	 * Loads multiple files with dice sequences.
	 * 
	 * @param sequenceFiles
	 *            {@link Collection}<{@link Path}> Files with dice sequences.
	 * @return {@link List}<{@link HMMDataStore}<{@link DiceRoll},
	 *         {@link DiceType}>> A list of matched observed-hidden sequence
	 *         pairs.
	 * @throws IOException
	 */
	public static List<HMMDataStore<DiceRoll, DiceType>> loadDiceFiles(Collection<Path> sequenceFiles)
			throws IOException {
		List<HMMDataStore<DiceRoll, DiceType>> data = new ArrayList<HMMDataStore<DiceRoll, DiceType>>();
		for (Path p : sequenceFiles) {
			data.add(loadDiceFile(p));
		}
		return data;
	}

	/**
	 * Loads a file with amino acid sequences.
	 * 
	 */
	public static List<HMMDataStore<AminoAcid, Feature>> loadBioFile(Path sequenceFile) throws IOException {
		List<List<AminoAcid>> obsSeqs = new ArrayList<List<AminoAcid>>();
		List<List<Feature>> hiddenSeqs = new ArrayList<List<Feature>>();

		try (BufferedReader reader = Files.newBufferedReader(sequenceFile)) {
			reader.lines().forEach(new Consumer<String>() {
				@Override
				public void accept(String line) {
					if (!line.isEmpty()) {
						if (line.startsWith("#")) {
							List<AminoAcid> obs = line.substring(1).chars().mapToObj(i -> AminoAcid.valueOf((char) i))
									.collect(Collectors.toList());
							obs.add(0, AminoAcid.START);
							obs.add(AminoAcid.END);
							obsSeqs.add(obs);
						} else {
							List<Feature> hidden = line.chars().mapToObj(i -> Feature.valueOf((char) i))
									.collect(Collectors.toList());
							hidden.add(0, Feature.START);
							hidden.add(Feature.END);
							hiddenSeqs.add(hidden);
						}
					}
				}
			});
		} catch (IOException e) {
			throw new IOException("Can't access the file " + sequenceFile, e);
		}
		List<HMMDataStore<AminoAcid, Feature>> sequencePairs = IntStream.range(0, obsSeqs.size())
				.mapToObj(i -> new HMMDataStore<AminoAcid, Feature>(obsSeqs.get(i), hiddenSeqs.get(i)))
				.collect(Collectors.toList());

		return sequencePairs;
	}
}