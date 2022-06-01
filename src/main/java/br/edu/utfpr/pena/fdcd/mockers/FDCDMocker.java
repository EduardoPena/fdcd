package br.edu.utfpr.pena.fdcd.mockers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.BitSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.evidence.EvidenceSet;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.evidence.context.TupleContextBuilder;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.evidence.context.twocol.TwoColTupleContextBuilder;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.PredicateSpace;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.TwoColumnPredicateSpace;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.builder.PredicateSpaceBuilder;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.builder.TwoColumnPredicateSpaceBuilder;
import br.edu.utfpr.pena.fdcd.algorithms.enumeration.DCEnumeration;
import br.edu.utfpr.pena.fdcd.algorithms.enumeration.hydra.EvidenceInversion;
import br.edu.utfpr.pena.fdcd.algorithms.enumeration.hydra.TwoColEvidenceInversion;
import br.edu.utfpr.pena.fdcd.algorithms.enumeration.incs.INCS;
import br.edu.utfpr.pena.fdcd.algorithms.enumeration.incs.twocol.TwoColINCS;
import br.edu.utfpr.pena.fdcd.algorithms.enumeration.mmcs.ExternalMMCS;
import br.edu.utfpr.pena.fdcd.input.Table;
import br.edu.utfpr.pena.fdcd.input.reader.CSVInput;
import br.edu.utfpr.pena.fdcd.utils.io.IOHelper;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class FDCDMocker implements Callable<Integer> {

	protected static Logger log = LoggerFactory.getLogger(FDCDMocker.class);

	@Parameters(index = "0", description = "The input file.")
	String inputFilePath;

	@Option(names = { "-n", "--lines" }, description = "The number of rows to read.")
	private int numRecords;

	@Option(names = { "-o", "--output" }, description = "The input file.")
	String outputFilePath;

	@Option(names = { "-e", "--enumeration" }, description = "DC enumeration algorithm: INCS, EI, HEI, MMCS, HMMCS ")
	String dcEnumAlgorithm;

	@Option(names = { "-t", "--twocol" }, description = "Enable cross column predicates or not.")
	boolean twoCols;

	enum DCEnumerationAlgorithm {
		INCS, EI, HEI, MMCS, HMMCS, MCS
	}

	@Override
	public Integer call() throws Exception {

		CSVInput input;
		if (numRecords > 0)
			input = new CSVInput(inputFilePath, numRecords); // read rows limited to numRecords
		else
			input = new CSVInput(inputFilePath); // read all rows

		DCEnumerationAlgorithm enumAlg = getEnumerationAlgorithm(dcEnumAlgorithm, twoCols);

		if (!twoCols) {

			Table table = input.getTable();
			PredicateSpace predicateSpace = null;
			EvidenceSet evidenceSet = null;
			Set<BitSet> dcs = null;

			log.info("Building predicate space...");
			predicateSpace = new PredicateSpaceBuilder().build(table);
			log.info("Number of predicates: " + predicateSpace.size());

			log.info("Building evidence set...");
			TupleContextBuilder evidenceSetBuilder = new TupleContextBuilder(table, predicateSpace);
			evidenceSet = evidenceSetBuilder.build();

			log.info("Enumerating DCs from the evidence set using " + enumAlg + " algorithm...");
			DCEnumeration search = null;

			if (enumAlg == DCEnumerationAlgorithm.INCS) {

				search = new INCS(predicateSpace, evidenceSet);
				dcs = search.searchDCs();

			} else if (enumAlg == DCEnumerationAlgorithm.EI) {

				search = new EvidenceInversion(predicateSpace, evidenceSet);
				dcs = search.searchDCs();

			} else if (enumAlg == DCEnumerationAlgorithm.HEI) {

				search = new EvidenceInversion(predicateSpace, evidenceSet);
				dcs = search.searchDCs();

			} else if (enumAlg == DCEnumerationAlgorithm.MMCS) {

				search = new ExternalMMCS(table, predicateSpace, evidenceSet);
				dcs = search.searchDCs();
				IOHelper.cleanDirectoryForMMCS();

			} else if (enumAlg == DCEnumerationAlgorithm.HMMCS) {

				search = new ExternalMMCS(table, predicateSpace, evidenceSet);
				dcs = search.searchDCs();
				IOHelper.cleanDirectoryForMMCS();

			} else {
				search = new INCS(predicateSpace, evidenceSet);
				dcs = search.searchDCs();
			}

			log.info("Number of DCs discovered: " + dcs.size());

			if (outputFilePath != null) {

				log.info("Saving DCs into: " + outputFilePath);
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
				for (BitSet dc : dcs) {
					writer.write(predicateSpace.toDCstring(dc) + System.lineSeparator());
				}
				writer.close();
			}

		} else {

			Table table = input.getTable();
			TwoColumnPredicateSpace predicateSpace = null;
			EvidenceSet evidenceSet = null;
			Set<BitSet> dcs = null;

			log.info("Building two col predicate space...");
			predicateSpace = new TwoColumnPredicateSpaceBuilder().build(table);
			log.info("Number of predicates: " + predicateSpace.size());

			log.info("Building evidence set...");
			TwoColTupleContextBuilder evidenceSetBuilder = new TwoColTupleContextBuilder(table, predicateSpace);
			evidenceSet = evidenceSetBuilder.build();

			if (enumAlg != DCEnumerationAlgorithm.EI && enumAlg != DCEnumerationAlgorithm.INCS)
				enumAlg = DCEnumerationAlgorithm.EI;

			log.info("Enumerating DCs from the evidence set using " + enumAlg + " algorithm...");
			DCEnumeration search = null;

			if (enumAlg == DCEnumerationAlgorithm.INCS) {

				search = new TwoColINCS(predicateSpace, evidenceSet);
				dcs = search.searchDCs();

			} else if (enumAlg == DCEnumerationAlgorithm.EI) {

				search = new TwoColEvidenceInversion(predicateSpace, evidenceSet);
				dcs = search.searchDCs();

			}

			log.info("Number of DCs discovered: " + dcs.size());

			if (outputFilePath != null) {

				log.info("Saving DCs into: " + outputFilePath);
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
				for (BitSet dc : dcs) {
					writer.write(predicateSpace.toDCstring(dc) + System.lineSeparator());
				}
				writer.close();
			}

		}

		return 0;
	}

	private DCEnumerationAlgorithm getEnumerationAlgorithm(String dcEnumAlgorithm, boolean twoCols) {

		if (dcEnumAlgorithm == null) {
			return DCEnumerationAlgorithm.INCS;
		} else {
			dcEnumAlgorithm = dcEnumAlgorithm.toUpperCase();
			for (DCEnumerationAlgorithm alg : DCEnumerationAlgorithm.values()) {
				if (alg.name().equals(dcEnumAlgorithm)) {
					return alg;
				}
			}
			log.info("Enumeration algorithm not supported, using INCS as default. ");
			return DCEnumerationAlgorithm.INCS;
		}
	}

}
