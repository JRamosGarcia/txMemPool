package com.mempoolexplorer.txmempool.entites;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;

/**
 * Class that represents a transaction that should be mined and it's not.
 */
public class NotMinedTransaction {

	/**
	 * This is the ordinal position of the transaction in the blockQueue.
	 */
	private Transaction transaction;
	private Integer ordinalpositionInBlock;

	public NotMinedTransaction(Transaction transaction, Integer ordinalpositionInBlock) {
		super();
		this.transaction = transaction;
		this.ordinalpositionInBlock = ordinalpositionInBlock;
	}

	public Integer getOrdinalpositionInBlock() {
		return ordinalpositionInBlock;
	}

	public Transaction getTransaction() {
		return transaction;
	}

}
