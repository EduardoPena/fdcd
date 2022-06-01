package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.twocol;

import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class TwoColumnNumericalPredicateGroup extends TwoColumnPredicateGroup {

	protected Set<TwoColumnIndexedPredicate> twoColPredicates;

	protected TwoColumnIndexedPredicate twoColLt;
	protected TwoColumnIndexedPredicate twoColLte;
	protected TwoColumnIndexedPredicate twoColGt;
	protected TwoColumnIndexedPredicate twoColGte;

	// for single tuple predicates
	protected BitSet twoColEqTrueMask;
	protected BitSet twoColLtTrueMask;
	protected BitSet twoColGtTrueMask;

	// for two tuple predicates

	protected BitSet twoColTwoTupCorrectionMaskForEq;
	protected BitSet twoColTwoTupCorrectionMaskForLT;

	protected boolean isRhsBinned = false;

	public TwoColumnNumericalPredicateGroup(TwoColumnIndexedPredicate twoColEq, TwoColumnIndexedPredicate twoColUneq,
			TwoColumnIndexedPredicate twoColLt, TwoColumnIndexedPredicate twoColLte, TwoColumnIndexedPredicate twoColGt,
			TwoColumnIndexedPredicate twoColGte, boolean singleColumn) {
		super(twoColEq, twoColUneq, singleColumn);

		this.col1 = twoColEq.getLhs().getPredicate().getCol1();
		this.col2 = twoColEq.getRhs().getPredicate().getCol1();
		this.isSingleTuple = singleColumn;

		this.twoColEq = twoColEq;
		this.twoColUneq = twoColUneq;

		this.twoColLt = twoColLt;
		this.twoColLte = twoColLte;
		this.twoColGt = twoColGt;
		this.twoColGte = twoColGte;

		this.pids.add(twoColLt.getPredicateId());
		this.pids.add(twoColLte.getPredicateId());
		this.pids.add(twoColGt.getPredicateId());
		this.pids.add(twoColGte.getPredicateId());

		this.twoColPredicates = new LinkedHashSet<>();
		this.twoColPredicates.add(twoColEq);
		this.twoColPredicates.add(twoColUneq);
		this.twoColPredicates.add(twoColLt);
		this.twoColPredicates.add(twoColLte);
		this.twoColPredicates.add(twoColGt);
		this.twoColPredicates.add(twoColGte);

		twoColEqTrueMask = new BitSet();
		twoColEqTrueMask.set(twoColEq.getPredicateId());
		twoColEqTrueMask.set(twoColLte.getPredicateId());
		twoColEqTrueMask.set(twoColGte.getPredicateId());

		twoColLtTrueMask = new BitSet();
		twoColLtTrueMask.set(twoColLt.getPredicateId());
		twoColLtTrueMask.set(twoColLte.getPredicateId());
		twoColLtTrueMask.set(twoColUneq.getPredicateId());

		twoColGtTrueMask = new BitSet();
		twoColGtTrueMask.set(twoColGt.getPredicateId());
		twoColGtTrueMask.set(twoColGte.getPredicateId());
		twoColGtTrueMask.set(twoColUneq.getPredicateId());

		// here is how to correct for two tuple two col predicates based on EQ
		twoColTwoTupCorrectionMaskForEq = new BitSet();
		twoColTwoTupCorrectionMaskForEq.set(twoColEq.getPredicateId()); // include equality
		twoColTwoTupCorrectionMaskForEq.set(twoColUneq.getPredicateId());// remove unequal
		// twoColTwoTupCorrectionMaskForEq.set(twoColLt.getPredicateId());// no need to
		// include lt
		twoColTwoTupCorrectionMaskForEq.set(twoColLte.getPredicateId()); // include lte
		twoColTwoTupCorrectionMaskForEq.set(twoColGt.getPredicateId()); // remove gt
		// twoColTwoTupCorrectionMaskForEq.set(twoColGte.getPredicateId());// gte is
		// already there

		// here is how to correct for two tuple two col predicates based on LT
		twoColTwoTupCorrectionMaskForLT = new BitSet();
		twoColTwoTupCorrectionMaskForLT.set(twoColLt.getPredicateId()); // add
		twoColTwoTupCorrectionMaskForLT.set(twoColLte.getPredicateId()); // add
		twoColTwoTupCorrectionMaskForLT.set(twoColGt.getPredicateId()); // remove
		twoColTwoTupCorrectionMaskForLT.set(twoColGte.getPredicateId()); // remove

	}

	public Set<TwoColumnIndexedPredicate> getTwoColPredicates() {
		return twoColPredicates;
	}

	@Override
	public String toString() {
		if (isSingleTuple)
			return "TwoColNumericalOneTupGroup [ " + twoColPredicates + "]";
		else
			return "TwoColNumericalTwoTupGroup [" + twoColPredicates + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((col1 == null) ? 0 : col1.hashCode());
		result = prime * result + ((col2 == null) ? 0 : col2.hashCode());
		result = prime * result + (isSingleTuple ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj)

			return true;

		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TwoColumnNumericalPredicateGroup other = (TwoColumnNumericalPredicateGroup) obj;
		if (col1 == null) {
			if (other.col1 != null)
				return false;
		} else if (!col1.equals(other.col1))
			return false;
		if (col2 == null) {
			if (other.col2 != null)
				return false;
		} else if (!col2.equals(other.col2))
			return false;
		if (isSingleTuple != other.isSingleTuple)
			return false;

		return true;
	}

	public TwoColumnIndexedPredicate getTwoColLt() {
		return twoColLt;
	}

	public TwoColumnIndexedPredicate getTwoColLte() {
		return twoColLte;
	}

	public TwoColumnIndexedPredicate getTwoColGt() {
		return twoColGt;
	}

	public TwoColumnIndexedPredicate getTwoColGte() {
		return twoColGte;
	}

	public BitSet getTwoColEqTrueMask() {
		return twoColEqTrueMask;
	}

	public BitSet getTwoColLtTrueMask() {
		return twoColLtTrueMask;
	}

	public BitSet getTwoColGtTrueMask() {
		return twoColGtTrueMask;
	}

	public boolean isRhsBinned() {
		return isRhsBinned;
	}

	public void setRhsBinned(boolean isRhsBinned) {
		this.isRhsBinned = isRhsBinned;
	}

	public BitSet getTwoColTwoTupCorrectionMaskForEq() {
		return twoColTwoTupCorrectionMaskForEq;
	}

	public BitSet getTwoColTwoTupCorrectionMaskForLT() {
		return twoColTwoTupCorrectionMaskForLT;
	}

}
