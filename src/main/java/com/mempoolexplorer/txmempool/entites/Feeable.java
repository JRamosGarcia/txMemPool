package com.mempoolexplorer.txmempool.entites;

/**
 * Any transaction type which has fees and id
 *
 */
public interface Feeable {

	String getTxId();

	double getSatvByte();

}
