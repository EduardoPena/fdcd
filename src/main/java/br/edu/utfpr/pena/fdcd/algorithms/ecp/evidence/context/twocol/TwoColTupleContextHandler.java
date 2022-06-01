package br.edu.utfpr.pena.fdcd.algorithms.ecp.evidence.context.twocol;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.roaringbitmap.RoaringBitmap;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.evidence.EvidenceSet;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.IndexedPredicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.ColumnPredicateGroup;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.NumericalColumnPredicateGroup;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.twocol.TwoColumnNumericalPredicateGroup;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.twocol.TwoColumnPredicateGroup;
import br.edu.utfpr.pena.fdcd.input.columns.CategoricalColumn;
import br.edu.utfpr.pena.fdcd.input.columns.NumericalColumn;

public class TwoColTupleContextHandler {

	private int tid;
	private Set<TwoColRhsContext> contextSet;

	private Set<TwoColRhsContext> contexts2add;
	private Set<TwoColRhsContext> contexts2remove;

	long startTime, endTime, timeElapsed;

	public TwoColTupleContextHandler(int tuple, RoaringBitmap tableIds, BitSet baseEvidence) {
		super();
		this.tid = tuple;
		this.contextSet = new HashSet<>();

		TwoColRhsContext initialContext = createContext(tableIds, baseEvidence);
		this.contextSet.add(initialContext);

		this.contexts2add = new HashSet<>();
		this.contexts2remove = new HashSet<>();

	}

	private TwoColRhsContext createContext(RoaringBitmap tableIds, BitSet baseEvidence) {

		RoaringBitmap c2 = tableIds.clone();
		c2.remove(tid);

		BitSet initialEvidence = (BitSet) baseEvidence.clone();

		TwoColRhsContext context = new TwoColRhsContext(c2, initialEvidence);

		return context;
	}

	public void updateTwoColTwoTupleCategoricalContext(TwoColumnPredicateGroup group) {

		CategoricalColumn lhsCol = (CategoricalColumn) group.getCol1();
		CategoricalColumn rhsCol = (CategoricalColumn) group.getCol2();

		String stringValueLhs = lhsCol.getFloatDictionary().getObject(lhsCol.getValueAt(tid));

		float floatValueInRhs = rhsCol.getFloatDictionary().getFloatForString(stringValueLhs);

		if (floatValueInRhs == 0) { // it means that there is no "join partner"
			return;
		}

		RoaringBitmap equalC2 = group.getTwoColEq().getRhs().getEqualityIndex().geTidsOfEqualValues(floatValueInRhs);

		if (equalC2.getCardinality() == 1 && equalC2.contains(tid)) { // singleton tuples, everyone is different
			return;
		}

		contexts2add.clear();
		contexts2remove.clear();

		Iterator<TwoColRhsContext> contextIt = contextSet.iterator();

		while (contextIt.hasNext()) {

			TwoColRhsContext context = contextIt.next();

			RoaringBitmap sharedEqual = RoaringBitmap.and(equalC2, context.c2);

			if (sharedEqual.getCardinality() > 0) {

				if (context.c2.equals(sharedEqual)) {// all c2 has an equal value, we can remove the !=
					contexts2remove.add(context);
				} else {

					context.c2.andNot(sharedEqual);
				}

				BitSet newEvidence = (BitSet) context.evidence.clone();

				newEvidence.xor(group.getTwoColEq().getEqCategoricalMask());// put the equal into the evidence

				TwoColRhsContext newContext = new TwoColRhsContext(sharedEqual, newEvidence);

				contexts2add.add(newContext);

			}

		}

		contextSet.addAll(contexts2add);
		contextSet.removeAll(contexts2remove);

	}

