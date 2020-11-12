package com.mempoolexplorer.txmempool.entites.pools;

import java.util.Map;
import java.util.Optional;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.entites.IgnoredTransaction;
import com.mempoolexplorer.txmempool.entites.MisMinedTransactions;

public interface IgnoredTransactionsPool {

	Map<String,IgnoredTransaction> atomicGetIgnoredTransactionMap();

	void refresh(Block block, MisMinedTransactions mmt, TxMemPool txMemPool);

	Optional<IgnoredTransaction> getIgnoredTransaction(String txId);

	void drop();
}