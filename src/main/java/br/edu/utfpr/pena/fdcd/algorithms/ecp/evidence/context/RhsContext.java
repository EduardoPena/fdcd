package br.edu.utfpr.pena.fdcd.algorithms.ecp.evidence.context;

import java.util.BitSet;

import org.roaringbitmap.RoaringBitmap;

public class RhsContext {

	protected RoaringBitmap c2;
	protected BitSet evidence;

	public RhsContext(RoaringBitmap c2, BitSet evidence) {
		super();
		this.c2 = c2;
		this.evidence = evidence;
	}

	@Override
	public String toString() {
		return "EvidenceContext [evidence=" + evidence + "]=" + c2.getCardinality();
	}

	public RoaringBitmap getC2() {
		return c2;
	}

	public void setC2(RoaringBitmap c2) {
		this.c2 = c2;
	}

	public BitSet getEvidence() {
		return evidence;
	}

	public void setEvidence(BitSet evidence) {
		this.evidence = evidence;
	}

}
