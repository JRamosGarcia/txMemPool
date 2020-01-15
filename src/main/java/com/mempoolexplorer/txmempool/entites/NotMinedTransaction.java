package com.mempoolexplorer.txmempool.entites;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;

/**
 * Class that represents a transaction that should be mined and it's not.
 * Prefers containment over extends to avoid copy constructor
 */
public class NotMinedTransaction implements Feeable {

	/**
	 * This is the ordinal position of the transaction in the blockQueue.
	 */
	private Transaction tx;
	private Integer ordinalpositionInBlock;

	public NotMinedTransaction(Transaction transaction, Integer ordinalpositionInBlock) {
		super();
		this.tx = transaction;
		this.ordinalpositionInBlock = ordinalpositionInBlock;
	}

	public Integer getOrdinalpositionInBlock() {
		return ordinalpositionInBlock;
	}

	public Transaction getTx() {
		return tx;
	}

	@Override
	public String getTxId() {
		return tx.getTxId();
	}

	@Override
	public double getSatvByte() {
		return tx.getSatvByte();
	}

}
