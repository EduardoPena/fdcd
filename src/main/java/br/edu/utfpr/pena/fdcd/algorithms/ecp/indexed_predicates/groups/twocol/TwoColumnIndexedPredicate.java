package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.twocol;

import java.util.BitSet;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.IndexedPredicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.Predicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.RelationalOperator;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.TwoColumnPredicateSpace;
import br.edu.utfpr.pena.fdcd.input.columns.CategoricalColumn;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;

// Attention: to simplify coding, we use two IndexedPredicate to represent a twoColumn predicat
// for example: the pair of predicates lhs:t1.A=t2.A and lhs:t1.B=t2.B becomes a TwoColumnPredicate t1.A=t2.B, if singleTuple=True then the predicate is t1.A=t1.B

public class TwoColumnIndexedPredicate extends IndexedPredicate {

	private TwoColumnPredicateSpace twoColspace;

	private IndexedPredicate lhs; // we use the operator on the lhs to set the operator on this predicate

	private RelationalOperator op;
	private IndexedPredicate rhs;
	private boolean isSingleTuple;

	private Float2ObjectMap<String> lhsColumnDictionary;
	private Float2ObjectMap<String> rhsColumnDictionary;

	private BitSet eqCategoricalMask; // categorical

	public TwoColumnIndexedPredicate(Predicate predicate, IndexedPredicate lhs, IndexedPredicate rhs,
			boolean isSingleTuple, TwoColumnPredicateSpace twoColspace, boolean isNumerical) {
		super(twoColspace.getOneColspace(), predicate, isNumerical);
		this.twoColspace = twoColspace;
		this.lhs = lhs;
		this.op = lhs.getPredicate().getOp();
		this.rhs = rhs;
		this.isSingleTuple = isSingleTuple;

		if (!isNumerical) {

			CategoricalColumn lhsCol = (CategoricalColumn) lhs.getPredicate().getCol1();
			lhsColumnDictionary = lhsCol.buildReverseDictionaryMap();

			CategoricalColumn rhsCol = (CategoricalColumn) rhs.getPredicate().getCol1();
			rhsColumnDictionary = rhsCol.buildReverseDictionaryMap();

		}
	}

	public IndexedPredicate getLhs() {
		return lhs;
	}

	public RelationalOperator getOp() {
		return op;
	}

	public IndexedPredicate getRhs() {
		return rhs;
	}

	public boolean isSingleTuple() {
		return isSingleTuple;
	}

	public BitSet getEqCategoricalMask() {
		return eqCategoricalMask;
	}

	public void setEqCategoricalMask(BitSet eqCategoricalMask) {
		this.eqCategoricalMask = eqCategoricalMask;
	}

	public void setEqCategoricalMask(int eqPredID, int uneqPredID) {
		this.eqCategoricalMask = new BitSet();
		this.eqCategoricalMask.set(eqPredID);
		this.eqCategoricalMask.set(uneqPredID);

	}

	@Override
	public String toString() {
		if (isSingleTuple)
			return "(" + predicateId + ")" + "t1." + lhs.getPredicate().getCol1() + op.getShortString() + "t1."
					+ rhs.getPredicate().getCol1();
		else
			return "(" + predicateId + ")" + "t1." + lhs.getPredicate().getCol1() + op.getShortString() + "t2."
					+ rhs.getPredicate().getCol1();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isSingleTuple ? 1231 : 1237);
		result = prime * result + ((lhs == null) ? 0 : lhs.hashCode());
		result = prime * result + ((op == null) ? 0 : op.hashCode());
		result = prime * result + ((rhs == null) ? 0 : rhs.hashCode());
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
		TwoColumnIndexedPredicate other = (TwoColumnIndexedPredicate) obj;
		if (isSingleTuple != other.isSingleTuple)
			return false;
		if (lhs == null) {
			if (other.lhs != null)
				return false;
		} else if (!lhs.equals(other.lhs))
			return false;
		if (op != other.op)
			return false;
		if (rhs == null) {
			if (other.rhs != null)
				return false;
		} else if (!rhs.equals(other.rhs))
			return false;
		return true;
	}

	public boolean eval(int t1, int t2) {
		if (isNumerical())
			return evalNum(t1, t2);
		else
			return evalCat(t1, t2);

	}

	private boolean evalNum(int t1, int t2) {
		if (isSingleTuple) {

			Float c1Value = lhs.getPredicate().getCol1().getValuesList().getFloat(t1);

			Float c2Value = rhs.getPredicate().getCol1().getValuesList().getFloat(t1);

			return op.eval(c1Value, c2Value);
		} else {

			Float c1Value = lhs.getPredicate().getCol1().getValuesList().getFloat(t1);

			Float c2Value = rhs.getPredicate().getCol1().getValuesList().getFloat(t2);

			return op.eval(c1Value, c2Value);
		}
	}

	public boolean evalCat(int t1, int t2) {

		if (isSingleTuple) {

			String c1Value = lhsColumnDictionary.get(lhs.getPredicate().getCol1().getValuesList().getFloat(t1));

			String c2Value = rhsColumnDictionary.get(rhs.getPredicate().getCol1().getValuesList().getFloat(t1));

			return op.eval(c1Value, c2Value);
		} else {

			String c1Value = lhsColumnDictionary.get(lhs.getPredicate().getCol1().getValuesList().getFloat(t1));

			String c2Value = rhsColumnDictionary.get(rhs.getPredicate().getCol1().getValuesList().getFloat(t2));

			return op.eval(c1Value, c2Value);
		}

	}

}
