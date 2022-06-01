package br.edu.utfpr.pena.fdcd.input.sorters;

import java.util.Comparator;

import br.edu.utfpr.pena.fdcd.input.columns.Column;



public class ColumnComparatorByCardinality implements Comparator<Column> {

	@Override
	public int compare(Column g1, Column g2) {
		return Long.compare(g1.getCardinality(),
				g2.getCardinality());
	}

}
