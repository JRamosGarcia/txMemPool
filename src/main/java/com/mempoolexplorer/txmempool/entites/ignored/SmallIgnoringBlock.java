package com.mempoolexplorer.txmempool.entites.ignored;

import java.util.Optional;

public class SmallIgnoringBlock {

	private Integer height;
	private Optional<Integer> postitionInQueue;

	public SmallIgnoringBlock() {

	}

	public SmallIgnoringBlock(Integer height, Optional<Integer> postitionInQueue) {
		super();
		this.height = height;
		this.postitionInQueue = postitionInQueue;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Optional<Integer> getPostitionInQueue() {
		return postitionInQueue;
	}

	public void setPostitionInQueue(Optional<Integer> postitionInQueue) {
		this.postitionInQueue = postitionInQueue;
	}

}
