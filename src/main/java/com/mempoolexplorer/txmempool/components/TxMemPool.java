package com.mempoolexplorer.txmempool.components;

import java.util.Set;
import java.util.stream.Stream;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.mempool.TxPoolChanges;
import com.mempoolexplorer.txmempool.entites.MisMinedTransactions;

public interface TxMemPool {

	void refresh(TxPoolChanges txPoolChanges);

	void updateMiningQueue();

	MisMinedTransactions calculateMisMinedTransactions(Block block, int numConsecutiveBlocks);

	Integer getTxNumber();

	Set<String> getTxIdSet();

	Stream<Transaction> getDescendingTxStream();

	Set<String> getAllParentsOf(Transaction tx);

}