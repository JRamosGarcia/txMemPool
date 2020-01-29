package com.mempoolexplorer.txmempool.entites.miningqueue;

import com.mempoolexplorer.txmempool.controllers.entities.LiveMiningQueueGraphData;

public class LiveMiningQueue {

	private LiveMiningQueueGraphData liveMiningQueueGraphData;
	private MiningQueue miningQueue;

	public LiveMiningQueue(LiveMiningQueueGraphData liveMiningQueueGraphData, MiningQueue miningQueue) {
		super();
		this.liveMiningQueueGraphData = liveMiningQueueGraphData;
		this.miningQueue = miningQueue;
	}

	public LiveMiningQueueGraphData getLiveMiningQueueGraphData() {
		return liveMiningQueueGraphData;
	}

	public void setLiveMiningQueueGraphData(LiveMiningQueueGraphData liveMiningQueueGraphData) {
		this.liveMiningQueueGraphData = liveMiningQueueGraphData;
	}

	public MiningQueue getMiningQueue() {
		return miningQueue;
	}

	public void setMiningQueue(MiningQueue miningQueue) {
		this.miningQueue = miningQueue;
	}

}
