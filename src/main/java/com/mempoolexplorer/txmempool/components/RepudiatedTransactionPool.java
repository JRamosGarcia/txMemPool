package com.mempoolexplorer.txmempool.components;

import java.util.Map;
import java.util.Set;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.entites.MisMinedTransactions;
import com.mempoolexplorer.txmempool.entites.RepudiatedTransaction;

public interface RepudiatedTransactionPool {

	Map<String, RepudiatedTransaction> getMap();

	void refresh(Block block, MisMinedTransactions mmt, Set<String> memPoolSet);

}