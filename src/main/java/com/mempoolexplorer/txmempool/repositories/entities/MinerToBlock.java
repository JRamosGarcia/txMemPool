package com.mempoolexplorer.txmempool.repositories.entities;

import java.io.Serializable;

public class MinerToBlock implements Serializable {
	private static final long serialVersionUID = -8700126971230262729L;
	private String minerName;
	private Integer blockHeight;

	public MinerToBlock() {

	}

	public MinerToBlock(String minerName, Integer blockHeight) {
		super();
		this.minerName = minerName;
		this.blockHeight = blockHeight;
	}

	public String getMinerName() {
		return minerName;
	}

	public void setMinerName(String minerName) {
		this.minerName = minerName;
	}

	public Integer getBlockHeight() {
		return blockHeight;
	}

	public void setBlockHeight(Integer blockHeight) {
		this.blockHeight = blockHeight;
	}

}
