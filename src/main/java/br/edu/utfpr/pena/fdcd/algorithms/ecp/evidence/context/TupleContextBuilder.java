package br.edu.utfpr.pena.fdcd.algorithms.ecp.evidence.context;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.roaringbitmap.RoaringBitmap;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.evidence.EvidenceSet;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.evidence.ParallelEvidenceSet;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.IndexedPredicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.ColumnPredicateGroup;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.NumericalColumnPredicateGroup;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.indexes.EqualityIndex;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.indexes.LtBinnedIndex;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.indexes.LtIndex;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.indexes.comparators.EqualityIndexSizeComparator;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.PredicateSpace;
import br.edu.utfpr.pena.fdcd.input.Table;
import br.edu.utfpr.pena.fdcd.input.columns.Column;
import br.edu.utfpr.pena.fdcd.input.columns.NumericalColumn;
import br.edu.utfpr.pena.fdcd.input.sorters.ColumnComparatorByCardinality;
import br.edu.utfpr.pena.fdcd.input.sorters.RowComparatorMultiColumn;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;

public class TupleContextBuilder {

	private Table table;
	protected int rowNumber;
	private PredicateSpace predicateSpace;
	private ParallelEvidenceSet evidenceSet;
	private RoaringBitmap tableIds;

	private BitSet baseEvidence;

	private List<ColumnPredicateGroup> catPredGroups;
	private List<NumericalColumnPredicateGroup> numPredGroups;

	protected ArrayList<Integer> rowSequence;

	private static final int BinThreshold = 2000;

	public TupleContextBuilder(Table table, PredicateSpace predicateSpace) {

		this.table = table;
		this.rowNumber = table.getNUM_RECORDS();
		this.predicateSpace = predicateSpace;

		this.tableIds = new RoaringBitmap();
		this.tableIds.add((long) 0, (long) rowNumber);

		rowSequence = getRowSequence();

	}

	public TupleContextBuilder(Table table, PredicateSpace predicateSpace, boolean sortInput, boolean sortPredicates,
			boolean catPredicatesFirst) {

		this.table = table;
		this.rowNumber = table.getNUM_RECORDS();
		this.predicateSpace = predicateSpace;

		this.tableIds = new RoaringBitmap();
		this.tableIds.add((long) 0, (long) rowNumber);

		rowSequence = getRowSequence();

	}

	public EvidenceSet build() {

		this.evidenceSet = new ParallelEvidenceSet(predicateSpace);

		sortNumericalColumnsMulti(table.getNumericalColumns().values().size());

		buildPredicateIndexes();

		this.baseEvidence = createBaseEvidence();

		EqualityIndexSizeComparator equalityIndexComparator = new EqualityIndexSizeComparator();

		catPredGroups = new ArrayList<>(predicateSpace.getCategoricalPredicateGroups());
		Collections.sort(catPredGroups, equalityIndexComparator);
		Collections.reverse(catPredGroups);

		numPredGroups = new ArrayList<>(predicateSpace.getNumericalPredicateGroups());

		Collections.sort(numPredGroups, equalityIndexComparator);
		Collections.reverse(numPredGroups);

		rowSequence.parallelStream().forEach(tid -> {

			RoaringBitmap tableIdsCopy = tableIds.clone();

			tableIdsCopy.remove(0L, (tid + 1));

			TupleContextHandler eviContext = new TupleContextHandler(tid, tableIdsCopy, baseEvidence);

			for (ColumnPredicateGroup catGroup : catPredGroups) {

				eviContext.updateCategoricalContext(catGroup);

			}

			for (NumericalColumnPredicateGroup numGroup : numPredGroups) {

				eviContext.updateNumericalContext(numGroup);

			}

			eviContext.collectEvidence(evidenceSet, numPredGroups);

		});

		return evidenceSet;
	}

	private ArrayList<Integer> getRowSequence() {
		ArrayList<Integer> rowSequence = new ArrayList<>();
		for (int tid = 0; tid < rowNumber; ++tid) {
			rowSequence.add(tid);
		}
		return rowSequence;
	}

	private void buildPredicateIndexes() {

		for (ColumnPredicateGroup catPredicateGroup : predicateSpace.getCategoricalPredicateGroups()) {
			IndexedPredicate eq = catPredicateGroup.getEq();
			EqualityIndex eqIndex = new EqualityIndex(eq);
			eq.setEqualityIndex(eqIndex);
		}

		for (NumericalColumnPredicateGroup numPredicateGroup : predicateSpace.getNumericalPredicateGroups()) {

			IndexedPredicate eq = numPredicateGroup.getEq();
			EqualityIndex eqIndex = new EqualityIndex(eq);
			eq.setEqualityIndex(eqIndex);

			if (eqIndex.getIndex().size() >= BinThreshold) {
				numPredicateGroup.setBinnedLT(true);
				LtBinnedIndex ltBinnedIndex = new LtBinnedIndex(eqIndex, table.getNUM_RECORDS());
				numPredicateGroup.getLt().setLtBinnedIndex(ltBinnedIndex);

			} else {

				LtIndex ltIndex = new LtIndex(eqIndex);
				numPredicateGroup.getLt().setLtIndex(ltIndex);

			}

		}

	}

	private BitSet createBaseEvidence() {

		BitSet baseEvidence = new BitSet();
		// assuming UNEQ in the evidence, then we correct for EQ
		for (ColumnPredicateGroup catGroup : predicateSpace.getCategoricalPredicateGroups()) {
			IndexedPredicate uneq = catGroup.getUneq();
			baseEvidence.set(uneq.getPredicateId());

		}
		for (NumericalColumnPredicateGroup numGroup : predicateSpace.getNumericalPredicateGroups()) {
			// assuming GT in the evidence, then we correct for LT
			IndexedPredicate uneq = numGroup.getUneq();
			IndexedPredicate gt = numGroup.getGt();
			IndexedPredicate gte = numGroup.getGte();
			baseEvidence.set(uneq.getPredicateId());
			baseEvidence.set(gt.getPredicateId());
			baseEvidence.set(gte.getPredicateId());
		}
		return baseEvidence;

	}

	public void sortNumericalColumnsMulti(int levels) {

		ColumnComparatorByCardinality columnComparator = new ColumnComparatorByCardinality();

		List<NumericalColumn> numCols = new ArrayList<>(table.getNumericalColumns().values());

		if (numCols.isEmpty()) {
			return;
		}

		Collections.sort(numCols, columnComparator);
		Collections.reverse(numCols);

		List<Integer> orderedRowIDs = IntStream.range(0, table.getNUM_RECORDS()).boxed().collect(Collectors.toList());

		if (levels > 0 && levels <= numCols.size())
			numCols = numCols.subList(0, levels);

		RowComparatorMultiColumn rowComparator = new RowComparatorMultiColumn(numCols);
		Collections.sort(orderedRowIDs, rowComparator);

		for (Column c : table.getAllColumns().values()) {

			FloatList orderedValues = new FloatArrayList(table.getNUM_RECORDS());

			for (int tid : orderedRowIDs) {
				orderedValues.add(c.getValueAt(tid));

			}

			c.setValuesList(orderedValues);

		}

	}

	public EvidenceSet getEvidenceSet() {
		return evidenceSet;
	}

}
