package com.mempoolexplorer.txmempool.components.containers;

import com.mempoolexplorer.txmempool.entites.miningqueue.LiveMiningQueue;

public interface LiveMiningQueueContainer {

	// Can return null if service is not initialized yet
	LiveMiningQueue atomicGet();

	void refreshIfNeeded();

	void forceRefresh();

}