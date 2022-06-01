package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.indexes.comparators;

import java.util.Comparator;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.IndexedPredicate;

public class EqualityIndexSizePredicateComparator implements Comparator<IndexedPredicate> {

	@Override
	public int compare(IndexedPredicate p1, IndexedPredicate p2) {
		return Integer.compare(p1.getEqualityIndex().getIndex().size(), p2.getEqualityIndex().getIndex().size());
	}

}
