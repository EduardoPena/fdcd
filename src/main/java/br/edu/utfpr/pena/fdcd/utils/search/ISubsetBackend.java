package br.edu.utfpr.pena.fdcd.utils.search;

import java.util.Set;
import java.util.function.Consumer;

import br.edu.utfpr.pena.fdcd.utils.bitset.IBitSet;

public interface ISubsetBackend {

	boolean add(IBitSet bs);

	Set<IBitSet> getAndRemoveGeneralizations(IBitSet invalidFD);

	boolean containsSubset(IBitSet add);

	void forEach(Consumer<IBitSet> consumer);

}
