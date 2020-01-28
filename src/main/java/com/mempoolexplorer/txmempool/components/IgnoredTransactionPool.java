package com.mempoolexplorer.txmempool.components;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.entites.MisMinedTransactions;
import com.mempoolexplorer.txmempool.entites.IgnoredTransactionMap;

public interface IgnoredTransactionPool {

	IgnoredTransactionMap getInmutableMapView();

	void refresh(Block block, MisMinedTransactions mmt, TxMemPool txMemPool);

}