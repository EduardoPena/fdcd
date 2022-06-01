package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.indexes.EqualityIndex;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.indexes.LtBinnedIndex;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.indexes.LtIndex;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.Predicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.RelationalOperator;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.TupleColumnPairPredicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.TuplePairColumnPairPredicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.TuplePairSingleColumnPredicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.PredicateSpace;

public class IndexedPredicate implements Comparable {

	private final PredicateSpace pspace;

	private Predicate predicate;
	private boolean isNumerical;

	protected int predicateId;
	private int predicateInverseId;
	private BitSet implicationIdSet;
	private int predicateSymmetricId;

	private List<IndexedPredicate> implications;
	private IndexedPredicate inverse;
	private IndexedPredicate symmetric;

	private EqualityIndex equalityIndex;
	private LtIndex ltIndex;
	private LtBinnedIndex ltBinnedIndex;

	// Each mask is built for "xoring" evidence

	private BitSet eqCategoricalMask; // categorical

	private BitSet eqRangeMaskLt;// if we assumed evidence with GT and GTE, then we use this mask (use for
									// correcting range eq)

	private BitSet eqRangeMaskGt;// if we assumed evidence with LT and LTE, then we use this mask (use for
									// correcting range eq)

	private BitSet rangeMask;// this mask will remove what was assumed (GT or LT), and include the (opposite)
								// correct predicates

	private int predGroupID; // attention, only used for DFS search -> do not use it anywhere else

	public IndexedPredicate(PredicateSpace pspace, Predicate predicate, boolean isNumerical) {
		this.pspace = pspace;
		this.predicate = predicate;
		this.isNumerical = isNumerical;
		this.implicationIdSet = new BitSet();
	}

	public void buildCategoricalMask(int eqIdx, int uneqIdx) {

		eqCategoricalMask = new BitSet(); // include both EQ and UNEQ as a xor operation is able to correct the evidence
		eqCategoricalMask.set(eqIdx);
		eqCategoricalMask.set(uneqIdx);
	}

	public void buildNumericalEqRangeMask(int eqIdx, int uneqIdx, int ltIdx, int lteIdx, int gtIdx, int gteIdx) {

		// LT
		eqRangeMaskLt = new BitSet();
		eqRangeMaskLt.set(eqIdx); // include equality
		eqRangeMaskLt.set(uneqIdx);// remove unequal
		// eqRangeMaskLt.set(ltIdx);// no need to include lt
		eqRangeMaskLt.set(lteIdx); // include lte
		eqRangeMaskLt.set(gtIdx); // remove gt
		// eqCategoricalMask.set(gteIdx);// gte is already there

		// GT
		eqRangeMaskGt = new BitSet();
		eqRangeMaskGt.set(eqIdx); // include equality
		eqRangeMaskGt.set(uneqIdx);// remove unequal
		eqRangeMaskGt.set(ltIdx);// remove lt
		// eqRangeMaskGt.set(lteIdx); // lte is already there
		// eqRangeMaskGt.set(gtIdx); // no need to include gte
		eqRangeMaskGt.set(gteIdx);// include gte

	}

	public void buildNumericalRangeRangeMask(int ltIdx, int lteIdx, int gtIdx, int gteIdx) {

		// this mask will remove what was assumed, and include the correction
		rangeMask = new BitSet();
		rangeMask.set(ltIdx);
		rangeMask.set(lteIdx);
		rangeMask.set(gtIdx);
		rangeMask.set(gteIdx);
	}

	public void setPredicateSymmetricId(int predicateSymmetricId) {
		this.predicateSymmetricId = predicateSymmetricId;
	}

	public int getPredicateSymmetricId() {
		return predicateSymmetricId;
	}

	public void setImplicationIdSet(int... predIds) {

		for (int predId : predIds) {

			implicationIdSet.set(predId);
		}

	}

	public void setImplicationIdSet(BitSet implicationIdSet) {
		this.implicationIdSet = implicationIdSet;
	}

	public BitSet getImplicationIdSet() {
		return implicationIdSet;
	}

	public EqualityIndex getEqualityIndex() {
		return equalityIndex;
	}

	public void setEqualityIndex(EqualityIndex equalityIndex) {
		this.equalityIndex = equalityIndex;
	}

