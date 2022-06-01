package br.edu.utfpr.pena.fdcd.algorithms.ecp.indexed_predicates.predicates;

public enum RelationalOperator {

	EQUAL, UNEQUAL, GREATER, LESS, GREATER_EQUAL, LESS_EQUAL;

	private static final double EPSILON = 0.00001d;

	private RelationalOperator inverse;
	private RelationalOperator symmetric;
	private RelationalOperator[] implications;
	private RelationalOperator[] transitives;
	private String shortString;
	private String sqlString;


	public RelationalOperator getInverse() {
		return inverse;
	}

	public RelationalOperator getSymmetric() {
		return symmetric;
	}

	public RelationalOperator[] getImplications() {
		return implications;
	}

	public String getShortString() {
		return shortString;
	}
	
	public String getSQLString() {
		return sqlString;
	}

	public RelationalOperator[] getTransitives() {
		return transitives;
	}

	public boolean isTransitiveWith(RelationalOperator op) {
		for (RelationalOperator i : transitives) {
			if (i == op)
				return true;
		}
		return false;
	}

	public <T> boolean eval(Comparable<T> value1, T value2) {
		if (this == EQUAL) {
			return value1.equals(value2);
		} else if (this == UNEQUAL) {
			return !value1.equals(value2);
		} else {
			int c = value1.compareTo(value2);
			switch (this) {
			case GREATER_EQUAL:
				return c >= 0;
			case LESS:
				return c < 0;
			case LESS_EQUAL:
				return c <= 0;
			case GREATER:
				return c > 0;
			default:
				break;
			}
		}

		return false;
	}

	public boolean eval(int value1, int value2) {
		switch (this) {
		case EQUAL:
			return value1 == value2;
		case GREATER:
			return value1 > value2;
		case GREATER_EQUAL:
			return value1 >= value2;
		case LESS:
			return value1 < value2;
		case LESS_EQUAL:
			return value1 <= value2;
		case UNEQUAL:
			return value1 != value2;
		}
		return false;
	}

	public boolean eval(float value1, float value2) {
		switch (this) {
		case EQUAL:
			return Math.abs(value1 - value2) < EPSILON;
		case UNEQUAL:
			return Math.abs(value1 - value2) >= EPSILON;
		case GREATER:
			return value1 > value2;
		case GREATER_EQUAL:
			return value1 >= value2;
		case LESS:
			return value1 < value2;
		case LESS_EQUAL:
			return value1 <= value2;
		}
		return false;
	}

	/**
	 * a {op} b iff ! (a {op.inverse} b)
	 */
	static {
		EQUAL.inverse = UNEQUAL;
		UNEQUAL.inverse = EQUAL;
		GREATER.inverse = LESS_EQUAL;
		LESS.inverse = GREATER_EQUAL;
		GREATER_EQUAL.inverse = LESS;
		LESS_EQUAL.inverse = GREATER;
		
		
	}

	/**
	 * a {op} b iff b {op.symmetric} a
	 */
	static {
		EQUAL.symmetric = EQUAL;
		UNEQUAL.symmetric = UNEQUAL;
		GREATER.symmetric = LESS;
		LESS.symmetric = GREATER;
		GREATER_EQUAL.symmetric = LESS_EQUAL;
		LESS_EQUAL.symmetric = GREATER_EQUAL;
		
		
	}

	/**
	 * if a {op} b, then a {op.implications} b
	 */
	static {
		EQUAL.implications = new RelationalOperator[] { EQUAL, GREATER_EQUAL, LESS_EQUAL };
		UNEQUAL.implications = new RelationalOperator[] { UNEQUAL };
		GREATER.implications = new RelationalOperator[] { GREATER, GREATER_EQUAL, UNEQUAL };
		LESS.implications = new RelationalOperator[] { LESS, LESS_EQUAL, UNEQUAL };
		GREATER_EQUAL.implications = new RelationalOperator[] { GREATER_EQUAL };
		LESS_EQUAL.implications = new RelationalOperator[] { LESS_EQUAL };
		
	
	}

	/**
	 * if a {op} b and b {op.transitives} c, then a {op} c
	 */
	static {
		EQUAL.transitives = new RelationalOperator[] { EQUAL };
		UNEQUAL.transitives = new RelationalOperator[] { EQUAL };
		GREATER.transitives = new RelationalOperator[] { GREATER, GREATER_EQUAL, EQUAL };
		LESS.transitives = new RelationalOperator[] { LESS, LESS_EQUAL, EQUAL };
		GREATER_EQUAL.transitives = new RelationalOperator[] { GREATER, GREATER_EQUAL, EQUAL };
		LESS_EQUAL.transitives = new RelationalOperator[] { LESS, LESS_EQUAL, EQUAL };
		
		
	}

	/**
	 * a short string that can be used in text output
	 */
	static {
		EQUAL.shortString = "==";
		UNEQUAL.shortString = "<>";
		GREATER.shortString = ">";
		GREATER_EQUAL.shortString = ">=";
		LESS.shortString = "<";
		LESS_EQUAL.shortString = "<=";
		
	}
	
	static {
		EQUAL.sqlString = "=";
		UNEQUAL.sqlString = "<>";
		GREATER.sqlString = ">";
		GREATER_EQUAL.sqlString = ">=";
		LESS.sqlString = "<";
		LESS_EQUAL.sqlString = "<=";
		
	}
	

	public static RelationalOperator getOperator(String s) {
		switch (s) {
		case "EQ":
			return EQUAL;
		case "NEQ":
			return UNEQUAL;
		case "LT":
			return LESS;
		case "LTE":
			return LESS_EQUAL;
		case "GT":
			return GREATER;
		case "GTE":
			return GREATER_EQUAL;

		}

		return null;
	}
	
	public boolean isRangeInequality() {
		
		if(this==LESS || this==LESS_EQUAL || this==GREATER || this==GREATER_EQUAL)
			return true;
		else
			return false;
	}


}
