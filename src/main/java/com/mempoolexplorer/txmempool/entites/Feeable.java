package com.mempoolexplorer.txmempool.entites;

/**
 * Any transaction type which has fees and id
 *
 */
public interface Feeable {

	String getTxId();

	double getSatvByte();//This does not include ancestors
	
	double getSatvByteIncludingAncestors();// This includes ancestors if any

}
