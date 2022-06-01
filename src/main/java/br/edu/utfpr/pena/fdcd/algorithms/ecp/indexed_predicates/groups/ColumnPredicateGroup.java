package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups;

import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.roaringbitmap.RoaringBitmap;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.IndexedPredicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.RelationalOperator;
import br.edu.utfpr.pena.fdcd.input.columns.Column;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;

public class ColumnPredicateGroup {

	protected Set<IndexedPredicate> predicates;

	protected IndexedPredicate eq;
	protected IndexedPredicate uneq;

	protected Column column;

	protected BitSet predicateIDs;

	public ColumnPredicateGroup(IndexedPredicate eq, IndexedPredicate uneq) {

		this.column = eq.getPredicate().getCol1();

		this.eq = eq;
		this.uneq = uneq;

		this.predicates = new LinkedHashSet<>();
		this.predicates.add(eq);
		this.predicates.add(uneq);

	}

	public Set<IndexedPredicate> getPredicates() {
		return predicates;
	}

	public IndexedPredicate getEq() {
		return eq;
	}

	public IndexedPredicate getUneq() {
		return uneq;
	}

	public Column getColumn() {
		return column;
	}

	@Override
	public String toString() {
		return "CatColPredGroup [" + predicates + "]";
	}

	public ObjectCollection<RoaringBitmap> getPartialEqTidsSet(RoaringBitmap tids) {

		Column col = eq.getPredicate().getCol1();
		FloatList valuesList = col.getValuesList();

		Float2ObjectOpenHashMap<RoaringBitmap> partialIndex = new Float2ObjectOpenHashMap<RoaringBitmap>();

		for (int tid : tids) {

			float value = valuesList.getFloat(tid);

			RoaringBitmap mapEntry = partialIndex.computeIfAbsent(value, bitmap -> new RoaringBitmap());

			mapEntry.add(tid);
		}

		return partialIndex.values();
	}

	public Float2ObjectOpenHashMap<RoaringBitmap> getPartialIndex(RoaringBitmap tids) {

		Column col = eq.getPredicate().getCol1();
		FloatList valuesList = col.getValuesList();

		Float2ObjectOpenHashMap<RoaringBitmap> partialIndex = new Float2ObjectOpenHashMap<RoaringBitmap>();

		for (int tid : tids) {

			float value = valuesList.getFloat(tid);

			RoaringBitmap mapEntry = partialIndex.computeIfAbsent(value, bitmap -> new RoaringBitmap());

			mapEntry.add(tid);
		}

		return partialIndex;
	}

	public BitSet getPredicateIDs() {

		if (predicateIDs == null) {
			predicateIDs = new BitSet();

			for (IndexedPredicate p : predicates) {
				predicateIDs.set(p.getPredicateId());
			}

		}

		return predicateIDs;
	}

	public IndexedPredicate getPredicate(RelationalOperator op) {

		if (op.equals(RelationalOperator.EQUAL)) {
			return eq;
		} 
		else if (op.equals(RelationalOperator.UNEQUAL)) {
			return uneq;
		} 
		else {
			return null;
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((column == null) ? 0 : column.hashCode());
		result = prime * result + ((predicates == null) ? 0 : predicates.hashCode());
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
		ColumnPredicateGroup other = (ColumnPredicateGroup) obj;
		if (column == null) {
			if (other.column != null)
				return false;
		} else if (!column.equals(other.column))
			return false;
		if (predicates == null) {
			if (other.predicates != null)
				return false;
		} else if (!predicates.equals(other.predicates))
			return false;
		return true;
	}
	
	

}