	public void updateCategoricalContext(ColumnPredicateGroup catGroup) {

		IndexedPredicate eq = catGroup.getEq();

		float tupleValue = eq.getPredicate().getCol1().getValueAt(tid);

		RoaringBitmap equalC2 = eq.getEqualityIndex().geTidsOfEqualValues(tupleValue);

		if (equalC2.getCardinality() == 1) { // singleton tuples, everyone is different
			return;
		}

		contexts2add.clear();
		contexts2remove.clear();

		Iterator<TwoColRhsContext> contextIt = contextSet.iterator();

		while (contextIt.hasNext()) {

			TwoColRhsContext context = contextIt.next();

			RoaringBitmap sharedEqual = RoaringBitmap.and(equalC2, context.c2);

			if (sharedEqual.getCardinality() > 0) {

				if (context.c2.equals(sharedEqual)) {// all c2 has an equal value, we can remove the !=
					contexts2remove.add(context);
				} else {
					context.c2.andNot(sharedEqual);
				}

				BitSet newEvidence = (BitSet) context.evidence.clone();

				newEvidence.xor(eq.getEqCategoricalMask());// put the equal into the evidence

				TwoColRhsContext newContext = new TwoColRhsContext(sharedEqual, newEvidence);

				contexts2add.add(newContext);

			}

		}

		contextSet.addAll(contexts2add);
		contextSet.removeAll(contexts2remove);

	}

	public void updateTwoColTwoTupleNumericalContext(TwoColumnNumericalPredicateGroup group) {

		NumericalColumn lhsCol = (NumericalColumn) group.getCol1();
//		NumericalColumn rhsCol = (NumericalColumn) group.getCol2();

		float lhsTupleValue = lhsCol.getValueAt(tid);

		IndexedPredicate eq = group.getTwoColEq().getRhs();
		IndexedPredicate lt = group.getTwoColLt().getRhs();

		RoaringBitmap equalC2 = eq.getEqualityIndex().geTidsOfEqualValues(lhsTupleValue); // "finds join partner for
																							// equality"

		RoaringBitmap ltC2;

		if (!group.isRhsBinned()) {

			ltC2 = lt.getLtIndex().getTidsForNextLTValues(lhsTupleValue);
		} else {

			ltC2 = lt.getLtBinnedIndex().getTidsForNextLTValues(lhsTupleValue);

		}

		contexts2add.clear();
		contexts2remove.clear();

		Iterator<TwoColRhsContext> contextIt = contextSet.iterator();

		while (contextIt.hasNext()) {

			TwoColRhsContext context = contextIt.next();

			boolean skipRange = false;

			if (equalC2 != null && equalC2.getCardinality() > 0) { // only tests equality if it finds a join partner

				RoaringBitmap sharedEqual = RoaringBitmap.and(equalC2, context.c2);

				if (sharedEqual.getCardinality() > 0) {

					if (context.c2.equals(sharedEqual)) {// all c2 has an equal value, we can remove the !=
						skipRange = true; // no need to test other cases
						contexts2remove.add(context);
					} else {
						context.c2.andNot(sharedEqual);
					}

					BitSet newEvidence = (BitSet) context.evidence.clone();

					newEvidence.xor(group.getTwoColTwoTupCorrectionMaskForEq());// put the equal (and gte/lte) into the
																				// evidence

					TwoColRhsContext newContext = new TwoColRhsContext(sharedEqual, newEvidence);

					contexts2add.add(newContext);

				}
			}

			if (skipRange == false) {

				RoaringBitmap sharedLt = RoaringBitmap.and(ltC2, context.c2);

				if (sharedLt.getCardinality() > 0) {

					BitSet newEvidence = (BitSet) context.evidence.clone();

					newEvidence.xor(group.getTwoColTwoTupCorrectionMaskForLT());// put the inverse range predicates into
																				// the evidence

					TwoColRhsContext newContext = new TwoColRhsContext(sharedLt, newEvidence);

					contexts2add.add(newContext);

					context.c2.andNot(sharedLt);

					if (context.c2.getCardinality() == 0) {//

						contexts2remove.add(context);
					}

				}

			}

		}

		contextSet.addAll(contexts2add);
		contextSet.removeAll(contexts2remove);

	}

