package br.edu.utfpr.pena.fdcd.utils.search;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import br.edu.utfpr.pena.fdcd.utils.bitset.IBitSet;
import br.edu.utfpr.pena.fdcd.utils.search.helpers.ArrayIndexComparator;
import br.edu.utfpr.pena.fdcd.utils.search.helpers.BitSetTranslator;

public class TranslatingTreeSearch implements ITreeSearch {

	private NTreeSearch search = new NTreeSearch();

	private BitSetTranslator translator;
	private Collection<IBitSet> bitsetListTransformed;

	public TranslatingTreeSearch(int[] priorities, List<IBitSet> bitsetList) {
		ArrayIndexComparator comparator = new ArrayIndexComparator(priorities, ArrayIndexComparator.Order.DESCENDING); // use
																														// the
																														// predicate
																														// frequency
																														// to
																														// sort
																														// and
																														// organize
																														// the
																														// tree
		this.translator = new BitSetTranslator(comparator.createIndexArray());
		this.bitsetListTransformed = translator.transform(bitsetList);
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
//
//	public static long start, finish;
//	public static long treeSearch=0;
//	public static long transform=0;
	
	public void handleInvalid(IBitSet invalidDCU) {

		
		
		IBitSet invalidDC = translator.bitsetTransform(invalidDCU);
		
//		start = System.currentTimeMillis();
		
		
		Collection<IBitSet> remove = search.getAndRemoveGeneralizations(invalidDC);
		
//		finish = System.currentTimeMillis();
//		
//		transform+=finish-start;


		for (IBitSet removed : remove) {

			for (IBitSet bitset : bitsetListTransformed) {
				IBitSet temp = removed.clone();
				temp.and(bitset);

				// already one bit in block set?
				if (temp.isEmpty()) {
					IBitSet valid = bitset.clone();
					valid.andNot(invalidDC);
					for (int i = valid.nextSetBit(0); i >= 0; i = valid.nextSetBit(i + 1)) {
						IBitSet add = removed.clone();
						add.set(i);

//						start = System.currentTimeMillis();
						
						if (!search.containsSubset(add)) {

							search.add(add);
						}
						
//						finish = System.currentTimeMillis();
//						
//						treeSearch+=finish-start;

					}
				}
			}
		}
	}

}
