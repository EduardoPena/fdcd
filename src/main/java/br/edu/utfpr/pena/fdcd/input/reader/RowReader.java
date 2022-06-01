package br.edu.utfpr.pena.fdcd.input.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.simpleflatmapper.csv.CsvParser;
import org.simpleflatmapper.util.CloseableIterator;

import br.edu.utfpr.pena.fdcd.input.Table;
import br.edu.utfpr.pena.fdcd.input.columns.Column;



/**
 * Reads a dataset in CSV format and returns an Object Table as a in-memory
 * representationof the dataset.
 * 
 */

public class RowReader {

	public static final int STRING_BUFFER_SIZE = 10000; // get from confs

	public static void readRows(Table table, Set<Column> requiredColumns) throws IOException {

		int nCols = 0;
		int nRecords = 0;
		int idxCols[] = getColIdxs(requiredColumns);

		Column[] columns = new Column[table.NUM_ORIGINAL_COLLUMNS];
		requiredColumns.forEach(c -> {

			columns[c.ColumnIndex] = c;
		});

		try (CloseableIterator<String[]> it = CsvParser.iterator(table.DATASETFILE)) {

			if (it.hasNext()) {
				String[] headers = it.next();
				nCols = headers.length;

			} else {
				throw new IOException("I could not parse the " + table.NAME + "data file");
			}

			ArrayList<String>[] stringBuffer = new ArrayList[nCols];

			for (int i : idxCols) {
				stringBuffer[i] = new ArrayList<String>(STRING_BUFFER_SIZE);
			}

			while (it.hasNext()) {

				String[] values = it.next();
				++nRecords;

				for (int i : idxCols) {
					stringBuffer[i].add(values[i]);
				}

				if (stringBuffer[idxCols[0]].size() == STRING_BUFFER_SIZE) {// if the first buffer in use is full

					for (int i : idxCols) {
						columns[i].addValues(stringBuffer[i]);
					}

					for (int i : idxCols) {
						stringBuffer[i] = new ArrayList<String>(STRING_BUFFER_SIZE);
					}
				}
			}

			if (stringBuffer[idxCols[0]].size() > 0) {// if the first buffer in use is full

				for (int i : idxCols) {
					columns[i].addValues(stringBuffer[i]);
				}
			}
		}

		table.setNUM_RECORDS(nRecords);

	}

	public static void readRows(Table table, Set<Column> requiredColumns, int maximumRows) throws IOException {

		int nCols = 0;
		int nRecords = 0;
		int idxCols[] = getColIdxs(requiredColumns);

		Column[] columns = new Column[table.NUM_ORIGINAL_COLLUMNS];
		requiredColumns.forEach(c -> {

			columns[c.ColumnIndex] = c;
		});

		try (CloseableIterator<String[]> it = CsvParser.iterator(table.DATASETFILE)) {

			if (it.hasNext()) {
				String[] headers = it.next();
				nCols = headers.length;

			} else {
				throw new IOException("I could not parse the " + table.NAME + "data file");
			}

			ArrayList<String>[] stringBuffer = new ArrayList[nCols];

			for (int i : idxCols) {
				stringBuffer[i] = new ArrayList<String>(STRING_BUFFER_SIZE);
			}

			while (it.hasNext()) {

				String[] values = it.next();
				++nRecords;

				for (int i : idxCols) {
					stringBuffer[i].add(values[i]);
				}

				if (stringBuffer[idxCols[0]].size() == STRING_BUFFER_SIZE) {// if the first buffer in use is full

					for (int i : idxCols) {
						columns[i].addValues(stringBuffer[i]);
					}

					for (int i : idxCols) {
						stringBuffer[i] = new ArrayList<String>(STRING_BUFFER_SIZE);
					}
				}

				if (nRecords >= maximumRows) {
					break;
				}

			}

			if (stringBuffer[idxCols[0]].size() > 0) {// if the first buffer in use is full

				for (int i : idxCols) {
					columns[i].addValues(stringBuffer[i]);
				}
			}
		}

		table.setNUM_RECORDS(nRecords);

	}

	private static int[] getColIdxs(Set<Column> requiredColumns) {

		int idxCols[] = new int[requiredColumns.size()];

		Iterator<Column> colIt = requiredColumns.iterator();

		int i = 0;
		while (colIt.hasNext()) {
			idxCols[i] = colIt.next().ColumnIndex;
			i++;
		}

		Arrays.sort(idxCols);

		return idxCols;
	}

}
