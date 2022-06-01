package br.edu.utfpr.pena.fdcd.algorithms.ecp.evidence;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.roaringbitmap.RoaringBitmap;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.PredicateSpace;
import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.TwoColumnPredicateSpace;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public class ParallelEvidenceSet extends EvidenceSet {

	private Object2LongOpenHashMap<BitSet> evidence2CountMap;

	private Map<BitSet, RoaringBitmap> evidence2Tuples;

	private long eviCounter;

	private PredicateSpace predicateSpace;

	public ParallelEvidenceSet(PredicateSpace predicateSpace) {
		super(predicateSpace);
		this.evidence2CountMap = new Object2LongOpenHashMap<>();
		this.evidence2Tuples = new HashMap<>();
		this.eviCounter = 0;
		this.predicateSpace = predicateSpace;

	}

	public ParallelEvidenceSet(TwoColumnPredicateSpace predicateSpace) {
		super(predicateSpace.getOneColspace());
		this.evidence2CountMap = new Object2LongOpenHashMap<>();
		this.evidence2Tuples = new HashMap<>();
		this.eviCounter = 0;
		this.predicateSpace = predicateSpace.getOneColspace();

	}

	public synchronized void addOneEvidence(BitSet evidence) {
		evidence2CountMap.addTo(evidence, 1);
		eviCounter++;

	}

	public synchronized void addEvidenceWithMultiplicityRetainTuples(BitSet evidence, long multiplicity, int tid,
			RoaringBitmap newTuples) {

		evidence2CountMap.addTo(evidence, multiplicity);
		eviCounter += multiplicity;

		RoaringBitmap tids = evidence2Tuples.computeIfAbsent(evidence, bitmap -> new RoaringBitmap());
		tids.or(newTuples);
		tids.add(tid);

	}

	public synchronized void addEvidenceWithMultiplicity(BitSet evidence, long multiplicity) {

		evidence2CountMap.addTo(evidence, multiplicity);
		eviCounter += multiplicity;
	}

	public Object2LongOpenHashMap<BitSet> getEvidence2CountMap() {
		return evidence2CountMap;
	}

	public void addPartialEvidenceSet(ParallelEvidenceSet partialEvidenceSet) {

		for (Object2LongMap.Entry<BitSet> entry : partialEvidenceSet.getEvidence2CountMap().object2LongEntrySet()) {

			this.addEvidenceWithMultiplicity(entry.getKey(), entry.getLongValue());

		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		long eviCount = 0;

		for (Object2LongMap.Entry<BitSet> entry : evidence2CountMap.object2LongEntrySet()) {

			sb.append(predicateSpace.convert2PredicateStringSet(entry.getKey()));
			sb.append("=" + entry.getLongValue());
			sb.append("\n");

			eviCount += entry.getLongValue();

		}

		sb.append("EvidenceSet size=" + evidence2CountMap.size() + "\n");
		sb.append("Evicounter=" + eviCounter + " " + "Evicount=" + eviCount);

		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((evidence2CountMap == null) ? 0 : evidence2CountMap.hashCode());
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
		ParallelEvidenceSet other = (ParallelEvidenceSet) obj;
		if (evidence2CountMap == null) {
			if (other.evidence2CountMap != null)
				return false;
		} else if (!evidence2CountMap.equals(other.evidence2CountMap))
			return false;
		return true;
	}

	public Map<Set<String>, Long> getStringMappedEvidenceSet() {

		Map<Set<String>, Long> map = new HashMap<>();

		for (Object2LongMap.Entry<BitSet> entry : evidence2CountMap.object2LongEntrySet()) {

			map.put(predicateSpace.convert2PredicateStringSet(entry.getKey()), entry.getLongValue());

		}

		return map;
	}

	public int size() {

		return evidence2CountMap.size();
	}

	public long getEviCounter() {
		return eviCounter;
	}

	public void show() {

		System.out.println("Qtde:" + eviCounter);

		for (BitSet evi : evidence2Tuples.keySet()) {
			System.out.println(evidence2Tuples.get(evi).getCardinality());
		}

	}

	public void remove(BitSet wrongEvi) {
		evidence2CountMap.removeLong(wrongEvi);

	}

}
