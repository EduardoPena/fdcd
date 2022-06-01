package br.edu.utfpr.pena.fdcd.utils.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Object2IndexMapper<T> {

	private Map<T, Integer> indexes = new HashMap<>();// object id is incremental

	private List<T> objects = new ArrayList<>(); // the id of the object is its position in the array (serves as an inverted index)

	private int nextIndex = 0;

	public Integer getIndex(T object) {
		Integer index = indexes.putIfAbsent(object, Integer.valueOf(nextIndex));
		if (index == null) {
			index = Integer.valueOf(nextIndex);
			++nextIndex;
			objects.add(object);
		}
		return index;
	}

	public T getObject(int index) {
		return objects.get(index);
	}

	public int size() {
		return nextIndex;
	}

	public List<T> getObjects() {
		return objects;
	}

}
