package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups;

import java.util.ArrayList;
import java.util.List;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.IndexedPredicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.RelationalOperator;



public class NumericalColumnPredicateGroup extends ColumnPredicateGroup {

	protected IndexedPredicate lt;
	protected IndexedPredicate lte;
	protected IndexedPredicate gt;
	protected IndexedPredicate gte;

	protected List<IndexedPredicate> rangePredicatesLTGT;

	protected List<IndexedPredicate> rangePredicatesLTEGTE;

	protected boolean binnedLT;

	public NumericalColumnPredicateGroup(IndexedPredicate eq, IndexedPredicate uneq, IndexedPredicate lt,
			IndexedPredicate lte, IndexedPredicate gt, IndexedPredicate gte) {
		super(eq, uneq);

		this.lt = lt;
		this.lte = lte;
		this.gt = gt;
		this.gte = gte;

		this.binnedLT = false;

		this.predicates.add(lt);
		this.predicates.add(lte);
		this.predicates.add(gt);
		this.predicates.add(gte);

		this.rangePredicatesLTGT = new ArrayList<>(2);
		this.rangePredicatesLTGT.add(lt);
//		this.rangePredicates.add(lte);
		this.rangePredicatesLTGT.add(gt);
//		this.rangePredicates.add(gte);

		this.rangePredicatesLTEGTE = new ArrayList<>(2);
		this.rangePredicatesLTEGTE.add(lt);
//		this.rangePredicatesLTEGTE.add(lte);
//		this.rangePredicatesLTEGTE.add(gt);
		this.rangePredicatesLTEGTE.add(gte);
		this.rangePredicatesLTEGTE.add(eq);

//		this.rangePredicatesLTEGTE.add(uneq);

	}

	public List<IndexedPredicate> getRangePredicatesLTGT() {
		return rangePredicatesLTGT;
	}

	public List<IndexedPredicate> getRangePredicatesLTEGTE() {
		return rangePredicatesLTEGTE;
	}

	public IndexedPredicate getLt() {
		return lt;
	}

	public IndexedPredicate getLte() {
		return lte;
	}

	public IndexedPredicate getGt() {
		return gt;
	}

	public IndexedPredicate getGte() {
		return gte;
	}

	public boolean isBinnedLT() {
		return binnedLT;
	}

	public void setBinnedLT(boolean binnedLT) {
		this.binnedLT = binnedLT;
	}

	public IndexedPredicate getPredicate(RelationalOperator op) {

		if (op.equals(RelationalOperator.EQUAL)) {
			return eq;
		} else if (op.equals(RelationalOperator.UNEQUAL)) {
			return uneq;
		} else if (op.equals(RelationalOperator.LESS)) {
			return lt;
		} else if (op.equals(RelationalOperator.LESS_EQUAL)) {
			return lte;
		} else if (op.equals(RelationalOperator.GREATER)) {
			return gt;
		} else if (op.equals(RelationalOperator.GREATER_EQUAL)) {
			return gte;
		} else {
			return null;
		}

	}

	@Override
	public String toString() {
		return "NumColPredGroup [" + predicates + "]";
	}

}
