package com.mempoolexplorer.txmempool.entites.ignored;

import java.util.Optional;

public class SmallIgnoringBlock {

	private Integer height;
	private Integer totalTxNumMined;
	private Optional<Integer> postitionInQueue;

	public SmallIgnoringBlock() {
	}

	public SmallIgnoringBlock(Integer height, Integer totalTxNumMined, Optional<Integer> postitionInQueue) {
		this.height = height;
		this.totalTxNumMined = totalTxNumMined;
		this.postitionInQueue = postitionInQueue;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Integer getTotalTxNumMined() {
		return totalTxNumMined;
	}

	public void setTotalTxNumMined(Integer totalTxNumMined) {
		this.totalTxNumMined = totalTxNumMined;
	}

	public Optional<Integer> getPostitionInQueue() {
		return postitionInQueue;
	}

	public void setPostitionInQueue(Optional<Integer> postitionInQueue) {
		this.postitionInQueue = postitionInQueue;
	}

}
