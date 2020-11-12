package com.mempoolexplorer.txmempool.components.containers;

import java.util.Optional;

import com.mempoolexplorer.txmempool.entites.miningqueue.LiveMiningQueue;
import com.mempoolexplorer.txmempool.entites.miningqueue.MiningQueue;

public interface LiveMiningQueueContainer {

	// Can return null if service is not initialized yet
	LiveMiningQueue atomicGet();

	Optional<MiningQueue> refreshIfNeeded();

	MiningQueue forceRefresh();

	void drop();
}