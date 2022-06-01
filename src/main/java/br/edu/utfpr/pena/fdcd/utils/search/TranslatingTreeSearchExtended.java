package br.edu.utfpr.pena.fdcd.utils.search;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.space.PredicateSpace;
import br.edu.utfpr.pena.fdcd.utils.bitset.IBitSet;
import br.edu.utfpr.pena.fdcd.utils.search.helpers.ArrayIndexComparator;
import br.edu.utfpr.pena.fdcd.utils.search.helpers.BitSetTranslator;

public class TranslatingTreeSearchExtended implements ITreeSearch {

	private NTreeSearch search = new NTreeSearch();

	private BitSetTranslator translator;
	private Collection<IBitSet> bitsetListTransformed; // per predicate group

	private PredicateSpace pSpace;

	public TranslatingTreeSearchExtended(int[] priorities, List<IBitSet> bitsetList, PredicateSpace pSpace) {
		ArrayIndexComparator comparator = new ArrayIndexComparator(priorities, ArrayIndexComparator.Order.DESCENDING);
		this.translator = new BitSetTranslator(comparator.createIndexArray());
		this.bitsetListTransformed = translator.transform(bitsetList);
		this.pSpace = pSpace;
	}

	@Override
	public boolean add(IBitSet bs) {
		IBitSet translated = translator.bitsetTransform(bs);
		return search.add(translated);
	}

	@Override
	public void forEachSuperSet(IBitSet bitset, Consumer<IBitSet> consumer) {
		search.forEachSuperSet(bitset, superset -> consumer.accept(translator.bitsetRetransform(superset)));
	}

	@Override
	public void forEach(Consumer<IBitSet> consumer) {
		search.forEach(bitset -> consumer.accept(translator.bitsetRetransform(bitset)));
	}

	@Override
	public void remove(IBitSet remove) {
		search.remove(translator.bitsetTransform(remove));
	}

	@Override
	public boolean containsSubset(IBitSet bitset) {
		return search.containsSubset(translator.bitsetTransform(bitset));
	}

	@Override
	public Collection<IBitSet> getAndRemoveGeneralizations(IBitSet invalidDC) {
		Set<IBitSet> temp = search.getAndRemoveGeneralizations(invalidDC);
		return translator.retransform(temp);
	}

	public Comparator<IBitSet> getComparator() {
		return new Comparator<IBitSet>() {

			@Override
			public int compare(IBitSet o1, IBitSet o2) {
				int erg = Integer.compare(o2.cardinality(), o1.cardinality());
				return erg != 0 ? erg : translator.bitsetTransform(o2).compareTo(translator.bitsetTransform(o1));

			}
		};

	}

//	public static long timeCheckIsNot = 0;
//	public static long timeCheckIs = 0;
//
//	long startTime, endTime, timeElapsed;

	public void handleInvalid(IBitSet invalidDCU) {

		IBitSet invalidDC = translator.bitsetTransform(invalidDCU);
		Collection<IBitSet> remove = search.getAndRemoveGeneralizations(invalidDC);

		for (IBitSet removed : remove) {

			for (IBitSet bitset : bitsetListTransformed) {
				IBitSet temp = removed.clone();
				temp.and(bitset);

				// already one bit in block set?
				if (temp.isEmpty()) {

					IBitSet valid = bitset.clone();
					valid.andNot(invalidDC); // valid predicates allowed

					for (int i = valid.nextSetBit(0); i >= 0; i = valid.nextSetBit(i + 1)) {
						IBitSet add = removed.clone();
						add.set(i);

//						startTime = System.currentTimeMillis();

						if (!search.containsSubset(add)) {

							search.add(add);

//							endTime = System.currentTimeMillis();
//							timeElapsed = endTime - startTime;
//
//							timeCheckIs += timeElapsed;
						}

//						else {
//
//							endTime = System.currentTimeMillis();
//							timeElapsed = endTime - startTime;
//
//							timeCheckIsNot += timeElapsed;
//
//						}

					}

//					}
				}
			}

		}

	}

//	public void handleInvalid2(IBitSet invalidDCU) {
//
//		IBitSet invalidDC = translator.bitsetTransform(invalidDCU);
//		Collection<IBitSet> remove = search.getAndRemoveGeneralizations(invalidDC);
//
//		for (IBitSet removed : remove) {
//
//			for (IBitSet bitset : bitsetListTransformed) {
//				IBitSet temp = removed.clone();
//				temp.and(bitset);
//
//				// already one bit in block set?
//				if (temp.isEmpty()) {
//
//					IBitSet valid = bitset.clone();
//					valid.andNot(invalidDC); // valid predicates allowed
//
////					if (!search.containsSubset(valid)) {
//
//					for (int i = valid.nextSetBit(0); i >= 0; i = valid.nextSetBit(i + 1)) {
//						IBitSet add = removed.clone();
//						add.set(i);
//
//						startTime = System.currentTimeMillis();
//
//						if (!search.containsSubset(add)) {
//
//							search.add(add);
//
//							endTime = System.currentTimeMillis();
//							timeElapsed = endTime - startTime;
//
//							timeCheckIs += timeElapsed;
//						} else {
//
//							endTime = System.currentTimeMillis();
//							timeElapsed = endTime - startTime;
//
//							timeCheckIsNot += timeElapsed;
//
//						}
//
//					}
//
////					}
//				}
//			}
//
//		}
//
//	}

}
