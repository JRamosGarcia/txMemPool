package com.mempoolexplorer.txmempool.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "txmempool")
public class TxMempoolProperties {

	private Integer refreshCountToCreateNewMiningQueue;
	private Integer miningQueueNumTxs;

	public Integer getRefreshCountToCreateNewMiningQueue() {
		return refreshCountToCreateNewMiningQueue;
	}

	public void setRefreshCountToCreateNewMiningQueue(Integer refreshCountToCreateNewMiningQueue) {
		this.refreshCountToCreateNewMiningQueue = refreshCountToCreateNewMiningQueue;
	}

	public Integer getMiningQueueNumTxs() {
		return miningQueueNumTxs;
	}

	public void setMiningQueueNumTxs(Integer miningQueueNumTxs) {
		this.miningQueueNumTxs = miningQueueNumTxs;
	}

}