	public LtIndex getLtIndex() {
		return ltIndex;
	}

	public void setLtIndex(LtIndex ltIndex) {
		this.ltIndex = ltIndex;
	}

	public LtBinnedIndex getLtBinnedIndex() {
		return ltBinnedIndex;
	}

	public void setLtBinnedIndex(LtBinnedIndex ltBinnedIndex) {
		this.ltBinnedIndex = ltBinnedIndex;
	}

	public Predicate getPredicate() {
		return predicate;
	}

	public boolean isNumerical() {
		return isNumerical;
	}

	public int getPredicateId() {
		return predicateId;
	}

	public int getPredGroupID() {
		return predGroupID;
	}

	public void setPredGroupID(int predGroupID) {
		this.predGroupID = predGroupID;
	}

	public void setPredicateId(int predicateId) {
		this.predicateId = predicateId;
	}

	public int getPredicateInverseId() {
		return predicateInverseId;
	}

	public void setPredicateInverseId(int predicateInverseId) {
		this.predicateInverseId = predicateInverseId;
	}

	public BitSet getEqCategoricalMask() {
		return eqCategoricalMask;
	}

	public void setEqCategoricalMask(BitSet eqCategoricalMask) {
		this.eqCategoricalMask = eqCategoricalMask;
	}

	public BitSet getEqRangeMaskLt() {
		return eqRangeMaskLt;
	}

	public void setEqRangeMaskLt(BitSet eqRangeMaskLt) {
		this.eqRangeMaskLt = eqRangeMaskLt;
	}

	public BitSet getEqRangeMaskGt() {
		return eqRangeMaskGt;
	}

	public void setEqRangeMaskGt(BitSet eqRangeMaskGt) {
		this.eqRangeMaskGt = eqRangeMaskGt;
	}

	public BitSet getRangeMask() {
		return rangeMask;
	}

	public void setRangeMask(BitSet rangeMask) {
		this.rangeMask = rangeMask;
	}

	public IndexedPredicate getInverse() {
		if (inverse == null) {
			inverse = pspace.getPredicateById(predicateInverseId);
		}
		return inverse;
	}

	public IndexedPredicate getSymmetric() {
		if (symmetric == null) {
			symmetric = pspace.getPredicateById(predicateSymmetricId);
		}
		return symmetric;
	}

	public List<IndexedPredicate> getImplications() {

		if (implications == null) {

			implications = new ArrayList<>();

			for (int pid = implicationIdSet.nextSetBit(0); pid >= 0; pid = implicationIdSet.nextSetBit(pid + 1)) {

				implications.add(pspace.getPredicateById(pid));
			}

		}

		return implications;
	}

	// extend for more types of predicates
	public boolean hasEquivalentOperands(IndexedPredicate p2) {

		if (predicate.getClass().equals(p2.getPredicate().getClass())
				&& predicate.getColumns().equals(p2.getPredicate().getColumns())) {

			return true;

		} else {
			return false;
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IndexedPredicate other = (IndexedPredicate) obj;
		if (predicate == null) {
			if (other.predicate != null)
				return false;
		} else if (!predicate.equals(other.predicate))
			return false;
		return true;
	}

	@Override
	public String toString() {
		// return predicate + "=" + predicateIdx + "
		// [implication="+implicationIdxSet+"]";// + "=" +
		// equalityIndex.getIndex().size();
		return "(" + predicateId + ")" + predicate;// + "=" + equalityIndex.getIndex().size();
	}

	public boolean isEQ() {
		return predicate.getOp().equals(RelationalOperator.EQUAL);
	}

	public boolean isUNEQ() {
		return predicate.getOp().equals(RelationalOperator.UNEQUAL);
	}

	public boolean isLT() {
		return predicate.getOp().equals(RelationalOperator.LESS);
	}

	public boolean isGT() {
		return predicate.getOp().equals(RelationalOperator.GREATER);
	}

	public boolean isLTE() {
		return predicate.getOp().equals(RelationalOperator.LESS_EQUAL);
	}

	public boolean isGTE() {
		return predicate.getOp().equals(RelationalOperator.GREATER_EQUAL);
	}

	@Override
	public int compareTo(Object o) {
		IndexedPredicate p = (IndexedPredicate) o;

		return Integer.compare(this.predicateId, p.predicateId);
	}

}
