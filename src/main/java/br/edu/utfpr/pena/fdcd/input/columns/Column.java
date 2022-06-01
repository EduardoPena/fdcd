package br.edu.utfpr.pena.fdcd.input.columns;

import java.util.ArrayList;

import br.edu.utfpr.pena.fdcd.utils.misc.MurmurHash3;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.agkn.hll.HLL;

public abstract class Column {

	public static final int DEFAULT_INITIAL_LIST_CAPACITY = 10000;
	
	public static final String DEFAULT_NULL_STRING = "<NULL>";
	
	public static final Float DEFAULT_NULL_NUMBER = Float.MIN_VALUE;

	public enum ColumnPrimitiveType {
		INT, FLOAT, STR
	};

	public final String TableName;
	public final String ColumnName;
	public final int ColumnIndex;
	public final ColumnPrimitiveType ColumnType;

	protected FloatList valuesList;

	protected HLL hll;
	protected long cardinality = 0;
	
	

	public abstract void addValues(ArrayList<String> values);
	
	public abstract boolean containsSameValue(Column c1, double percentage);
	
	
	

	public Column(String tableName, String colName, int colIndex, ColumnPrimitiveType colType) {
		this.TableName = tableName;
		this.ColumnName = colName;
		this.ColumnIndex = colIndex;
		this.ColumnType = colType;
		// this.nextTid = 0;

		valuesList = new FloatArrayList(DEFAULT_INITIAL_LIST_CAPACITY);
	}

	public Column(String tableName, String colName, int colIndex, ColumnPrimitiveType colType, FloatList valuesList) {
		this.TableName = tableName;
		this.ColumnName = colName;
		this.ColumnIndex = colIndex;
		this.ColumnType = colType;
		// this.nextTid = 0;

		this.valuesList = valuesList;
	}

	public float getValueAt(int i) {

		return valuesList.getFloat(i);

	}

//	public abstract String getStrValueAt(int tid);

	public int size() {

		return valuesList.size();
	}

	public long getCardinality() {

		if (hll == null) {

			hll = new HLL(13/* log2m */, 5/* registerWidth */);

			for (float value : valuesList) {

				hll.addRaw(MurmurHash3.fmix64(Float.floatToRawIntBits(value)));

			}

			cardinality = hll.cardinality();

		}

		return cardinality;
	}

	public FloatList getValuesList() {
		return valuesList;
	}

	public void setValuesList(FloatList orderedValues) {
		this.valuesList = orderedValues;
	}

	public void setValuesListBuildNewHLL(FloatList newValues) {
		this.valuesList = newValues;
		hll = new HLL(13/* log2m */, 5/* registerWidth */);

		for (float value : valuesList) {

			hll.addRaw(MurmurHash3.fmix64(Float.floatToRawIntBits(value)));

		}

		cardinality = hll.cardinality();

	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		// sb.append(TableName + "." + ColumnName);
		sb.append(ColumnName);
//		if (valuesList != null && !valuesList.isEmpty())
//			sb.append(": " + valuesList);

		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ColumnIndex;
		result = prime * result + ((ColumnName == null) ? 0 : ColumnName.hashCode());
		result = prime * result + ((ColumnType == null) ? 0 : ColumnType.hashCode());
		result = prime * result + ((TableName == null) ? 0 : TableName.hashCode());
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
		Column other = (Column) obj;
		if (ColumnIndex != other.ColumnIndex)
			return false;
		if (ColumnName == null) {
			if (other.ColumnName != null)
				return false;
		} else if (!ColumnName.equals(other.ColumnName))
			return false;
		if (ColumnType != other.ColumnType)
			return false;
		if (TableName == null) {
			if (other.TableName != null)
				return false;
		} else if (!TableName.equals(other.TableName))
			return false;
		return true;
	}

	public String getSQLTypedColString() {

		return "\"" + ColumnName + " " + ColumnType + "\" " + mapToSQLType(ColumnType);
	}

	public String getCSVTypedColString() {

		return "\"" + ColumnName + " " + ColumnType + "\"";
	}

	public String getSQLServerTypedColString() {

		return ColumnName + " " + mapToSQLType(ColumnType);
	}

	private String mapToSQLType(ColumnPrimitiveType columnType) {

		switch (columnType) {
		case INT:
			return "integer";
		case FLOAT:
			return "decimal(12,4)";
		case STR:
			return "varchar(500)";

		}

		return null;

	}

	public abstract void clearValuesCountMap();

}