package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates;

import java.util.ArrayList;
import java.util.List;

import com.koloboke.function.FloatFloatPredicate;

import br.edu.utfpr.pena.fdcd.input.Table;
import br.edu.utfpr.pena.fdcd.input.columns.Column;



public class TupleColumnPairPredicate extends Predicate {

	private final Column col2;

	private FloatFloatPredicate evaluator;

	public TupleColumnPairPredicate(Column col1, RelationalOperator op, Column col2) {

		super(col1, op);
		this.col2 = col2;
		this.evaluator = getPredicateEvalutor();

	}

	private FloatFloatPredicate getPredicateEvalutor() {

		switch (op) {

		case EQUAL:
			return (x, y) -> x == y;
		case UNEQUAL:
			return (x, y) -> x != y;
		case GREATER:
			return (x, y) -> x > y;
		case GREATER_EQUAL:
			return (x, y) -> x >= y;
		case LESS:
			return (x, y) -> x < y;
		case LESS_EQUAL:
			return (x, y) -> x <= y;
		default:
			break;
		}

		return null;
	}

	public boolean evalForTid(int tid) {
		return evaluator.test(col1.getValueAt(tid), col2.getValueAt(tid));
	}

	@Override
	public boolean eval(int t1, int t2) {
		return evalForTid(t1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((col2 == null) ? 0 : col2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TupleColumnPairPredicate other = (TupleColumnPairPredicate) obj;
		if (col2 == null) {
			if (other.col2 != null)
				return false;
		} else if (!col2.equals(other.col2))
			return false;
		return true;
	}

	@Override
	public String toString() {
	
		return "t1." + col1.ColumnName + op.getShortString() + "t1." + col2.ColumnName;
	}

	@Override
	public String toSQLAttributeProjectionString(String tid) {
		return tid + "." + col1.ColumnName + "," + tid + "." + col2.ColumnName;

	}

	@Override
	public String toSQLString() {
		return "t1." + col1.ColumnName + op.getSQLString() + "t1." + col2.ColumnName;
	}
	
	
	

	@Override
	public List<Column> getColumns() {
		List<Column> cols = new ArrayList<>();
		cols.add(col1);
		cols.add(col2);
		return cols;
	}

	@Override
	public long getCardinality() {

		return col1.getCardinality() < col2.getCardinality() ? col1.getCardinality() : col2.getCardinality();
	}

	@Override
	public boolean isColumnPairPredicate() {
		return true;
	}

	@Override
	public Predicate createPredicateReferencingInput(Table table) {

		Column newRefCol1 = table.getColumnByName(col1.ColumnName);

		Column newRefCol2 = table.getColumnByName(col2.ColumnName);

		TupleColumnPairPredicate newRefPred = new TupleColumnPairPredicate(newRefCol1, op, newRefCol2);

		return newRefPred;
	}

}
