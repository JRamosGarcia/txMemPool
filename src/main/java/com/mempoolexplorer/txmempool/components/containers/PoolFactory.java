package com.mempoolexplorer.txmempool.components.containers;

import com.mempoolexplorer.txmempool.controllers.exceptions.AlgorithmTypeNotFoundException;
import com.mempoolexplorer.txmempool.entites.AlgorithmType;
import com.mempoolexplorer.txmempool.entites.pools.IgnoredTransactionsPool;
import com.mempoolexplorer.txmempool.entites.pools.IgnoringBlocksPool;
import com.mempoolexplorer.txmempool.entites.pools.RepudiatedTransactionsPool;

public interface PoolFactory {

	IgnoredTransactionsPool getIgnoredTransactionsPool(AlgorithmType at);

	IgnoringBlocksPool getIgnoringBlocksPool(AlgorithmType at);

	RepudiatedTransactionsPool getRepudiatedTransactionsPool(AlgorithmType at);

	IgnoredTransactionsPool getIgnoredTransactionsPool(String at) throws AlgorithmTypeNotFoundException;

	IgnoringBlocksPool getIgnoringBlocksPool(String at) throws AlgorithmTypeNotFoundException;

	RepudiatedTransactionsPool getRepudiatedTransactionsPool(String at) throws AlgorithmTypeNotFoundException;

	void drop();
}