	public void updateNumericalContext(NumericalColumnPredicateGroup numGroup) {

		float tupleValue = numGroup.getColumn().getValueAt(tid);

		IndexedPredicate eq = numGroup.getEq();
		IndexedPredicate lt = numGroup.getLt();

		RoaringBitmap equalC2 = eq.getEqualityIndex().geTidsOfEqualValues(tupleValue);

		RoaringBitmap ltC2;

		if (!numGroup.isBinnedLT())
			ltC2 = lt.getLtIndex().getTidsLTValues(tupleValue);
		else
			ltC2 = lt.getLtBinnedIndex().getTidsLTValues(tupleValue);

		contexts2add.clear();
		contexts2remove.clear();

		Iterator<TwoColRhsContext> contextIt = contextSet.iterator();

		while (contextIt.hasNext()) {

			TwoColRhsContext context = contextIt.next();

			boolean skipRange = false;

			RoaringBitmap sharedEqual = RoaringBitmap.and(equalC2, context.c2);

			if (sharedEqual.getCardinality() > 0) {

				if (context.c2.equals(sharedEqual)) {// all c2 has an equal value, we can remove the !=
					skipRange = true; // no need to test other cases
					contexts2remove.add(context);
				} else {
					context.c2.andNot(sharedEqual);
				}

				BitSet newEvidence = (BitSet) context.evidence.clone();

				newEvidence.xor(eq.getEqRangeMaskLt());// put the equal (and gte/lte) into the evidence

				TwoColRhsContext newContext = new TwoColRhsContext(sharedEqual, newEvidence);

				contexts2add.add(newContext);

			}

			if (skipRange == false) {

				RoaringBitmap sharedLt = RoaringBitmap.and(ltC2, context.c2);

				if (sharedLt.getCardinality() > 0) {

					BitSet newEvidence = (BitSet) context.evidence.clone();

					newEvidence.xor(lt.getRangeMask());// put the inverse range predicates into the evidence

					TwoColRhsContext newContext = new TwoColRhsContext(sharedLt, newEvidence);

					contexts2add.add(newContext);

					context.c2.andNot(sharedLt);

					if (context.c2.getCardinality() == 0) {//

						contexts2remove.add(context);
					}

				}

			}

		}

		contextSet.addAll(contexts2add);
		contextSet.removeAll(contexts2remove);

	}

	public void collectEvidence(EvidenceSet evidenceSet,
			List<NumericalColumnPredicateGroup> numericalColPredicateGroups) {

		Iterator<TwoColRhsContext> contextIt = contextSet.iterator();

		if (numericalColPredicateGroups.isEmpty()) {

			while (contextIt.hasNext()) {
				TwoColRhsContext context = contextIt.next();

				// evidenceSet.addEvidenceWithMultiplicity(context.evidence,
				// context.c2.getLongCardinality());

				/***
				 * Attention: the (cardinality*2) below is due to identity property of
				 * predicates == and <> t1.A==t2.A => t2.A==t1.A So, we do not need to "invert"
				 * the evidence, but simply count one additional one
				 **/
				evidenceSet.addEvidenceWithMultiplicity(context.evidence, context.c2.getLongCardinality() * 2);

			}

		} else {

//			List<Long> multiplicities = new ArrayList<>();

			while (contextIt.hasNext()) {
				TwoColRhsContext context = contextIt.next();

				// evidenceSet.addEvidenceWithMultiplicity(context.evidence,
				// context.c2.getLongCardinality());

				long eviMultiplicity = context.c2.getLongCardinality();

//				multiplicities.add(eviMultiplicity);

				if (eviMultiplicity == 0) {
					continue;
				}

				evidenceSet.addEvidenceWithMultiplicity(context.evidence, eviMultiplicity);

			}

		}

	}

	

	
	public Set<TwoColRhsContext> getContextSet() {
		return contextSet;
	}

	private TwoColTupleContextHandler(int tuple, TwoColRhsContext context) {
		this.tid = tuple;
		this.contextSet = new HashSet<>();
		contextSet.add(context);
		this.contexts2add = new HashSet<>();
		this.contexts2remove = new HashSet<>();

	}

	public List<TwoColTupleContextHandler> splitContexts() {

		List<TwoColTupleContextHandler> contexts = new ArrayList<TwoColTupleContextHandler>();

		Iterator<TwoColRhsContext> contextIt = contextSet.iterator();

		while (contextIt.hasNext()) {

			TwoColRhsContext context = contextIt.next();
			contexts.add(new TwoColTupleContextHandler(this.tid, context));
		}

		return contexts;
	}

	public void clear() {
		contextSet = null;
		this.contexts2add = null;
		this.contexts2remove = null;

	}



}
