package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.indexes.comparators;

import java.util.Comparator;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.ColumnPredicateGroup;

public class EqualityIndexSizeComparator implements Comparator<ColumnPredicateGroup> {

	@Override
	public int compare(ColumnPredicateGroup g1, ColumnPredicateGroup g2) {
		return Integer.compare(g1.getEq().getEqualityIndex().getIndex().size(),
				g2.getEq().getEqualityIndex().getIndex().size());
	}

}
