package br.edu.utfpr.pena.fdcd.algorithms.dc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.Predicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.TupleColumnPairPredicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.TuplePairColumnPairPredicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.TuplePairSingleColumnPredicate;
import br.edu.utfpr.pena.fdcd.input.Table;
import br.edu.utfpr.pena.fdcd.input.columns.Column;
import br.edu.utfpr.pena.fdcd.input.columns.Column.ColumnPrimitiveType;

public class DenialConstraint {

	private List<Predicate> predicates;

	public DenialConstraint() {
		predicates = new ArrayList<Predicate>();
	}

	public DenialConstraint(List<Predicate> predicates) {
		this.predicates = predicates;
	}

	public void addPredicate(Predicate p) {

		predicates.add(p);

	}

	@Override
	public String toString() {

		List<String> predicateListStr = new ArrayList<String>();
		predicates.forEach(p -> predicateListStr.add(p.toString()));

		String dcStr = "not(" + String.join(" and ", predicateListStr) + ")";

		return dcStr;
	}

	public boolean hasSingleTuplePredicate() {

		for (Predicate p : predicates) {
			if (p instanceof TupleColumnPairPredicate) {
				return true;
			}
		}

		return false;
	}

	public DenialConstraint createDCReferencingInput(Table table) {

		DenialConstraint newRefDC = new DenialConstraint();

		for (Predicate p : getPredicateList()) {

			Predicate newRefP = p.createPredicateReferencingInput(table);

			newRefDC.addPredicate(newRefP);

		}

		return newRefDC;

	}

	public List<Predicate> getPredicateList() {
		return predicates;
	}

	public void setPredicateList(List<Predicate> predicateList) {
		this.predicates = predicateList;
	}

	public List<Predicate> getTuplePairPredicates() {

		List<Predicate> tuplePairPredicates = new ArrayList<Predicate>();

		for (Predicate p : predicates) {
			if (p instanceof TuplePairColumnPairPredicate || p instanceof TuplePairSingleColumnPredicate) {

				tuplePairPredicates.add(p);
			}
		}
		return tuplePairPredicates;
	}

	public List<TupleColumnPairPredicate> getTupleColumnPairPredicates() {

		List<TupleColumnPairPredicate> predicatesPriority1 = new ArrayList<>(); // numerical column pair
		List<TupleColumnPairPredicate> predicatesPriority2 = new ArrayList<>(); // string column pair

		for (Predicate p : predicates) {

			if (p instanceof TupleColumnPairPredicate
					&& p.getColumns().iterator().next().ColumnType.equals(ColumnPrimitiveType.STR)) {
				predicatesPriority2.add((TupleColumnPairPredicate) p);
			} else if (p instanceof TupleColumnPairPredicate) {
				predicatesPriority1.add((TupleColumnPairPredicate) p);// numerical first, as it usually has high
																		// selectivity
			}

		}

		List<TupleColumnPairPredicate> predicatesPerPriorities = new ArrayList<>();

		predicatesPerPriorities.addAll(predicatesPriority1);
		predicatesPerPriorities.addAll(predicatesPriority2);

		return predicatesPerPriorities;
	}

	public Set<Predicate> getCategoricalColumnPairPredicates() {

		Set<Predicate> catColumnPairPredicates = new HashSet<>(); // string column pair

		for (Predicate p : predicates) {

			if ((p instanceof TupleColumnPairPredicate || p instanceof TuplePairColumnPairPredicate)
					&& p.getColumns().iterator().next().ColumnType.equals(ColumnPrimitiveType.STR)) {

				catColumnPairPredicates.add(p);
			}
		}

		return catColumnPairPredicates;
	}

	public Set<Column> getRequiredColumns() {

		Set<Column> cols = new HashSet<>();

		predicates.forEach(p -> cols.addAll(p.getColumns()));

		return cols;
	}

	public int[] getRequiredColIdxs() {

		Set<Integer> colsIdxs = new TreeSet<>();

		getRequiredColumns().forEach(c -> colsIdxs.add(c.ColumnIndex));

		int requiredColIdxs[] = new int[colsIdxs.size()];

		Iterator<Integer> it = colsIdxs.iterator();
		int i = 0;
		while (it.hasNext()) {
			requiredColIdxs[i] = it.next();
		}

		return requiredColIdxs;
	}

	public int size() {

		return getPredicateList().size();
	}

}
