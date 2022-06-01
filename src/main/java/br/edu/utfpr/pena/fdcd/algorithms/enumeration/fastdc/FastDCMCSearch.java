package br.edu.utfpr.pena.fdcd.algorithms.enumeration.fastdc;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.evidence.EvidenceSet;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.ColumnPredicateGroup;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.PredicateSpace;
import br.edu.utfpr.pena.fdcd.utils.bitset.BitUtils;
import br.edu.utfpr.pena.fdcd.utils.bitset.IBitSet;
import br.edu.utfpr.pena.fdcd.utils.bitset.LongBitSet;
import br.edu.utfpr.pena.fdcd.utils.search.TreeSearch;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public class FastDCMCSearch {

	private PredicateSpace pSpace;

	private Object2LongOpenHashMap<IBitSet> eviSet;

	private Map<Integer, Set<Integer>> pred2PredGroupMap;

	private TreeSearch MC;

	private Set<BitSet> dcSet;

	public FastDCMCSearch(PredicateSpace predicateSpace, EvidenceSet evidenceSet) {
		this.pSpace = predicateSpace;

		this.eviSet = transform2IBitsets(evidenceSet.getEvidence2CountMap());

		this.MC = new TreeSearch();

		this.dcSet = new HashSet<>();

		// helper for removing trivial predicates (of the same predGroup))
		pred2PredGroupMap = new HashMap<>();
		for (ColumnPredicateGroup pGroup : pSpace.getPredicateGroups()) {
			Set<Integer> pids = new HashSet<>();

			for (int i = pGroup.getPredicateIDs().nextSetBit(0); i != -1; i = pGroup.getPredicateIDs()
					.nextSetBit(i + 1)) {
				pids.add(i);
			}

			for (int i = pGroup.getPredicateIDs().nextSetBit(0); i != -1; i = pGroup.getPredicateIDs()
					.nextSetBit(i + 1)) {
				pred2PredGroupMap.put(i, pids);

			}
		}

	}

	public Set<BitSet> searchDCs() {

		Set<IBitSet> initialEvis = new HashSet<>();

		for (IBitSet evi : eviSet.keySet())
			initialEvis.add(evi);

		List<Integer> initialPreds = new ArrayList<>();
		for (int pid = 0; pid < pSpace.size(); pid++)
			initialPreds.add(pid);

		sortPredicates(initialPreds, initialEvis);

		for (int i = 0; i < initialPreds.size(); i++) {

			int pid = initialPreds.get(i);

			IBitSet path = BitUtils.buildIBitSet(pid);

			List<Integer> currPreds = new ArrayList<>(initialPreds);
			currPreds = currPreds.subList(i + 1, currPreds.size());
			currPreds.removeAll(pred2PredGroupMap.get(pid));

			Set<IBitSet> currEvis = filterCoveredEvidences(initialEvis, pid);

			searchMinimalCover(pid, path, currPreds, currEvis);

		}

		return dcSet;
	}

	private void searchMinimalCover(int lastPredicate, IBitSet currPath, List<Integer> currValidPreds,
			Set<IBitSet> currEvis) {

		// Base cases
		if (currValidPreds.isEmpty() && !currEvis.isEmpty())
			return; // no DCs in this branch

		if (currEvis.isEmpty()) {

			if (!subsetCovers(currPath)) {

				MC.add(currPath);
				IBitSet dc = convert2DC(currPath);
				dcSet.add(dc.toBitSet());

				return;
			}
		}

		sortPredicates(currValidPreds, currEvis);

		for (int i = 0; i < currValidPreds.size(); i++) {

			int pid = currValidPreds.get(i);

			List<Integer> newCurrPreds = new ArrayList<>(currValidPreds);
			newCurrPreds = newCurrPreds.subList(i + 1, newCurrPreds.size());
			newCurrPreds.removeAll(pred2PredGroupMap.get(pid));

			Set<IBitSet> newCurrEvis = filterCoveredEvidences(currEvis, pid);

			IBitSet newPath = BitUtils.buildIBitSet(pid);
			newPath.or(currPath);

			if (newCurrEvis == null) {
				return;
			}

			searchMinimalCover(pid, newPath, newCurrPreds, newCurrEvis);

		}

	}

	private boolean subsetCovers(IBitSet currPath) {

		subSetLoop: for (int i = currPath.nextSetBit(0); i != -1; i = currPath.nextSetBit(i + 1)) {

			IBitSet subset = currPath.clone();
			subset.clear(i);

			for (IBitSet evi : eviSet.keySet()) {

				if (subset.getAndCardinality(evi) == 0) {// current subset have evidence satisfying all predicates
					continue subSetLoop;
				}
			}

			return true; // did not find a evidence that satisfy all

		}

		return false;

	}

	private IBitSet convert2DC(IBitSet currPath) {

		IBitSet bs = LongBitSet.FACTORY.create();

		for (int i = currPath.nextSetBit(0); i != -1; i = currPath.nextSetBit(i + 1)) {
			bs.set(pSpace.getPredicateById(i).getInverse().getPredicateId());
		}

		return bs;
	}

	private void sortPredicates(List<Integer> currPreds, Set<IBitSet> currEvis) {

		long predCounts[] = new long[pSpace.size()];

		// get counts for each evidence
		for (IBitSet evi : currEvis) {

			// long eviCount = eviSet.getLong(evi);

			for (int i = evi.nextSetBit(0); i != -1; i = evi.nextSetBit(i + 1)) {
				// predCounts[i] += eviCount;
				predCounts[i]++;
			}
		}

		currPreds.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return Long.compare(predCounts[o2], predCounts[o1]);
			}
		});

	}

	private Set<IBitSet> filterCoveredEvidences(Set<IBitSet> evis, int pid) {

		Set<IBitSet> filteredEvis = new HashSet<>();

		for (IBitSet evi : evis) {
			if (!evi.get(pid)) {
				filteredEvis.add(evi);
			}
		}

		return filteredEvis;
	}

	private Object2LongOpenHashMap<IBitSet> transform2IBitsets(Object2LongOpenHashMap<BitSet> evidence2CountMap) {
		Object2LongOpenHashMap<IBitSet> eviMap = new Object2LongOpenHashMap<>();

		for (Object2LongMap.Entry<BitSet> entry : evidence2CountMap.object2LongEntrySet()) {

			IBitSet bs = LongBitSet.FACTORY.create(entry.getKey());

			eviMap.put(bs, entry.getLongValue());

		}
		return eviMap;
	}

}
