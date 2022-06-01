package br.edu.utfpr.pena.fdcd.input.columns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.floats.FloatList;

public class NumericalColumn extends Column {

	private float minimumValue, maximumValue;

	private Map<Float, Integer> floatValues;
	
	public NumericalColumn(String tableName, String name, int index, ColumnPrimitiveType colType) {

		super(tableName, name, index, colType);
		minimumValue = 0;
		maximumValue = 0;

	}

	public NumericalColumn(String tableName, String columnName, int colID, ColumnPrimitiveType colType,
			FloatList copiedValuesList) {

		super(tableName, columnName, colID, colType, copiedValuesList);

	}

	public void addValues(ArrayList<String> values) {

		for (String sval : values) {

			float fval;

			// if (sval.isEmpty()||sval.isBlank()||sval.contentEquals("NaN")) {
			//if (sval.isEmpty() || sval.isBlank()) {
			if (sval.isEmpty()) {
				// fval = Float.MIN_VALUE;

				// fval = 0;
				fval = DEFAULT_NULL_NUMBER;

			} else {
//				System.out.println(sval);

				fval = Float.parseFloat(sval);

			}

			if (valuesList.isEmpty()) {
				minimumValue = fval;
				maximumValue = fval;
			}

			if (minimumValue > fval)

				minimumValue = fval;

			if (maximumValue < fval)
				maximumValue = fval;

			valuesList.add(fval);
		}
	}
	
	
	public boolean containsSameValue(Column otherColumn, double percentage) {

		if (!otherColumn.ColumnType.equals(this.ColumnType))
			return false;

		Map<Float, Integer> thisCounts = getStringValues();

		Map<Float, Integer> otherColumnCounts = ((NumericalColumn) otherColumn).getStringValues();

		int totalCount = 0;
		int sharedCount = 0;
		for (Float  s : thisCounts.keySet()) {
			int thisCount = thisCounts.get(s);
			Integer otherCount = otherColumnCounts.get(s);
			if(otherCount==null)
				otherCount=0;
			
			sharedCount += Math.min(thisCount, otherCount);
			totalCount += Math.max(thisCount, otherCount);
		}
		
		return ((double) sharedCount) / ((double) totalCount) > percentage ? true : false;

	}

	public Map<Float, Integer> getStringValues() {

		if (floatValues == null) {
			floatValues = new HashMap<>();

			for (float f : valuesList) {
				
				int count = floatValues.containsKey(f) ? floatValues.get(f) : 0;
				floatValues.put(f, count + 1);
			}
		}

		return floatValues;
	}
	
	
	

	public float getMinimumValue() {
		return minimumValue;
	}

	public void setMinimumValue(float minimumValue) {
		this.minimumValue = minimumValue;
	}

	public float getMaximumValue() {
		return maximumValue;
	}

	public void setMaximumValue(float maximumValue) {
		this.maximumValue = maximumValue;
	}

	@Override
	public void clearValuesCountMap() {
		floatValues=null;
		
	}

}