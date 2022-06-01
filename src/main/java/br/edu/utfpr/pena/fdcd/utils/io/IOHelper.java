package br.edu.utfpr.pena.fdcd.utils.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.evidence.EvidenceSet;

public class IOHelper {

	public static void saveEvidenceSet2File(EvidenceSet evidenceSet, String fileName) throws IOException {

		FileWriter fileWriter = new FileWriter(fileName);
		PrintWriter printWriter = new PrintWriter(fileWriter);

		for (BitSet s : evidenceSet.getEvidence2CountMap().keySet()) {

			StringBuilder str = new StringBuilder(s.toString().replace(",", ""));
			str.deleteCharAt(0);
			str.deleteCharAt(str.length() - 1);

			printWriter.print(str + "\n");
		}

		printWriter.close();

	}

	public static void saveEvidenceSet2File(Set<BitSet> bitEviSet, String fileName) throws IOException {

		FileWriter fileWriter = new FileWriter(fileName);
		PrintWriter printWriter = new PrintWriter(fileWriter);

		for (BitSet s : bitEviSet) {

			StringBuilder str = new StringBuilder(s.toString().replace(",", ""));
			str.deleteCharAt(0);
			str.deleteCharAt(str.length() - 1);

			printWriter.print(str + "\n");
		}

		printWriter.close();

	}

	public static Set<BitSet> loadMHSsFromFile(String mhsOutputFile) throws IOException {

		Set<BitSet> bitSets = new HashSet<>();

		File file = new File(mhsOutputFile);

		if (!file.exists()) // in case it does not find any DCs
			return bitSets;

		BufferedReader br = new BufferedReader(new FileReader(file));

		
		
		
		String st;
		while ((st = br.readLine()) != null) {

			BitSet bs = new BitSet();
			for (String p : st.split(" ")) {

				bs.set(Integer.parseInt(p));

			}

			bitSets.add(bs);

		}
		
		

		return bitSets;
	}

	public static void cleanDirectoryForMMCS() {
		File folder = new File(System.getProperty("user.dir"));
		Arrays.stream(folder.listFiles()).filter(f -> f.getName().endsWith(".mhs") || f.getName().endsWith(".eviset"))
				.forEach(File::delete);

	}

}
