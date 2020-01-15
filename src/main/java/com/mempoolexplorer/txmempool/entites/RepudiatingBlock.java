package com.mempoolexplorer.txmempool.entites;

import java.time.Instant;

import com.mempoolexplorer.txmempool.utils.SysProps;

public class RepudiatingBlock {

	public static final String UNKNOWN = "Unknown";

	private Integer blockHeight;
	private Integer numTxInMinedBlock;
	private Integer weight;// up to 4000000
	private Instant blockChangeTime;// Mined time set by us, not mining operators.
	private MaxMinFeeTransactions maxMinFeesInBlock;// Sat/vByte
	private RepudiatingBlockStats minedAndInMemPoolStats;// ok txs
	private RepudiatingBlockStats notMinedButInCandidateBlockStats;// shouldHaveBeenMined
	private RepudiatingBlockStats minedInMempoolButNotInCandidateBlockStats;// shouldHaveNotBeenMined

	private Long lostReward;// In satoshis
	private Integer minedButNotInMemPoolTxNum;// >=1 due to coinbase transaction
	private String coinbaseTxId;// For fun
	private String coinbase;// For searching mining operator
	private String minerName = UNKNOWN;// If not known, "Unknown"

	public Integer getBlockHeight() {
		return blockHeight;
	}

	public void setBlockHeight(Integer blockHeight) {
		this.blockHeight = blockHeight;
	}

	public Integer getNumTxInMinedBlock() {
		return numTxInMinedBlock;
	}

	public void setNumTxInMinedBlock(Integer numTxInMinedBlock) {
		this.numTxInMinedBlock = numTxInMinedBlock;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public Instant getBlockChangeTime() {
		return blockChangeTime;
	}

	public void setBlockChangeTime(Instant blockChangeTime) {
		this.blockChangeTime = blockChangeTime;
	}

	public MaxMinFeeTransactions getMaxMinFeesInBlock() {
		return maxMinFeesInBlock;
	}

	public void setMaxMinFeesInBlock(MaxMinFeeTransactions maxMinFeesInBlock) {
		this.maxMinFeesInBlock = maxMinFeesInBlock;
	}

	public RepudiatingBlockStats getMinedAndInMemPoolStats() {
		return minedAndInMemPoolStats;
	}

	public void setMinedAndInMemPoolStats(RepudiatingBlockStats minedAndInMemPoolStats) {
		this.minedAndInMemPoolStats = minedAndInMemPoolStats;
	}

	public RepudiatingBlockStats getNotMinedButInCandidateBlockStats() {
		return notMinedButInCandidateBlockStats;
	}

	public void setNotMinedButInCandidateBlockStats(RepudiatingBlockStats notMinedButInCandidateBlockStats) {
		this.notMinedButInCandidateBlockStats = notMinedButInCandidateBlockStats;
	}

	public RepudiatingBlockStats getMinedInMempoolButNotInCandidateBlockStats() {
		return minedInMempoolButNotInCandidateBlockStats;
	}

	public void setMinedInMempoolButNotInCandidateBlockStats(
			RepudiatingBlockStats minedInMempoolButNotInCandidateBlockStats) {
		this.minedInMempoolButNotInCandidateBlockStats = minedInMempoolButNotInCandidateBlockStats;
	}

	public Long getLostReward() {
		return lostReward;
	}

	public void setLostReward(Long lostReward) {
		this.lostReward = lostReward;
	}

	public Integer getMinedButNotInMemPoolTxNum() {
		return minedButNotInMemPoolTxNum;
	}

	public void setMinedButNotInMemPoolTxNum(Integer minedButNotInMemPoolTxNum) {
		this.minedButNotInMemPoolTxNum = minedButNotInMemPoolTxNum;
	}

	public String getCoinbaseTxId() {
		return coinbaseTxId;
	}

	public void setCoinbaseTxId(String coinbaseTxId) {
		this.coinbaseTxId = coinbaseTxId;
	}

	public String getCoinbase() {
		return coinbase;
	}

	public void setCoinbase(String coinbase) {
		this.coinbase = coinbase;
	}

	public String getMinerName() {
		return minerName;
	}

	public void setMinerName(String minerName) {
		this.minerName = minerName;
	}

	public static String getUnknown() {
		return UNKNOWN;
	}

	@Override
	public String toString() {
		String nl = SysProps.NL;
		StringBuilder builder = new StringBuilder();
		builder.append("RepudiatingBlock [blockHeight=");
		builder.append(blockHeight);
		builder.append(nl);
		builder.append("numTxInMinedBlock=");
		builder.append(numTxInMinedBlock);
		builder.append(nl);
		builder.append("weight=");
		builder.append(weight);
		builder.append(nl);
		builder.append("blockChangeTime=");
		builder.append(blockChangeTime);
		builder.append(nl);
		builder.append("maxMinFeesInBlock=");
		builder.append(maxMinFeesInBlock);
		builder.append(nl);
		builder.append("minedAndInMemPoolStats=");
		builder.append(minedAndInMemPoolStats);
		builder.append(nl);
		builder.append("notMinedButInCandidateBlockStats=");
		builder.append(notMinedButInCandidateBlockStats);
		builder.append(nl);
		builder.append("minedInMempoolButNotInCandidateBlockStats=");
		builder.append(minedInMempoolButNotInCandidateBlockStats);
		builder.append(nl);
		builder.append("lostReward=");
		builder.append(lostReward);
		builder.append(nl);
		builder.append("minedButNotInMemPoolTxNum=");
		builder.append(minedButNotInMemPoolTxNum);
		builder.append(nl);
		builder.append("coinbaseTxId=");
		builder.append(coinbaseTxId);
		builder.append(nl);
		builder.append("coinbase=");
		builder.append(coinbase);
		builder.append(nl);
		builder.append("minerName=");
		builder.append(minerName);
		builder.append(nl);
		builder.append("]");
		return builder.toString();
	}

}
