package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.twocol;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.ColumnPredicateGroup;
import br.edu.utfpr.pena.fdcd.input.columns.Column;



public class TwoColumnPredicateGroup extends ColumnPredicateGroup {

	protected Set<TwoColumnIndexedPredicate> twoColPredicates;

	protected TwoColumnIndexedPredicate twoColEq;
	protected TwoColumnIndexedPredicate twoColUneq;

	protected Column col1;
	protected Column col2;
	protected boolean isSingleTuple;

	protected Set<Integer> pids;

	public TwoColumnPredicateGroup(TwoColumnIndexedPredicate eq, TwoColumnIndexedPredicate uneq,
			boolean isSingleTuple) {
		super(eq.getLhs(), uneq.getLhs());

		this.col1 = eq.getLhs().getPredicate().getCol1();
		this.col2 = eq.getRhs().getPredicate().getCol1();

		this.isSingleTuple = isSingleTuple;

		this.twoColEq = eq;
		this.twoColUneq = uneq;

		this.twoColPredicates = new LinkedHashSet<>();
		this.twoColPredicates.add(eq);
		this.twoColPredicates.add(uneq);

		this.pids = new HashSet<>();
		this.pids.add(eq.getPredicateId());
		this.pids.add(uneq.getPredicateId());

	}

	public Set<TwoColumnIndexedPredicate> getTwoColPredicates() {
		return twoColPredicates;
	}

	@Override
	public String toString() {
		if (isSingleTuple)
			return "TwoColCatOneTupGroup [ " + twoColPredicates + "]";
		else
			return "TwoColCatTwoTupGroup [" + twoColPredicates + "]";
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
		TwoColumnPredicateGroup other = (TwoColumnPredicateGroup) obj;
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

	public TwoColumnIndexedPredicate getTwoColEq() {
		return twoColEq;
	}

	public TwoColumnIndexedPredicate getTwoColUneq() {
		return twoColUneq;
	}

	public Column getCol1() {
		return col1;
	}

	public Column getCol2() {
		return col2;
	}

	public Set<Integer> getPids() {
		return pids;
	}

	public void setPids(Set<Integer> pids) {
		this.pids = pids;
	}

	public void setTwoColEq(TwoColumnIndexedPredicate twoColEq) {
		this.twoColEq = twoColEq;
	}

}
