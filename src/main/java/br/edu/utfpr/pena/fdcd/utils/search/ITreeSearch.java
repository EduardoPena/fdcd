package br.edu.utfpr.pena.fdcd.utils.search;

import java.util.Collection;
import java.util.function.Consumer;

import br.edu.utfpr.pena.fdcd.utils.bitset.IBitSet;

public interface ITreeSearch {

	boolean add(IBitSet bs);

	void forEachSuperSet(IBitSet bitset, Consumer<IBitSet> consumer);

	void forEach(Consumer<IBitSet> consumer);

	void remove(IBitSet remove);

	boolean containsSubset(IBitSet bitset);

	Collection<IBitSet> getAndRemoveGeneralizations(IBitSet invalidDC);

}