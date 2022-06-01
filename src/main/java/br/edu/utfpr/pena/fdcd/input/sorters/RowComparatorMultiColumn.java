package br.edu.utfpr.pena.fdcd.input.sorters;

import java.util.Comparator;
import java.util.List;

import br.edu.utfpr.pena.fdcd.input.columns.NumericalColumn;



public class RowComparatorMultiColumn implements Comparator<Integer> {

	private List<NumericalColumn> numCols;
	

	public RowComparatorMultiColumn(List<NumericalColumn> numCols) {
		super();
		this.numCols = numCols;
		
	}

	@Override
	public int compare(Integer i1, Integer i2) {

		int iCol = 0;
		NumericalColumn col = numCols.get(iCol);

		while (col.getValueAt(i1) == col.getValueAt(i2) && iCol < numCols.size()-1) {
			col = numCols.get(++iCol);
		}
		

		return Float.compare(col.getValueAt(i1), col.getValueAt(i2));

	}

}
