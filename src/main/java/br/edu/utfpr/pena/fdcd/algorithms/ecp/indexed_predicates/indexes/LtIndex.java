package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.indexes;

import org.roaringbitmap.RoaringBitmap;

import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.floats.FloatBidirectionalIterator;
import it.unimi.dsi.fastutil.floats.FloatRBTreeSet;
import it.unimi.dsi.fastutil.floats.FloatSortedSet;

public class LtIndex {

	private Float2ObjectOpenHashMap<RoaringBitmap> unsortedIndex;

	private FloatSortedSet sortedEntries; // no need to keep it in mememory once the index is built


	private Float2ObjectOpenHashMap<RoaringBitmap> ltSortedIndex;

	public LtIndex(EqualityIndex equalityIndex) {

		this.unsortedIndex = equalityIndex.getIndex();
//		this.sortedEntries = new FloatRBTreeSet(unsortedIndex.keySet());
		buildLtIndex();

	}

	private void buildLtIndex() {

		sortedEntries = new FloatRBTreeSet(unsortedIndex.keySet());

		ltSortedIndex = new Float2ObjectOpenHashMap<RoaringBitmap>();
//		ltSortedIndex = new Float2ObjectRBTreeMap<RoaringBitmap>();

		FloatBidirectionalIterator it = sortedEntries.iterator(sortedEntries.lastFloat());

		float lastValue = it.previousFloat();

		RoaringBitmap lastBitmap = unsortedIndex.get(lastValue);

		RoaringBitmap lastLt = new RoaringBitmap();
		ltSortedIndex.put(lastValue, lastLt);// last element is lower than nothing

		RoaringBitmap previousLt = new RoaringBitmap();
		previousLt.or(lastBitmap);// fix for the next iteration

		while (it.hasPrevious()) {

			float currValue = it.previousFloat();

			ltSortedIndex.put(currValue, previousLt);

			RoaringBitmap currBitmap = unsortedIndex.get(currValue);

			previousLt = RoaringBitmap.or(previousLt, currBitmap);// fix for the next iteration

		}

	}

	public Float2ObjectOpenHashMap<RoaringBitmap> getLtIndex() {
//	public Float2ObjectSortedMap<RoaringBitmap> getLtIndex() {
		return ltSortedIndex;
	}

	public RoaringBitmap getTidsLTValues(float value) {
		return ltSortedIndex.get(value);
	}

	public RoaringBitmap getTidsOfEqualValues(float value) {
		return unsortedIndex.get(value);
	}

//	public FloatSortedSet getSortedEntries() {
//		return sortedEntries;
//	}

	@Override
	public String toString() {
		return "Index [index=" + unsortedIndex + "]";
	}

	// for two col approach
	public RoaringBitmap getTidsForNextLTValues(float lhsTupleValue) {

		RoaringBitmap tids = ltSortedIndex.get(lhsTupleValue);// already have that value in the domain

		if (tids == null) {

			FloatSortedSet head = sortedEntries.headSet(lhsTupleValue);

			if (!head.isEmpty()) {

				float value = head.lastFloat();

				tids = getTidsLTValues(value);


				// plus the tids of that value because it is already less than lhsTupleValue
//				 tids =RoaringBitmap.or(tids, unsortedIndex.get(value));

			}
			// if (head.isEmpty()) {// no element is striclt less than lhsTupleValue
			else {

				float minimumValue = sortedEntries.firstFloat();
				tids = getTidsLTValues(minimumValue);

				tids = RoaringBitmap.or(tids, unsortedIndex.get(minimumValue));

			}


		}

		return tids;
	}

}
