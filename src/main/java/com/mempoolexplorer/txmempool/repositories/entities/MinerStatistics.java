package com.mempoolexplorer.txmempool.repositories.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "minerStatistics")
public class MinerStatistics {

	@Id
	private String minerName;
	private Long totalLostRewardBT;
	private Long totalLostRewardCB;
	private Integer numBlocksMined;

	public MinerStatistics() {

	}

	public MinerStatistics(String minerName, Long totalLostRewardBT, Long totalLostRewardCB, Integer numBlocksMined) {
		super();
		this.minerName = minerName;
		this.totalLostRewardBT = totalLostRewardBT;
		this.totalLostRewardCB = totalLostRewardCB;
		this.numBlocksMined = numBlocksMined;
	}

	public String getMinerName() {
		return minerName;
	}

	public void setMinerName(String minerName) {
		this.minerName = minerName;
	}

	public Long getTotalLostRewardBT() {
		return totalLostRewardBT;
	}

	public void setTotalLostRewardBT(Long totalLostRewardBT) {
		this.totalLostRewardBT = totalLostRewardBT;
	}

	public Long getTotalLostRewardCB() {
		return totalLostRewardCB;
	}

	public void setTotalLostRewardCB(Long totalLostRewardCB) {
		this.totalLostRewardCB = totalLostRewardCB;
	}

	public Integer getNumBlocksMined() {
		return numBlocksMined;
	}

	public void setNumBlocksMined(Integer numBlocksMined) {
		this.numBlocksMined = numBlocksMined;
	}

}
