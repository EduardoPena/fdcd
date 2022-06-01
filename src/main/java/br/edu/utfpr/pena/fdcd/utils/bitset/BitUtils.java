package br.edu.utfpr.pena.fdcd.utils.bitset;

import java.util.BitSet;

public class BitUtils {

	public static BitSet buildBitSet(int pid) {
		BitSet bs = new BitSet();
		bs.set(pid);
		return bs;
	}

	public static BitSet buildBitSetMinusBit(BitSet bs, int minusBit) {
		BitSet newBs = (BitSet) bs.clone();
		newBs.clear(minusBit);
		return newBs;
	}

	public static BitSet getAndBitSet(BitSet pBits, BitSet eviBits) {

		BitSet andBits;

		// TODO check if cloning smaller is even worth it
		if (pBits.size() < eviBits.size()) {
			andBits = (BitSet) pBits.clone();
			andBits.and(eviBits);
		} else {
			andBits = (BitSet) eviBits.clone();
			andBits.and(pBits);
		}

		return andBits;
	}

	public static BitSet getAndNotBitSet(BitSet bs1, BitSet bs2) {

		BitSet andNotBits = (BitSet) bs1.clone();

		andNotBits.andNot(bs2);

		return andNotBits;
	}

	public static BitSet getIntersection(BitSet bs1, BitSet bs2) {
		BitSet inter = (BitSet) bs1.clone();
		inter.and(bs2);
		return inter;
	}

	public static BitSet copyAndAddBit(BitSet oldBitset, int bit2add) {
		BitSet newBitset = (BitSet) oldBitset.clone();
		newBitset.set(bit2add);

		return newBitset;
	}

	public static boolean isContainedIn(BitSet predPath, BitSet evi) {

		BitSet bs = (BitSet) predPath.clone();

		bs.and(evi);

		return bs.cardinality() == predPath.cardinality();
	}

	// for LongBitSet
	public static IBitSet buildIBitSet(int pid) {

		IBitSet bs = LongBitSet.FACTORY.create();
		bs.set(pid);

		return bs;
	}

	public static boolean isSubSetOf(BitSet preds, BitSet evi) {

		BitSet bsAnd = (BitSet) preds.clone();
		bsAnd.and(evi);
		return bsAnd.cardinality() == preds.cardinality() ? true : false;
	}

}
