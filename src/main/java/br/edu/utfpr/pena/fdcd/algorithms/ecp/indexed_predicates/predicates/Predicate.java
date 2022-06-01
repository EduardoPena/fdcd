package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates;

import java.util.List;

import br.edu.utfpr.pena.fdcd.input.Table;
import br.edu.utfpr.pena.fdcd.input.columns.Column;



public abstract class Predicate {

	protected final Column col1;
	protected final RelationalOperator op;

	protected Predicate(Column col, RelationalOperator op) {
		this.op = op;
		this.col1 = col;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((col1 == null) ? 0 : col1.hashCode());
		result = prime * result + ((op == null) ? 0 : op.hashCode());
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
		Predicate other = (Predicate) obj;
		if (col1 == null) {
			if (other.col1 != null)
				return false;
		} else if (!col1.equals(other.col1))
			return false;
		if (op != other.op)
			return false;
		return true;
	}

	public abstract List<Column> getColumns();

	public Column getCol1() {
		return col1;
	}

	public RelationalOperator getOp() {
		return op;
	}

	public boolean isRangeInequality() {

		return op.isRangeInequality();
	}

	public abstract String toSQLString();

	public abstract String toSQLAttributeProjectionString(String tid);

	public abstract long getCardinality();

	public abstract boolean isColumnPairPredicate();

	public boolean isTuplePairSingleColumnEquality() {

		if (this instanceof TuplePairSingleColumnPredicate && op == RelationalOperator.EQUAL)
			return true;

		return false;
	}

	public boolean isTwoColumnEquality() {
		if (this instanceof TuplePairColumnPairPredicate && op == RelationalOperator.EQUAL)
			return true;

		return false;
	}

	public boolean isTuplePairSingleColumnDifferentThan() {

		if (this instanceof TuplePairSingleColumnPredicate && op == RelationalOperator.UNEQUAL)
			return true;

		return false;
	}

	public boolean isTuplePairColumnPairDifferentThan() {

		if (this instanceof TuplePairColumnPairPredicate && op == RelationalOperator.UNEQUAL)
			return true;

		return false;
	}

	public boolean isSingleColumnRangeInequality() {

		if (this instanceof TuplePairSingleColumnPredicate && op.isRangeInequality())
			return true;

		return false;
	}

	public abstract boolean eval(int t1, int t2);

	public boolean isTwoColumnRangeInequality() {
		if (this instanceof TuplePairColumnPairPredicate && op.isRangeInequality())
			return true;

		return false;
	}

	

	public String toStringLeftHandSide() {
		return ""+col1+op.getShortString();
	}

	public abstract Predicate createPredicateReferencingInput(Table table);

}
