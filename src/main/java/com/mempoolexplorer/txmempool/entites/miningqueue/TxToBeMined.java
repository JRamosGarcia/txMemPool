package com.mempoolexplorer.txmempool.entites.miningqueue;

import java.util.List;
import java.util.Optional;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;

public class TxToBeMined {
	private Transaction tx;
	private CandidateBlock containingBlock;
	private int positionInBlock;
	private Optional<Transaction> payingChildTx;// Child paying for this tx in case of CPFP
	private Optional<List<Transaction>> reducedBy;// parents Already in block who reduces satVByte

	public TxToBeMined(Transaction tx, Optional<Transaction> payingChildTx, Optional<List<Transaction>> reducedBy,
			CandidateBlock containedBlock, int positionInBlock) {
		super();
		this.tx = tx;
		this.containingBlock = containedBlock;
		this.positionInBlock = positionInBlock;
		this.payingChildTx = payingChildTx;
		this.reducedBy = reducedBy;
	}

	public Transaction getTx() {
		return tx;
	}

	public CandidateBlock getContainingBlock() {
		return containingBlock;
	}

	public int getPositionInBlock() {
		return positionInBlock;
	}

	public Optional<Transaction> getPayingChildTx() {
		return payingChildTx;
	}

	public Optional<List<Transaction>> getReducedBy() {
		return reducedBy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tx == null) ? 0 : tx.hashCode());
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
		TxToBeMined other = (TxToBeMined) obj;
		if (tx == null) {
			if (other.tx != null)
				return false;
		} else if (!tx.equals(other.tx))
			return false;
		return true;
	}

}
