package com.mempoolexplorer.txmempool.entites.miningqueue;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;

public class ModifiedTx {

	private Transaction tx;
	private long realAncestorFees;

	public ModifiedTx(Transaction tx, long realFees) {
		super();
		this.tx = tx;
		this.realAncestorFees = realFees;
	}

	public Transaction getTx() {
		return tx;
	}

	public void setTx(Transaction tx) {
		this.tx = tx;
	}

	public long getRealAncestorFees() {
		return realAncestorFees;
	}

	public void setRealAncestorFees(long realAncestorFees) {
		this.realAncestorFees = realAncestorFees;
	}

	public double getRealAncestorSatVByte() {
		return satVByteFrom(realAncestorFees, tx.getWeight());
	}

	private double satVByteFrom(long fees, int weight) {
		return ((double) fees) / ((double) (weight) / 4.0D);
	}
}
