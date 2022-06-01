package br.edu.utfpr.pena.fdcd.algorithms.ecp.evidence;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.roaringbitmap.RoaringBitmap;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.PredicateSpace;
import br.edu.utfpr.pena.fdcd.utils.bitset.IBitSet;
import br.edu.utfpr.pena.fdcd.utils.bitset.LongBitSet;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public class EvidenceSet {

	private Object2LongOpenHashMap<BitSet> evidence2CountMap;

	private Map<BitSet, RoaringBitmap> evidence2Tuples;

	private long eviCounter;

	private PredicateSpace predicateSpace;

	public EvidenceSet(PredicateSpace predicateSpace) {
		this.evidence2CountMap = new Object2LongOpenHashMap<>();
		this.evidence2Tuples = new HashMap<>();
		this.eviCounter = 0;
		this.predicateSpace = predicateSpace;

	}

	public void addOneEvidence(BitSet evidence) {
		evidence2CountMap.addTo(evidence, 1);
		eviCounter++;

	}

	public void addEvidenceWithMultiplicity(BitSet evidence, long multiplicity) {

		evidence2CountMap.addTo(evidence, multiplicity);
		eviCounter += multiplicity;
	}

	public void addEvidenceWithMultiplicityRetainTuples(BitSet evidence, long multiplicity, int tid,
			RoaringBitmap newTuples) {

		evidence2CountMap.addTo(evidence, multiplicity);
		eviCounter += multiplicity;

		RoaringBitmap tids = evidence2Tuples.computeIfAbsent(evidence, bitmap -> new RoaringBitmap());
		tids.or(newTuples);
		tids.add(tid);

	}

	public Object2LongOpenHashMap<BitSet> getEvidence2CountMap() {
		return evidence2CountMap;
	}

	public void addPartialEvidenceSet(EvidenceSet partialEvidenceSet) {

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
		EvidenceSet other = (EvidenceSet) obj;
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

	public void remove(BitSet wrongEvi) {
		evidence2CountMap.removeLong(wrongEvi);

	}

	public Object2LongOpenHashMap<IBitSet> toIBitSetEviMap() {


		Object2LongOpenHashMap<IBitSet> eviMap = new Object2LongOpenHashMap<>();

		for (Object2LongMap.Entry<BitSet> entry : evidence2CountMap.object2LongEntrySet()) {

			IBitSet bs = LongBitSet.FACTORY.create(entry.getKey());

			eviMap.put(bs, entry.getLongValue());

		}



		return eviMap;
	}

}
