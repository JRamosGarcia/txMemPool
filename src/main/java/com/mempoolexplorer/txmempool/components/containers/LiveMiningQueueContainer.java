package com.mempoolexplorer.txmempool.components.containers;

import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.entites.miningqueue.MiningQueue;

public interface LiveMiningQueueContainer {

	void atomicSet(MiningQueue mq);

	MiningQueue atomicGet();

	void refreshIfNeeded(TxMemPool txMemPool);

	void forceRefresh(TxMemPool txMemPool);

}