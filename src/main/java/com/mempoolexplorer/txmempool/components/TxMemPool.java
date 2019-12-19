package com.mempoolexplorer.txmempool.components;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.mempool.TxPoolChanges;
import com.mempoolexplorer.txmempool.entites.MisMinedTransactions;

public interface TxMemPool {

	void refresh(TxPoolChanges txPoolChanges);

	void updateMiningQueue();

	MisMinedTransactions calculateMisMinedTransactions(Block block, int numConsecutiveBlocks);

	Integer getTxNumber();

}