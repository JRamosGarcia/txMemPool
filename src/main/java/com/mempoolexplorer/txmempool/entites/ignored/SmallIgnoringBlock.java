package com.mempoolexplorer.txmempool.entites.ignored;

public class SmallIgnoringBlock {

	private Integer height;
	private Integer postitionInQueue;

	public SmallIgnoringBlock() {

	}

	public SmallIgnoringBlock(Integer height, Integer postitionInQueue) {
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

	public Integer getPostitionInQueue() {
		return postitionInQueue;
	}

	public void setPostitionInQueue(Integer postitionInQueue) {
		this.postitionInQueue = postitionInQueue;
	}

}
