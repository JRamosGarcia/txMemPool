package com.mempoolexplorer.txmempool.entites.miningqueue;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;

public class TxToBeMined {
	private Transaction tx;
	private QueuedBlock containingBlock;
	private int positionInBlock;

	public TxToBeMined(Transaction tx, QueuedBlock containedBlock, int positionInBlock) {
		super();
		this.tx = tx;
		this.containingBlock = containedBlock;
		this.positionInBlock = positionInBlock;
	}

	public Transaction getTx() {
		return tx;
	}

	public QueuedBlock getContainingBlock() {
		return containingBlock;
	}

	public int getPositionInBlock() {
		return positionInBlock;
	}

	public void setPositionInBlock(int positionInBlock) {
		this.positionInBlock = positionInBlock;
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
