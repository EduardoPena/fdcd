package br.edu.utfpr.pena.fdcd.input.columns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import br.edu.utfpr.pena.fdcd.utils.misc.UnsortedObject2FloatMapProvider;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.FloatList;

public class CategoricalColumn extends Column {

	private UnsortedObject2FloatMapProvider<String> floatDictionary; // to save memory

	private boolean useSimilarity;

	private Map<String, Integer> stringValues;

	public CategoricalColumn(String tableName, String name, int index, ColumnPrimitiveType colType) {

		super(tableName, name, index, colType);

		floatDictionary = new UnsortedObject2FloatMapProvider<String>();
		this.useSimilarity = false;
	}

	public CategoricalColumn(String tableName, String name, int index, ColumnPrimitiveType colType,
			boolean useSimilarity) {

		super(tableName, name, index, colType);

		floatDictionary = new UnsortedObject2FloatMapProvider<String>();
		this.useSimilarity = useSimilarity;
	}

	public CategoricalColumn(String tableName, String columnName, int colID, ColumnPrimitiveType colType,
			UnsortedObject2FloatMapProvider<String> copiedfloatDictionary, FloatList copiedValuesList) {
		super(tableName, columnName, colID, colType, copiedValuesList);

		this.floatDictionary = copiedfloatDictionary;
		this.useSimilarity = false;

	}

	public void addValues(ArrayList<String> values) {

		for (String value : values) {

			//if (value.isEmpty() || value.isBlank()) {
			if (value.isEmpty() ) {
				value = DEFAULT_NULL_STRING;
			}

			float floatValue = floatDictionary.createOrGetFloat(value);
			valuesList.add(floatValue);
		}
	}

	public UnsortedObject2FloatMapProvider<String> getFloatDictionary() {
		return floatDictionary;
	}

	public void setFloatDictionary(UnsortedObject2FloatMapProvider<String> floatDictionary) {
		this.floatDictionary = floatDictionary;
	}

	@Override
	public int size() {
		return valuesList.size();
	}

	public Float2ObjectMap<String> buildReverseDictionaryMap() {

		return floatDictionary.getReversedMap();
	}

	public boolean containsSameValue(Column otherColumn, double percentage) {

		if (!otherColumn.ColumnType.equals(this.ColumnType))
			return false;

		Map<String, Integer> thisCounts = getStringValues();

		Map<String, Integer> otherColumnCounts = ((CategoricalColumn) otherColumn).getStringValues();
		


		int totalCount = 0;
		int sharedCount = 0;
		for (String s : thisCounts.keySet()) {
			int thisCount = thisCounts.get(s);
			Integer otherCount = otherColumnCounts.get(s);
			if(otherCount==null)
				otherCount=0;
			sharedCount += Math.min(thisCount, otherCount);
			totalCount += Math.max(thisCount, otherCount);
		}
		
		
		
		return ((double) sharedCount) / ((double) totalCount) > percentage ? true : false;

	}

	public Map<String, Integer> getStringValues() {

		if (stringValues == null) {
			stringValues = new HashMap<>();

			for (float f : valuesList) {
				String value = floatDictionary.getObject(f);
				int count = stringValues.containsKey(value) ? stringValues.get(value) : 0;
				stringValues.put(value, count + 1);
			}
		}

		return stringValues;
	}

//	@Override
//	public String getStrValueAt(int tid) {
//		
//		return floatDictionary.getObject(index);
//	}
	
	public void clearValuesCountMap() {
		stringValues=null;
		
	}

	public boolean useSimilarity() {
		return useSimilarity;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		// sb.append(TableName + "." + ColumnName);
		sb.append(ColumnName);
		if (useSimilarity)
			sb.append("(similarity)");
//		if (valuesList != null && !valuesList.isEmpty())
//			sb.append(": " + valuesList);

		return sb.toString();
	}

}