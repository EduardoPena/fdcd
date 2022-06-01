package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.indexes;

import org.roaringbitmap.RoaringBitmap;

import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.floats.FloatBidirectionalIterator;
import it.unimi.dsi.fastutil.floats.FloatRBTreeSet;
import it.unimi.dsi.fastutil.floats.FloatSortedSet;

public class LtBinnedIndex {

	private Float2ObjectOpenHashMap<RoaringBitmap> unsortedIndex; // eq map , simple key -> tids index

	private Float2ObjectOpenHashMap<InnerIndex> value2IndexMap; // key to its bin based index

	private FloatSortedSet sortedEntries;

	private final int nbins = 500;
	private int tableSize;
	private int tidsPerBin;

	public LtBinnedIndex(EqualityIndex equalityIndex, int tableSize) {

		this.unsortedIndex = equalityIndex.getIndex();
		this.tableSize = tableSize;
		this.tidsPerBin = tableSize / nbins;


		buildIndex();

	}

	private void buildIndex() {

		value2IndexMap = new Float2ObjectOpenHashMap<InnerIndex>(); // main index

		sortedEntries = new FloatRBTreeSet(unsortedIndex.keySet());// sorted entries

		FloatBidirectionalIterator it = sortedEntries.iterator(sortedEntries.lastFloat());

		// build index incrementally

		InnerIndex currentInnerIndex = new InnerIndex();

		RoaringBitmap previousRemainingLT = new RoaringBitmap(); // any key in this inner index has no remaining LT

		currentInnerIndex.remainingLT = previousRemainingLT.clone(); // any key in this inner index has no remaining LT

		float lastValue = it.previousFloat();

		value2IndexMap.put(lastValue, currentInnerIndex);

		RoaringBitmap lastLt = new RoaringBitmap();
		currentInnerIndex.innerLtSortedIndex.put(lastValue, lastLt);// last element is lower than nothing

		RoaringBitmap lastBitmap = unsortedIndex.get(lastValue);
		RoaringBitmap previousLt = new RoaringBitmap();
		previousLt.or(lastBitmap);// fix for the next iteration

		while (it.hasPrevious()) {

			if (previousLt.getCardinality() >= tidsPerBin) {

				currentInnerIndex = new InnerIndex();
				currentInnerIndex.remainingLT = RoaringBitmap.or(previousRemainingLT, previousLt);

				previousRemainingLT = currentInnerIndex.remainingLT;

				previousLt = new RoaringBitmap();

			}

			float currValue = it.previousFloat();

			value2IndexMap.put(currValue, currentInnerIndex);// feed the index

			currentInnerIndex.innerLtSortedIndex.put(currValue, previousLt);

			RoaringBitmap currBitmap = unsortedIndex.get(currValue);

			previousLt = RoaringBitmap.or(previousLt, currBitmap); // fix for the next iteration

		}

	}

	public class InnerIndex {

		Float2ObjectOpenHashMap<RoaringBitmap> innerLtSortedIndex;
		RoaringBitmap remainingLT;

		public InnerIndex() {

			this.innerLtSortedIndex = new Float2ObjectOpenHashMap<RoaringBitmap>();

		}

	}

	public RoaringBitmap getTidsLTValues(float value) {

		InnerIndex index = value2IndexMap.get(value);
		return RoaringBitmap.or(index.innerLtSortedIndex.get(value), index.remainingLT);
	}

	// optimize for smaller
//	public RoaringBitmap getTidsLTValues(float value, RoaringBitmap valueBits) {
//
//		InnerIndex index = value2IndexMap.get(value);
//		
//		RoaringBitmap temp = 
//		
//		
//		return RoaringBitmap.or(index.innerLtSortedIndex.get(value), index.remainingLT);
//	}

	public int size() {

		return value2IndexMap.size();
	}

	// for two col approach
	public RoaringBitmap getTidsForNextLTValues(float lhsTupleValue) {

		RoaringBitmap tids = null;

		if (unsortedIndex.containsKey(lhsTupleValue)) {

			tids = getTidsLTValues(lhsTupleValue); // already got it there

		} else {

			FloatSortedSet head = sortedEntries.headSet(lhsTupleValue);

			if (!head.isEmpty()) {

				float value = head.lastFloat();
				return getTidsLTValues(value);

			} else {
				
				float minimumValue = sortedEntries.firstFloat();
				tids = getTidsLTValues(minimumValue);
				tids = RoaringBitmap.or(tids, unsortedIndex.get(minimumValue));
				
			}

		}

		return tids;
	}

}
