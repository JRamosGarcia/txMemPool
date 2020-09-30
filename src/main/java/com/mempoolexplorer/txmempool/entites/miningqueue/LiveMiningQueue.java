package com.mempoolexplorer.txmempool.entites.miningqueue;

import com.mempoolexplorer.txmempool.controllers.entities.CompleteLiveMiningQueueGraphData;

public class LiveMiningQueue {

	private CompleteLiveMiningQueueGraphData liveMiningQueueGraphData;
	private MiningQueue miningQueue;

	public LiveMiningQueue(CompleteLiveMiningQueueGraphData liveMiningQueueGraphData, MiningQueue miningQueue) {
		super();
		this.liveMiningQueueGraphData = liveMiningQueueGraphData;
		this.miningQueue = miningQueue;
	}

	public CompleteLiveMiningQueueGraphData getLiveMiningQueueGraphData() {
		return liveMiningQueueGraphData;
	}

	public void setLiveMiningQueueGraphData(CompleteLiveMiningQueueGraphData liveMiningQueueGraphData) {
		this.liveMiningQueueGraphData = liveMiningQueueGraphData;
	}

	public MiningQueue getMiningQueue() {
		return miningQueue;
	}

	public void setMiningQueue(MiningQueue miningQueue) {
		this.miningQueue = miningQueue;
	}

}
