package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.builder;

import java.util.ArrayList;
import java.util.List;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.IndexedPredicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.twocol.TwoColumnIndexedPredicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.twocol.TwoColumnNumericalPredicateGroup;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.twocol.TwoColumnPredicateGroup;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.RelationalOperator;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.TupleColumnPairPredicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.TuplePairColumnPairPredicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.PredicateProvider;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.PredicateSpace;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.TwoColumnPredicateSpace;
import br.edu.utfpr.pena.fdcd.input.Table;
import br.edu.utfpr.pena.fdcd.input.columns.CategoricalColumn;
import br.edu.utfpr.pena.fdcd.input.columns.Column;
import br.edu.utfpr.pena.fdcd.utils.misc.Object2IndexMapper;

public class TwoColumnPredicateSpaceBuilder {

	private static double SharedValuesRatio = 0.35;

	public TwoColumnPredicateSpace build(Table input) {

		// *********** single column predicates ***********//

		PredicateSpace space = new PredicateSpaceBuilder().build(input);

		// ************************************************************************************//

		// Cross columns predicate

		TwoColumnPredicateSpace twoColspace = new TwoColumnPredicateSpace(space);

		List<Column> columns = new ArrayList<>(input.getAllColumns().values());

		for (int ic1 = 0; ic1 < columns.size(); ic1++) {

			Column c1 = columns.get(ic1);

			for (int ic2 = ic1 + 1; ic2 < columns.size(); ic2++) {

				Column c2 = columns.get(ic2);

				if (!c1.equals(c2) && c1.ColumnType.equals(c2.ColumnType)
						&& c1.containsSameValue(c2, SharedValuesRatio)) {

					if (c1 instanceof CategoricalColumn) {

						twoColspace.addTwoColSingleTupleCategoricalPredicateGroup(
								createTwoColCategoricalColumnGroup(twoColspace, c1, c2, true));

						twoColspace.addTwoColTwoTupleCategoricalPredicateGroup(
								createTwoColCategoricalColumnGroup(twoColspace, c1, c2, false));

					} else { // NumericalColumn

						twoColspace.addTwoColSingleTupleNumericalPredicateGroup(
								createTwoColNumericalColumnGroup(twoColspace, c1, c2, true));

						twoColspace.addTwoColTwoTupleNumericalPredicateGroup(
								createTwoColNumericalColumnGroup(twoColspace, c1, c2, false));

					}

//					PredicateProvider.getInstance().getPredicate(op, col1)

				}

			}

		}
		columns.forEach(c -> c.clearValuesCountMap());

		return twoColspace;
	}

	private TwoColumnPredicateGroup createTwoColCategoricalColumnGroup(TwoColumnPredicateSpace twoColspace, Column c1,
			Column c2, boolean singleColumn) {

		Object2IndexMapper<IndexedPredicate> predicateIndex = twoColspace.getOneColspace().getPredicateIndex();

		IndexedPredicate eqC1 = PredicateProvider.getInstance().getPredicate(RelationalOperator.EQUAL, c1);
		IndexedPredicate eqC2 = PredicateProvider.getInstance().getPredicate(RelationalOperator.EQUAL, c2);

		IndexedPredicate uneqC1 = PredicateProvider.getInstance().getPredicate(RelationalOperator.UNEQUAL, c1);
		IndexedPredicate uneqC2 = PredicateProvider.getInstance().getPredicate(RelationalOperator.UNEQUAL, c2);

		TwoColumnIndexedPredicate twoColEq;
		TwoColumnIndexedPredicate twoColUneq;
		if (singleColumn) {
			// TupleColumnPairPredicates
			twoColEq = new TwoColumnIndexedPredicate(new TupleColumnPairPredicate(c1, RelationalOperator.EQUAL, c2),
					eqC1, eqC2, singleColumn, twoColspace, false);
			twoColUneq = new TwoColumnIndexedPredicate(new TupleColumnPairPredicate(c1, RelationalOperator.UNEQUAL, c2),
					uneqC1, uneqC2, singleColumn, twoColspace, false);

		} else {

			// TuplePairColumnPairPredicates
			twoColEq = new TwoColumnIndexedPredicate(new TuplePairColumnPairPredicate(c1, RelationalOperator.EQUAL, c2),
					eqC1, eqC2, singleColumn, twoColspace, false);
			twoColUneq = new TwoColumnIndexedPredicate(
					new TuplePairColumnPairPredicate(c1, RelationalOperator.UNEQUAL, c2), uneqC1, uneqC2, singleColumn,
					twoColspace, false);
		}
		// build masks for PipeDC

		int eqIdx = predicateIndex.getIndex(twoColEq);

		int uneqIdx = predicateIndex.getIndex(twoColUneq);

		twoColEq.setPredicateId(eqIdx);

		twoColUneq.setPredicateSymmetricId(eqIdx);

		twoColUneq.setPredicateId(uneqIdx);

		// mask to correct evidence
		twoColEq.setEqCategoricalMask(eqIdx, uneqIdx);

		// ***********************************//

		return new TwoColumnPredicateGroup(twoColEq, twoColUneq, singleColumn);

	}

