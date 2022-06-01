package br.edu.utfpr.pena.fdcd.utils.misc;

import java.util.HashSet;
import java.util.Set;

import br.edu.utfpr.pena.fdcd.input.columns.Column;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

/*	
 * A simple Dictionary compression scheme
 * 
 * TODO CAUTION: WE DO ONLY EQUAL AND UNEQUAL
 * TO COMPARE MULTICOLUMNS THE PROVIDER MUST BE THE SAME FOR EVERY COLUMN THAT IS COMPARED TOGETHER
 * TO USE RANGE OPERATORS, WE MUST IMPLEMENT A SORTED VERSION OF THIS CLASS*/

public class UnsortedObject2FloatMapProvider<T> {

	private Object2FloatMap<T> object2FloatMap;
	private float nextIndex;

	private Set<Column> sharedColumns;

	private Float2ObjectMap<T> float2ObjectMap;
	// only if we need to reconstruct the tuple

	public UnsortedObject2FloatMapProvider() {
		object2FloatMap = new Object2FloatOpenHashMap<>();
		nextIndex = 1f;
		sharedColumns = new HashSet<>();

		float2ObjectMap = new Float2ObjectOpenHashMap<>();
	}

	public UnsortedObject2FloatMapProvider(Object2FloatMap<T> copiedObject2FloatMap, float copiedNextIndex,
			Set<Column> copiedsharedColumns) {// for deep copy
		object2FloatMap = copiedObject2FloatMap;
		this.nextIndex = copiedNextIndex;
		sharedColumns = copiedsharedColumns; // take care when changing values for shared columns ***
	}

	public float createOrGetFloat(T object) {

		float mapValue = object2FloatMap.getFloat(object);// returns 0 if it is not there

		if (mapValue == 0) {

			float newValue = ++nextIndex;

			object2FloatMap.put(object, newValue);

			float2ObjectMap.put(newValue, object); // only if we need to reconstruct the
			// // tuple

			return newValue;
		} else {

			return mapValue;

		}

	}
	
	public float getFloatForString(T object) {

		return object2FloatMap.getFloat(object);// returns 0 if it is not there

	}
	

	public float getFloatMap(T object) {

		return object2FloatMap.getFloat(object);// returns 0 if it is not there

	}

	public Object2FloatMap<T> getObject2FloatMap() {
		return object2FloatMap;
	}

	public Set<Column> getSharedColumns() {

		if (sharedColumns == null) {

			sharedColumns = new HashSet<>();
		}

		return sharedColumns;
	}

	public void addColumn(Column c) {

		getSharedColumns().add(c);

	}

	public void setSharedColumns(Set<Column> sharedColumns) {
		this.sharedColumns = sharedColumns;
	}

	public Float2ObjectMap<T> getReversedMap() {

		if (float2ObjectMap != null) {
			return float2ObjectMap;
		}

		float2ObjectMap = new Float2ObjectOpenHashMap<>();

		for (Object2FloatMap.Entry<T> entry : object2FloatMap.object2FloatEntrySet()) {

			float2ObjectMap.put(entry.getFloatValue(), entry.getKey());

		}

		return float2ObjectMap;
	}

	public UnsortedObject2FloatMapProvider<String> deepCopy() {

		// UnsortedObject2FloatMapProvider<T> copied = new
		// UnsortedObject2FloatMapProvider<T>();

		Object2FloatMap<String> copiedObject2FloatMap = new Object2FloatOpenHashMap<>();

		for (Entry<T> entry : object2FloatMap.object2FloatEntrySet()) {

			String s = (String) entry.getKey();
			float value = entry.getFloatValue();

			copiedObject2FloatMap.put(new String(s), value);

		}

		float newNextIndex = this.nextIndex;

		UnsortedObject2FloatMapProvider<String> copied = new UnsortedObject2FloatMapProvider<String>(
				copiedObject2FloatMap, newNextIndex, new HashSet<>(sharedColumns));

		return copied;
	}

	public T getObject(float floatValue) {
		return float2ObjectMap.get(floatValue);
	}
	//
	// public Float2ObjectMap<T> getInt2ObjectMap() {
	// return float2ObjectMap;
	// }

	// public static void main(String[] args) {
	//
	// Object2IntMapProvider<String> provider = new Object2IntMapProvider<String>();
	//
	// String[] valores = { "AA", "AA", "AB", "AC", "YY" };
	//
	// for (String s : valores) {
	// provider.createOrGetInt(s);
	// }
	//
	// System.out.println(provider.getObject2intMap());
	//
	// System.out.println(provider.getInt2ObjectMap());
	//
	// }

}
