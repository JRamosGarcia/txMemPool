package com.mempoolexplorer.txmempool.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "txmempool")
public class TxMempoolProperties {

	private Integer refreshCountToCreateNewMiningQueue;
	private Integer miningQueueNumTxs;
	private Integer miningQueueMaxNumBlocks;
	private Integer liveMiningQueueMaxTxs;
	private Integer liveMiningQueueMaxSatByteListSize;

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

	public Integer getMiningQueueMaxNumBlocks() {
		return miningQueueMaxNumBlocks;
	}

	public void setMiningQueueMaxNumBlocks(Integer miningQueueMaxNumBlocks) {
		this.miningQueueMaxNumBlocks = miningQueueMaxNumBlocks;
	}

	public Integer getLiveMiningQueueMaxTxs() {
		return liveMiningQueueMaxTxs;
	}

	public void setLiveMiningQueueMaxTxs(Integer liveMiningQueueMaxTxs) {
		this.liveMiningQueueMaxTxs = liveMiningQueueMaxTxs;
	}

	public Integer getLiveMiningQueueMaxSatByteListSize() {
		return liveMiningQueueMaxSatByteListSize;
	}

	public void setLiveMiningQueueMaxSatByteListSize(Integer liveMiningQueueMaxSatByteListSize) {
		this.liveMiningQueueMaxSatByteListSize = liveMiningQueueMaxSatByteListSize;
	}

}
