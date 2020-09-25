package com.mempoolexplorer.txmempool.entites.miningqueue;

import com.mempoolexplorer.txmempool.controllers.entities.CompletLiveMiningQueueGraphData;

public class LiveMiningQueue {

	private CompletLiveMiningQueueGraphData liveMiningQueueGraphData;
	private MiningQueue miningQueue;

	public LiveMiningQueue(CompletLiveMiningQueueGraphData liveMiningQueueGraphData, MiningQueue miningQueue) {
		super();
		this.liveMiningQueueGraphData = liveMiningQueueGraphData;
		this.miningQueue = miningQueue;
	}

	public CompletLiveMiningQueueGraphData getLiveMiningQueueGraphData() {
		return liveMiningQueueGraphData;
	}

	public void setLiveMiningQueueGraphData(CompletLiveMiningQueueGraphData liveMiningQueueGraphData) {
		this.liveMiningQueueGraphData = liveMiningQueueGraphData;
	}

	public MiningQueue getMiningQueue() {
		return miningQueue;
	}

	public void setMiningQueue(MiningQueue miningQueue) {
		this.miningQueue = miningQueue;
	}

}
