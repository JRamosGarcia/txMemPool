package com.mempoolexplorer.txmempool.components;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.mempool.TxPoolChanges;

public interface TxMemPool {

	void refresh(TxPoolChanges txPoolChanges);

	Integer getTxNumber();

	Stream<Transaction> getDescendingTxStream();

	Set<String> getAllParentsOf(Transaction tx);

	boolean containsTxId(String txId);

	boolean containsAddrId(String addrId);
	
	Optional<Transaction> getTx(String txId);

	Set<String> getTxIdsOfAddress(String addrId);

}