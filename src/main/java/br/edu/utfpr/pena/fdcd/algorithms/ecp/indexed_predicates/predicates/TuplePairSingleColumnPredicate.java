package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates;

import java.util.ArrayList;
import java.util.List;

import com.koloboke.function.FloatFloatPredicate;

import br.edu.utfpr.pena.fdcd.input.Table;
import br.edu.utfpr.pena.fdcd.input.columns.Column;



public class TuplePairSingleColumnPredicate extends Predicate {

	private FloatFloatPredicate evaluator;

	public TuplePairSingleColumnPredicate(Column col, RelationalOperator op) {
		super(col, op);
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

	@Override
	public String toString() {
		return "t1." + col1.ColumnName + op.getShortString() + "t2." + col1.ColumnName;
	}

	@Override
	public String toSQLString() {
		return "t1." + col1.getCSVTypedColString() + op.getSQLString() + "t2." + col1.getCSVTypedColString();
	}

	@Override
	public String toSQLAttributeProjectionString(String tid) {
		return tid + "." + col1.getCSVTypedColString();

	}

	@Override
	public List<Column> getColumns() {
		List<Column> cols = new ArrayList<>();
		cols.add(col1);
		return cols;
	}

	@Override
	public long getCardinality() {
		return col1.getCardinality();
	}

	@Override
	public boolean isColumnPairPredicate() {
		return false;
	}

	@Override
	public boolean eval(int t1, int t2) {

		return evaluator.test(col1.getValueAt(t1), col1.getValueAt(t2));
	}

	@Override
	public Predicate createPredicateReferencingInput(Table table) {

		Column newRefCol1 = table.getColumnByName(col1.ColumnName);

		TuplePairSingleColumnPredicate newRefPred = new TuplePairSingleColumnPredicate(newRefCol1, op);

		return newRefPred;
	}

}
