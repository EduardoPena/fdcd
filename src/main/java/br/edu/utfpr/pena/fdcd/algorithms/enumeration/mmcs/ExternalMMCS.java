package br.edu.utfpr.pena.fdcd.algorithms.enumeration.mmcs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.evidence.EvidenceSet;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.PredicateSpace;
import br.edu.utfpr.pena.fdcd.algorithms.enumeration.DCEnumeration;
import br.edu.utfpr.pena.fdcd.input.Table;
import br.edu.utfpr.pena.fdcd.input.columns.Column;
import br.edu.utfpr.pena.fdcd.utils.bitset.IBitSet;
import br.edu.utfpr.pena.fdcd.utils.bitset.LongBitSet;
import br.edu.utfpr.pena.fdcd.utils.io.IOHelper;
import br.edu.utfpr.pena.fdcd.utils.search.NTreeSearch;

public class ExternalMMCS implements DCEnumeration {

	private Table table;
	private PredicateSpace predicateSpace;
	private EvidenceSet evidenceSet;

	private Set<BitSet> mhss;
	private Set<BitSet> dcs;

	private boolean parallel = false;
	private int cores;

	public ExternalMMCS(Table table, PredicateSpace predicateSpace, EvidenceSet evidenceSet) {
		super();
		this.table = table;
		this.predicateSpace = predicateSpace;
		this.evidenceSet = evidenceSet;
	}

	public ExternalMMCS(Table table, PredicateSpace predicateSpace, EvidenceSet evidenceSet, boolean parallel) {
		super();
		this.table = table;
		this.predicateSpace = predicateSpace;
		this.evidenceSet = evidenceSet;
		this.parallel = parallel;
	}

	public ExternalMMCS(Table table, PredicateSpace predicateSpace, EvidenceSet evidenceSet, boolean parallel,
			int cores) {
		super();
		this.table = table;
		this.predicateSpace = predicateSpace;
		this.evidenceSet = evidenceSet;
		this.parallel = parallel;
		this.cores = cores;
	}

	public ExternalMMCS(PredicateSpace predicateSpace) {
		super();

		this.predicateSpace = predicateSpace;
	}

	public Set<BitSet> searchDCs() {

		String eviSetFile = table.NAME + ".eviset";

		// save eviSet into a file
		try {
			IOHelper.saveEvidenceSet2File(evidenceSet, eviSetFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// C++ implementation in
		// https://github.com/VeraLiconaResearchGroup/Minimal-Hitting-Set-Algorithms
		String program = "./agdmhs";
		String input = eviSetFile;
		String mhsOutput = eviSetFile.replace(".eviset", ".mhs");

		File oldMhss = new File(mhsOutput);
		oldMhss.delete();

		String alg = "pmmcs";// uno hitting set optimized
		Process process;

		// executes the mhs enumeration from file
		try {

			if (parallel) {
				int cores = Runtime.getRuntime().availableProcessors();
				process = new ProcessBuilder(program, input, mhsOutput, "-a", alg, "-t", ("" + cores)).start(); // parallel
			} else {
				process = new ProcessBuilder(program, input, mhsOutput, "-a", alg).start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}


		try {
			mhss = IOHelper.loadMHSsFromFile(mhsOutput);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		

		return getAllDCs();

	}

	public Set<BitSet> getAllDCs() {
		if (dcs == null) {
			dcs = new HashSet<>();
			for (BitSet mhs : mhss) {

				
				
				BitSet dc = mhs2DC(mhs);
				if (!isTrivialDC(dc)) {
					
					dcs.add(dc);
				}

			}

		}
		
		

		return dcs;
	}

	public void enumerateMHSsHybrid(Set<BitSet> bitEviSet, int pathI) {
		// pathI is to avoid threads writing using the same files

		String eviSetFile = "evi" + pathI + "" + ".eviset";

		// save eviSet into a file
		try {
			IOHelper.saveEvidenceSet2File(bitEviSet, eviSetFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// C++ implementation in
		// https://github.com/VeraLiconaResearchGroup/Minimal-Hitting-Set-Algorithms
		String program = "./agdmhs";
		String input = eviSetFile;
		String mhsOutput = eviSetFile.replace(".eviset", (pathI + ".mhs")); // (pathI+".mhs") is to avoid multiple
																			// threads using the same file

		File oldMhss = new File(mhsOutput);
		if (oldMhss.delete()) {
			System.out.println("Deleting previous mhs calculation: " + oldMhss.getName());
		}

		String alg = "pmmcs";// uno hitting set optimized
		Process process;

		// executes the mhs enumeration from file
		try {

			process = new ProcessBuilder(program, input, mhsOutput, "-a", alg).start();

		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			mhss = IOHelper.loadMHSsFromFile(mhsOutput);
		} catch (IOException e) {
			e.printStackTrace();

		}

	}

	public Set<BitSet> getAllDCsHybrid() {

		if (dcs == null) {
			dcs = new HashSet<>();

			for (BitSet mhs : mhss) {

//				BitSet dc = mhs2DC(mhs);
//				if (!isTrivialDC(dc))
//					dcs.add(dc);

				dcs.add(mhs);

			}

		}

		return dcs;
	}

	public Set<BitSet> getMinimezedDCs() {

		Set<BitSet> dcs = getAllDCs();

		NTreeSearch nt = new NTreeSearch();
		for (BitSet key : dcs) {
			nt.add(LongBitSet.FACTORY.create(key));
		}

		Set<BitSet> nonGeneralized = new HashSet<>();

		for (BitSet key : dcs) {

			IBitSet bs = LongBitSet.FACTORY.create(key);

			nt.remove(bs);

			if (!nt.containsSubset(bs)) {

				nonGeneralized.add(key);
			}

			nt.add(bs);

		}
		System.out.println("Non generalized DCs=" + nonGeneralized.size());
		dcs = nonGeneralized;

		return dcs;

	}

	private BitSet mhs2DC(BitSet mhs) {

		BitSet bitsetDC = new BitSet();

		for (int predID = mhs.nextSetBit(0); predID >= 0; predID = mhs.nextSetBit(predID + 1)) {

			int idxNegation = predicateSpace.getPredicateById(predID).getPredicateInverseId();

			bitsetDC.set(idxNegation);
		}

		return bitsetDC;
	}

	public Set<BitSet> getFilteredDCs(Set<BitSet> dcs) {

		Set<BitSet> filteredDCs = new HashSet<>();

		for (BitSet dc : dcs) {

			Set<Column> cols = new HashSet<>();

			for (int predID = dc.nextSetBit(0); predID >= 0; predID = dc.nextSetBit(predID + 1)) {

				cols.add(predicateSpace.getPredicateById(predID).getPredicate().getCol1());
			}

			if (dc.cardinality() == cols.size()) {
				filteredDCs.add(dc);
			}

		}

		return filteredDCs;
	}

	public boolean isTrivialDC(BitSet dc) {

		Set<Column> cols = new HashSet<>();

		for (int predID = dc.nextSetBit(0); predID >= 0; predID = dc.nextSetBit(predID + 1)) {

			cols.add(predicateSpace.getPredicateById(predID).getPredicate().getCol1());
		}

		return dc.cardinality() == cols.size() ? false : true;
	}

}