	private TwoColumnNumericalPredicateGroup createTwoColNumericalColumnGroup(TwoColumnPredicateSpace twoColspace,
			Column c1, Column c2, boolean singleColumn) {

		Object2IndexMapper<IndexedPredicate> predicateIndex = twoColspace.getOneColspace().getPredicateIndex();

		IndexedPredicate eqC1 = PredicateProvider.getInstance().getPredicate(RelationalOperator.EQUAL, c1);
		IndexedPredicate eqC2 = PredicateProvider.getInstance().getPredicate(RelationalOperator.EQUAL, c2);

		IndexedPredicate uneqC1 = PredicateProvider.getInstance().getPredicate(RelationalOperator.UNEQUAL, c1);
		IndexedPredicate uneqC2 = PredicateProvider.getInstance().getPredicate(RelationalOperator.UNEQUAL, c2);

		IndexedPredicate ltC1 = PredicateProvider.getInstance().getPredicate(RelationalOperator.LESS, c1);
		IndexedPredicate ltC2 = PredicateProvider.getInstance().getPredicate(RelationalOperator.LESS, c2);

		IndexedPredicate lteC1 = PredicateProvider.getInstance().getPredicate(RelationalOperator.LESS_EQUAL, c1);
		IndexedPredicate lteC2 = PredicateProvider.getInstance().getPredicate(RelationalOperator.LESS_EQUAL, c2);

		IndexedPredicate gtC1 = PredicateProvider.getInstance().getPredicate(RelationalOperator.GREATER, c1);
		IndexedPredicate gtC2 = PredicateProvider.getInstance().getPredicate(RelationalOperator.GREATER, c2);

		IndexedPredicate gteC1 = PredicateProvider.getInstance().getPredicate(RelationalOperator.GREATER_EQUAL, c1);
		IndexedPredicate gteC2 = PredicateProvider.getInstance().getPredicate(RelationalOperator.GREATER_EQUAL, c2);

		TwoColumnIndexedPredicate twoColEq;
		TwoColumnIndexedPredicate twoColUneq;
		TwoColumnIndexedPredicate twoColLt;
		TwoColumnIndexedPredicate twoColLte;
		TwoColumnIndexedPredicate twoColGt;
		TwoColumnIndexedPredicate twoColGte;

		if (singleColumn) {

			twoColEq = new TwoColumnIndexedPredicate(new TupleColumnPairPredicate(c1, RelationalOperator.EQUAL, c2),
					eqC1, eqC2, singleColumn, twoColspace, true);
			twoColUneq = new TwoColumnIndexedPredicate(new TupleColumnPairPredicate(c1, RelationalOperator.UNEQUAL, c2),
					uneqC1, uneqC2, singleColumn, twoColspace, true);

			twoColLt = new TwoColumnIndexedPredicate(new TupleColumnPairPredicate(c1, RelationalOperator.LESS, c2),
					ltC1, ltC2, singleColumn, twoColspace, true);

			twoColLte = new TwoColumnIndexedPredicate(
					new TupleColumnPairPredicate(c1, RelationalOperator.LESS_EQUAL, c2), lteC1, lteC2, singleColumn,
					twoColspace, true);

			twoColGt = new TwoColumnIndexedPredicate(new TupleColumnPairPredicate(c1, RelationalOperator.GREATER, c2),
					gtC1, gtC2, singleColumn, twoColspace, true);

			twoColGte = new TwoColumnIndexedPredicate(
					new TupleColumnPairPredicate(c1, RelationalOperator.GREATER_EQUAL, c2), gteC1, gteC2, singleColumn,
					twoColspace, true);

		} else {

			twoColEq = new TwoColumnIndexedPredicate(new TuplePairColumnPairPredicate(c1, RelationalOperator.EQUAL, c2),
					eqC1, eqC2, singleColumn, twoColspace, true);
			twoColUneq = new TwoColumnIndexedPredicate(
					new TuplePairColumnPairPredicate(c1, RelationalOperator.UNEQUAL, c2), uneqC1, uneqC2, singleColumn,
					twoColspace, true);

			twoColLt = new TwoColumnIndexedPredicate(new TuplePairColumnPairPredicate(c1, RelationalOperator.LESS, c2),
					ltC1, ltC2, singleColumn, twoColspace, true);

			twoColLte = new TwoColumnIndexedPredicate(
					new TuplePairColumnPairPredicate(c1, RelationalOperator.LESS_EQUAL, c2), lteC1, lteC2, singleColumn,
					twoColspace, true);

			twoColGt = new TwoColumnIndexedPredicate(
					new TuplePairColumnPairPredicate(c1, RelationalOperator.GREATER, c2), gtC1, gtC2, singleColumn,
					twoColspace, true);

			twoColGte = new TwoColumnIndexedPredicate(
					new TuplePairColumnPairPredicate(c1, RelationalOperator.GREATER_EQUAL, c2), gteC1, gteC2,
					singleColumn, twoColspace, true);

		}

		int eqIdx = predicateIndex.getIndex(twoColEq);
		int uneqIdx = predicateIndex.getIndex(twoColUneq);
		int ltIdx = predicateIndex.getIndex(twoColLt);
		int lteIdx = predicateIndex.getIndex(twoColLte);
		int gtIdx = predicateIndex.getIndex(twoColGt);
		int gteIdx = predicateIndex.getIndex(twoColGte);

		// for negation and implication, see Table 2 of Xu Chu's paper on dc discovery

		twoColEq.setPredicateId(eqIdx);
		twoColEq.setPredicateInverseId(uneqIdx);
		twoColEq.setImplicationIdSet(eqIdx, gteIdx, lteIdx);
		twoColEq.setPredicateSymmetricId(eqIdx);

		twoColUneq.setPredicateId(uneqIdx);
		twoColUneq.setPredicateInverseId(eqIdx);
		twoColUneq.setImplicationIdSet(uneqIdx);
		twoColUneq.setPredicateSymmetricId(uneqIdx);

		twoColLt.setPredicateId(ltIdx);
		twoColLt.setPredicateInverseId(gteIdx);
		twoColLt.setImplicationIdSet(ltIdx, lteIdx, uneqIdx);
		twoColLt.setPredicateSymmetricId(gtIdx);

		twoColLte.setPredicateId(lteIdx);
		twoColLte.setPredicateInverseId(gtIdx);
		twoColLte.setImplicationIdSet(lteIdx);
		twoColLte.setPredicateSymmetricId(gteIdx);

		twoColGt.setPredicateId(gtIdx);
		twoColGt.setPredicateInverseId(lteIdx);
		twoColGt.setImplicationIdSet(gtIdx, gteIdx, uneqIdx);
		twoColGt.setPredicateSymmetricId(ltIdx);

		twoColGte.setPredicateId(gteIdx);
		twoColGte.setPredicateInverseId(ltIdx);
		twoColGte.setImplicationIdSet(gteIdx);
		twoColGte.setPredicateSymmetricId(lteIdx);

		return new TwoColumnNumericalPredicateGroup(twoColEq, twoColUneq, twoColLt, twoColLte, twoColGt, twoColGte,
				singleColumn);

	}

}
