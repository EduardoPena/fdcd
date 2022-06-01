package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.IndexedPredicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.RelationalOperator;
import br.edu.utfpr.pena.fdcd.input.columns.Column;

public class PredicateProvider {

	// singleton provider
	private static PredicateProvider instance;

	private PredicateSpace space;

	private PredicateProvider(PredicateSpace space) {
		this.space = space;
	}

	public IndexedPredicate getPredicateById(int pid) {

		return space.getPredicateById(pid);
	}

	public static void initStaticInstance(PredicateSpace space) {
		instance = new PredicateProvider(space);
	}

	public static PredicateProvider getInstance() {
		return instance;
	}

	public IndexedPredicate getPredicate(RelationalOperator op, Column col1) {

		return space.getColumn2GroupMap().get(col1).getPredicate(op);
	}

}
