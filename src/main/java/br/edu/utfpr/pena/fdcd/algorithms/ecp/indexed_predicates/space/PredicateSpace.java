package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.IndexedPredicate;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.ColumnPredicateGroup;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.NumericalColumnPredicateGroup;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates.Predicate;
import br.edu.utfpr.pena.fdcd.input.columns.Column;
import br.edu.utfpr.pena.fdcd.utils.misc.Object2IndexMapper;

public class PredicateSpace {

	protected Object2IndexMapper<IndexedPredicate> predicateIndex;

	protected Set<ColumnPredicateGroup> predicateGroups;

	protected Set<ColumnPredicateGroup> categoricalPredicateGroups;

	protected Set<ColumnPredicateGroup> categoricalSimilarityPredicateGroups;

	protected Set<NumericalColumnPredicateGroup> numericalPredicateGroups;

	protected Map<Column, ColumnPredicateGroup> column2GroupMap;

	public PredicateSpace(Object2IndexMapper<IndexedPredicate> predicateIndex) {
		this.predicateIndex = predicateIndex;
		this.predicateGroups = new LinkedHashSet<>();
		this.categoricalSimilarityPredicateGroups = new LinkedHashSet<>();
		this.categoricalPredicateGroups = new LinkedHashSet<>();
		this.numericalPredicateGroups = new LinkedHashSet<>();
	}

	private PredicateSpace() {
	}

	public PredicateSpace clone() {
		PredicateSpace ps = new PredicateSpace();
		ps.predicateIndex = this.predicateIndex;
		ps.predicateGroups = new LinkedHashSet<>(this.predicateGroups);
		ps.categoricalPredicateGroups = new LinkedHashSet<>(this.categoricalPredicateGroups);
		ps.categoricalSimilarityPredicateGroups = new LinkedHashSet<>(this.categoricalSimilarityPredicateGroups);
		ps.numericalPredicateGroups = new LinkedHashSet<>(this.numericalPredicateGroups);
		return ps;
	}

	public IndexedPredicate getPredicateById(int pid) {

		return predicateIndex.getObject(pid);
	}

	public int size() {
		return predicateIndex.size();
	}

	public Set<String> convert2PredicateFullStringSet(BitSet bitSet) {

		List<String> strSet = new ArrayList<String>();

		for (int i = bitSet.nextSetBit(0); i != -1; i = bitSet.nextSetBit(i + 1)) {
			Predicate p = predicateIndex.getObject(i).getPredicate();
			strSet.add(p.toString().toLowerCase());
		}

		Collections.sort(strSet);

		return new LinkedHashSet<>(strSet);
	}

	public Set<String> convert2PredicateStringSet(BitSet bitSet) {

		Set<String> strSet = new LinkedHashSet<String>();

		for (int i = bitSet.nextSetBit(0); i != -1; i = bitSet.nextSetBit(i + 1)) {
			Predicate p = predicateIndex.getObject(i).getPredicate();
			strSet.add(p.getCol1().toString().toLowerCase() + "" + p.getOp().getShortString());
		}

		return strSet;
	}

	public String convert2DVDDC(BitSet bitSet) {

		Set<String> strSet = new LinkedHashSet<String>();

		for (int i = bitSet.nextSetBit(0); i != -1; i = bitSet.nextSetBit(i + 1)) {
			Predicate p = predicateIndex.getObject(i).getPredicate();
			strSet.add(p.toString());
		}

		String dc = strSet.toString().toLowerCase();

		dc = dc.replace("[", "");
		dc = dc.replace("]", "");
		dc = dc.replace(" ", "");

		return dc;
	}

	public boolean containsSameColumnGroup(BitSet bitSet, int idx) {

		Predicate newp = predicateIndex.getObject(idx).getPredicate();

		for (int i = bitSet.nextSetBit(0); i != -1; i = bitSet.nextSetBit(i + 1)) {
			Predicate p = predicateIndex.getObject(i).getPredicate();

			if (newp.getCol1().equals(p.getCol1())) {
				return true;
			}

		}

		return false;
	}

	public void addCategoricalPredicateGroup(ColumnPredicateGroup categoricalGroup) {
		predicateGroups.add(categoricalGroup);
		categoricalPredicateGroups.add(categoricalGroup);
	}

	public void addCategoricalSimilarityPredicateGroup(ColumnPredicateGroup categoricalSimilarityGroup) {
		predicateGroups.add(categoricalSimilarityGroup);
		categoricalSimilarityPredicateGroups.add(categoricalSimilarityGroup);
	}

	public void addNumericalPredicateGroup(NumericalColumnPredicateGroup numericalGroup) {
		predicateGroups.add(numericalGroup);
		numericalPredicateGroups.add(numericalGroup);
	}

	public Object2IndexMapper<IndexedPredicate> getPredicateIndex() {
		return predicateIndex;
	}

	public Set<ColumnPredicateGroup> getCategoricalPredicateGroups() {
		return categoricalPredicateGroups;
	}

	public Set<ColumnPredicateGroup> getCategoricalSimilarityPredicateGroups() {
		return categoricalSimilarityPredicateGroups;
	}

	public void setCategoricalSimilarityPredicateGroups(
			Set<ColumnPredicateGroup> categoricalSimilarityPredicateGroups) {
		this.categoricalSimilarityPredicateGroups = categoricalSimilarityPredicateGroups;
	}

	public Set<NumericalColumnPredicateGroup> getNumericalPredicateGroups() {
		return numericalPredicateGroups;
	}

	public Set<ColumnPredicateGroup> getPredicateGroups() {
		return predicateGroups;
	}

	public Map<Column, ColumnPredicateGroup> getColumn2GroupMap() {

		if (column2GroupMap == null) {

			column2GroupMap = new HashMap<>();
			for (ColumnPredicateGroup group : predicateGroups) {
				column2GroupMap.put(group.getColumn(), group);
			}
		}

		return column2GroupMap;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("[Number of predicates: " + predicateIndex.size() + "]");
		sb.append("\n");

		for (ColumnPredicateGroup group : predicateGroups) {

			sb.append(group);
			sb.append("\n");

		}

		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	public BitSet getSymetric(BitSet dc) {

		BitSet sym = new BitSet();

		for (int i = dc.nextSetBit(0); i != -1; i = dc.nextSetBit(i + 1)) {
			sym.set(predicateIndex.getObject(i).getPredicateSymmetricId());

		}

		return sym;
	}

	public String toDCstring(BitSet dc) {

		List<String> preds = new ArrayList<String>();

		for (int i = dc.nextSetBit(0); i != -1; i = dc.nextSetBit(i + 1)) {
			preds.add(predicateIndex.getObject(i).getPredicate().toString());

		}

		String strDC = "not(" + String.join(" and ", preds).toLowerCase() +")";

		return strDC;
	}

}
