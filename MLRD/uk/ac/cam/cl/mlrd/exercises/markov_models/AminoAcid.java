package uk.ac.cam.cl.mlrd.exercises.markov_models;

import java.util.HashMap;
import java.util.Map;

public enum AminoAcid {
	START('B'), ARG('R'), HIS('H'), LYS('K'), ASP('D'), GLU('E'), SER('S'), THR('T'), ASN('N'), GLN('Q'), CYS('C'), SEC('U'), GLY(
			'G'), PRO('P'), ALA('A'), VAL('V'), ILE('I'), LEU('L'), MET('M'), PHE('F'), TYR('Y'), TRP('W'), END('Z');

	private char name;

	private static Map<Character, AminoAcid> aminoAcids = new HashMap<>();
	static {
		for (AminoAcid aminoAcid : AminoAcid.values()) {
			aminoAcids.put(aminoAcid.name, aminoAcid);
		}
	}

	private AminoAcid(char name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.valueOf(name);
	}

	/**
	 * Get the amino acid corresponding to the given character.
	 * 
	 * @param name
	 *            <code>char</code> The char name of the amino acid
	 * @return {@link Feature} The corresponding amino acid
	 */
	public static AminoAcid valueOf(char name) {
		return aminoAcids.get(name);
	}
}