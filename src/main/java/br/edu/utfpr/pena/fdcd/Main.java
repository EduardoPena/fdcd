package br.edu.utfpr.pena.fdcd;

import br.edu.utfpr.pena.fdcd.mockers.FDCDMocker;
import picocli.CommandLine;

public class Main {

	public static void main(String[] args) {
		int exitCode = new CommandLine(new FDCDMocker()).execute(args);
        System.exit(exitCode);
	}

}
