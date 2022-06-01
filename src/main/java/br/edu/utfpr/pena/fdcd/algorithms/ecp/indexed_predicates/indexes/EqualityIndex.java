package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.indexes;

import org.roaringbitmap.RoaringBitmap;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.IndexedPredicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.Predicate;
import br.edu.utfpr.pena.fdcd.input.columns.Column;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.floats.FloatList;


public class EqualityIndex {

	private Predicate predicate;

	private Float2ObjectOpenHashMap<RoaringBitmap> index;

	public EqualityIndex(IndexedPredicate indexedPredicate) {

		this.predicate = indexedPredicate.getPredicate();

		Column col = predicate.getCol1();
		FloatList valuesList = col.getValuesList();

		// System.out.println(valuesList);

		index = new Float2ObjectOpenHashMap<RoaringBitmap>((int) col.getCardinality());

		for (int tid = 0; tid < valuesList.size(); tid++) {

			float value = valuesList.getFloat(tid);

			RoaringBitmap mapEntry = index.computeIfAbsent(value, bitmap -> new RoaringBitmap());

			mapEntry.add(tid);
		}

	}

	public Float2ObjectOpenHashMap<RoaringBitmap> getIndex() {
		return index;
	}

	public RoaringBitmap geTidsOfEqualValues(float value) {
		return index.get(value);
	}

	@Override
	public String toString() {
		return "Index [index=" + index + "]";
	}

	public Predicate getPredicate() {

		return predicate;
	}

	public void removeTids(RoaringBitmap handledTids) {

		for (RoaringBitmap tids : index.values()) {
			tids.andNot(handledTids);
		}
	}

}
