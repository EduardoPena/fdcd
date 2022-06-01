package br.edu.utfpr.pena.fdcd.algorithms.enumeration.incs.twocol;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.ColumnPredicateGroup;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.groups.twocol.TwoColumnPredicateGroup;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.TwoColumnPredicateSpace;
import br.edu.utfpr.pena.fdcd.utils.bitset.BitUtils;
import br.edu.utfpr.pena.fdcd.utils.bitset.IBitSet;
import br.edu.utfpr.pena.fdcd.utils.bitset.LongBitSet;
import br.edu.utfpr.pena.fdcd.utils.misc.Object2IndexMapper;
import br.edu.utfpr.pena.fdcd.utils.search.NTreeSearch;
import br.edu.utfpr.pena.fdcd.utils.search.TreeSearch;

public class TwoColINCSExecutor {

	private TwoColumnPredicateSpace pSpace;
	private Set<BitSet> eviSet;
	private Map<Integer, Set<Integer>> pred2PredGroupMap;
	private Set<BitSet> covers;

	// evidence indexes
	private BitSet initialEviIDs;
	private Object2IndexMapper<BitSet> eviIndex; // each evi has an ID
	private Map<Integer, BitSet> pred2EviMap;// for each predicate p, the eviIDs of evidence containing p

	private boolean checkMinimals;

	private boolean parallel;

	long startTime, endTime, timeElapsed;

	protected List<Integer> initialSortedPreds;
	protected Set<Integer> addablePredicates;

	public TwoColINCSExecutor(List<Integer> currPreds, TwoColumnPredicateSpace predicateSpace,
			Set<BitSet> evidenceSet) {
		this(currPreds, predicateSpace, evidenceSet, true, true);
	}

	public TwoColINCSExecutor(List<Integer> currPreds, TwoColumnPredicateSpace predicateSpace, Set<BitSet> evidenceSet,
			boolean parallel) {
		this(currPreds, predicateSpace, evidenceSet, true, parallel);
	}

	// main constructor
	public TwoColINCSExecutor(List<Integer> currPreds, TwoColumnPredicateSpace predicateSpace, Set<BitSet> evidenceSet,
			boolean checkMinimals, boolean parallel) {
		this.pSpace = predicateSpace;

		this.eviSet = evidenceSet;

		this.checkMinimals = checkMinimals;

		this.parallel = parallel;

		this.covers = new HashSet<>();

		this.addablePredicates = new HashSet<>(currPreds);
		this.initialSortedPreds = new ArrayList<>(currPreds);

		// helper for removing trivial predicates (of the same predGroup))
		pred2PredGroupMap = new HashMap<>();

		for (ColumnPredicateGroup pGroup : pSpace.getOneColspace().getPredicateGroups()) {

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

		for (TwoColumnPredicateGroup pGroup : pSpace.getTwoColCatpredGroups()) {

			Set<Integer> pids = pGroup.getPids();

			for (int i : pids) {
				pred2PredGroupMap.put(i, pids);
			}
		}
		
		for (TwoColumnPredicateGroup pGroup : pSpace.getTwoColNumericalpredGroups()) {

			Set<Integer> pids = pGroup.getPids();

			for (int i : pids) {
				pred2PredGroupMap.put(i, pids);
			}
		}

	}

	public TwoColINCSExecutor(TwoColINCSExecutor base) {

		this.pSpace = base.pSpace;
		this.eviSet = base.eviSet;
		this.pred2EviMap = base.pred2EviMap;
		this.eviIndex = base.eviIndex;
		this.pred2PredGroupMap = base.pred2PredGroupMap;

		this.covers = new HashSet<>();
	}

	public Set<BitSet> searchDCs() {

		Set<BitSet> allCovers = ConcurrentHashMap.newKeySet();

		buildEvidenceIndexes();

		List<Integer> predIdSequence = new ArrayList<>();

		for (int pid = 0; pid < addablePredicates.size(); pid++) {
			predIdSequence.add(pid);
		}

		sortInitialPredicates(initialSortedPreds, initialEviIDs);

		Stream<Integer> stream;

		if (parallel)
			stream = predIdSequence.parallelStream();

		else
			stream = predIdSequence.stream();

		stream.forEach(i -> {

			int pid = initialSortedPreds.get(i);

			if (pred2EviMap.get(pid).cardinality() == 0) {
				BitSet dc = new BitSet();
				dc.set(pid);
				allCovers.add(dc);
				return;
			}

			BitSet path = BitUtils.buildBitSet(pid);

			List<Integer> currPreds = new ArrayList<>(initialSortedPreds);
			currPreds = currPreds.subList(i + 1, currPreds.size());
			currPreds.removeAll(pred2PredGroupMap.get(pid));

			BitSet currEvis = pred2EviMap.get(pid);

			TwoColINCSExecutor mcSearch = new TwoColINCSExecutor(this);

			mcSearch.searchCover(path, currPreds, currEvis);

			allCovers.addAll(mcSearch.covers);

		});

		covers = allCovers;

//		if (checkMinimals)
//			checkMinimality();

		return covers;
	}

	private void searchCover(BitSet currPath, List<Integer> currValidPreds, BitSet currEvis) {

		// Base cases
		if (currEvis.isEmpty()) {

			covers.add(currPath); // maybe a prefix tree

			return;

		} else if (!currEvis.isEmpty() & currValidPreds.isEmpty()) {
			return; // no DCs in this branch
		}

		sortPredicates(currValidPreds, currEvis);

		for (int i = 0; i < currValidPreds.size(); i++) {

			int pid = currValidPreds.get(i);

			List<Integer> newCurrPreds = new ArrayList<>(currValidPreds);
			newCurrPreds = newCurrPreds.subList(i + 1, newCurrPreds.size());
			newCurrPreds.removeAll(pred2PredGroupMap.get(pid));

			BitSet newCurrEvis = filterCoveredEvidences(currEvis, pid, newCurrPreds);

			if (newCurrEvis != null) {
				BitSet newPath = BitUtils.buildBitSet(pid);
				newPath.or(currPath);
				searchCover(newPath, newCurrPreds, newCurrEvis);
			}

		}

	}

	private void sortPredicates(List<Integer> currPreds, BitSet currEvis) {

		long predCounts[] = new long[pSpace.size()];

		Set<Integer> indiferentPreds = new HashSet<>();

		// get counts for each evidence
		for (int pid : currPreds) {
			BitSet andResul = BitUtils.getAndBitSet(currEvis, pred2EviMap.get(pid));

			if (andResul.cardinality() == currEvis.cardinality()) {
				indiferentPreds.add(pid);

			}

			predCounts[pid] = andResul.cardinality();
		}

		currPreds.removeAll(indiferentPreds);

		currPreds.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return Long.compare(predCounts[o1], predCounts[o2]);// ascending, we want the least frequent first
			}
		});

	}

	private void sortInitialPredicates(List<Integer> currPreds, BitSet currEvis) {

		long predCounts[] = new long[pSpace.size()];

		// get counts for each evidence
		for (int pid : currPreds) {
			BitSet andResul = BitUtils.getAndBitSet(currEvis, pred2EviMap.get(pid));

			predCounts[pid] = andResul.cardinality();

		}

		currPreds.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return Long.compare(predCounts[o1], predCounts[o2]);// ascending, we want the least frequent first
			}
		});

	}

	private BitSet filterCoveredEvidences(BitSet evis, int pid, List<Integer> newCurrPreds) {

		BitSet filteredEvis = (BitSet) evis.clone();
		filteredEvis.and(pred2EviMap.get(pid));

		BitSet preds = new BitSet();
		newCurrPreds.forEach(i -> preds.set(i));

		for (int eviID = filteredEvis.nextSetBit(0); eviID >= 0; eviID = filteredEvis.nextSetBit(eviID + 1)) {
			// the remaining predicates cannot form a DC as they are all contained in an
			// evidence
			if (BitUtils.isSubSetOf(preds, eviIndex.getObject(eviID))) {
				return null;
			}
		}

		return filteredEvis;
	}

	private void buildEvidenceIndexes() {

		pred2EviMap = new HashMap<>();

		for (int pid : addablePredicates) {
			pred2EviMap.put(pid, new BitSet());
		}

		initialEviIDs = new BitSet();

		eviIndex = new Object2IndexMapper<BitSet>();

		List<BitSet> eviList = minimize(eviSet);

		for (BitSet evi : eviList) {

			int eviID = eviIndex.getIndex(evi);

			initialEviIDs.set(eviID);

			for (int pID = evi.nextSetBit(0); pID >= 0; pID = evi.nextSetBit(pID + 1)) {

				pred2EviMap.get(pID).set(eviID);
			}

		}

	}

	private List<BitSet> minimize(final Set<BitSet> evis) {

		List<IBitSet> sortedNegCover = new ArrayList<>();
		evis.forEach(evi -> sortedNegCover.add(LongBitSet.FACTORY.create(evi)));

		Collections.sort(sortedNegCover, new Comparator<IBitSet>() {
			@Override
			public int compare(IBitSet o1, IBitSet o2) {
				int erg = Integer.compare(o2.cardinality(), o1.cardinality());
				return erg != 0 ? erg : o2.compareTo(o1);
			}
		});

		TreeSearch neg = new TreeSearch();
		sortedNegCover.stream().forEach(invalid -> addInvalidToNeg(neg, invalid));

		final ArrayList<BitSet> list = new ArrayList<>();

		neg.forEach(invalidFD -> list.add(invalidFD.toBitSet()));

		return list;
	}

	private void addInvalidToNeg(TreeSearch neg, IBitSet invalid) {
		if (neg.findSuperSet(invalid) != null)
			return;

		neg.getAndRemoveGeneralizations(invalid);
		neg.add(invalid);
	}

	private void checkMinimality() {

		NTreeSearch nt = new NTreeSearch();
		for (BitSet key : covers) {
			nt.add(LongBitSet.FACTORY.create(key));
		}

		Set<BitSet> nonGeneralized = new HashSet<>();

		for (BitSet key : covers) {

			IBitSet bs = LongBitSet.FACTORY.create(key);

			nt.remove(bs);

			if (!nt.containsSubset(bs)) {

				nonGeneralized.add(key);
			}

			nt.add(bs);

		}
		covers = nonGeneralized;
	}

}
