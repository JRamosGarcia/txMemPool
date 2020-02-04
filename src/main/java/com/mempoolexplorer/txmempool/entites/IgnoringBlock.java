package com.mempoolexplorer.txmempool.entites;

import java.time.Instant;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.CoinBaseTx;
import com.mempoolexplorer.txmempool.utils.SysProps;

public class IgnoringBlock {

	public static final String UNKNOWN = "Unknown";

	private Integer blockHeight;
	private Integer numTxInMinedBlock;
	private Integer weight;// up to 4000000
	private Instant blockChangeTime;// Mined time set by us, not mining operators.
	private MaxMinFeeTransactions maxMinFeesInBlock;// Sat/vByte
	private IgnoringBlockStats candidateBlockStats;
	private IgnoringBlockStats minedAndInMemPoolStats;// ok txs
	private IgnoringBlockStats notMinedButInCandidateBlockStats;// shouldHaveBeenMined
	private IgnoringBlockStats minedInMempoolButNotInCandidateBlockStats;// shouldHaveNotBeenMined

	private Long lostReward;// In satoshis
	private Integer minedButNotInMemPoolTxNum;// >=1 due to coinbase transaction
	private CoinBaseTx coinBaseTx;
	private String minerName = UNKNOWN;// If not known, "Unknown"
	private String ascciCoinBaseField;

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

	public IgnoringBlockStats getCandidateBlockStats() {
		return candidateBlockStats;
	}

	public void setCandidateBlockStats(IgnoringBlockStats candidateBlockStats) {
		this.candidateBlockStats = candidateBlockStats;
	}

	public IgnoringBlockStats getMinedAndInMemPoolStats() {
		return minedAndInMemPoolStats;
	}

	public void setMinedAndInMemPoolStats(IgnoringBlockStats minedAndInMemPoolStats) {
		this.minedAndInMemPoolStats = minedAndInMemPoolStats;
	}

	public IgnoringBlockStats getNotMinedButInCandidateBlockStats() {
		return notMinedButInCandidateBlockStats;
	}

	public void setNotMinedButInCandidateBlockStats(IgnoringBlockStats notMinedButInCandidateBlockStats) {
		this.notMinedButInCandidateBlockStats = notMinedButInCandidateBlockStats;
	}

	public IgnoringBlockStats getMinedInMempoolButNotInCandidateBlockStats() {
		return minedInMempoolButNotInCandidateBlockStats;
	}

	public void setMinedInMempoolButNotInCandidateBlockStats(
			IgnoringBlockStats minedInMempoolButNotInCandidateBlockStats) {
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

	public CoinBaseTx getCoinBaseTx() {
		return coinBaseTx;
	}

	public void setCoinBaseTx(CoinBaseTx coinBaseTx) {
		this.coinBaseTx = coinBaseTx;
	}

	public String getMinerName() {
		return minerName;
	}

	public void setMinerName(String minerName) {
		this.minerName = minerName;
	}

	public String getAscciCoinBaseField() {
		return ascciCoinBaseField;
	}

	public void setAscciCoinBaseField(String ascciCoinBaseField) {
		this.ascciCoinBaseField = ascciCoinBaseField;
	}

	@Override
	public String toString() {
		String nl = SysProps.NL;
		StringBuilder builder = new StringBuilder();
		builder.append("IgnoringBlock [blockHeight=");
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
		builder.append("candidateBlockStats=");
		builder.append(candidateBlockStats);
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
		builder.append("coinBaseTx=");
		builder.append(coinBaseTx);
		builder.append(nl);
		builder.append("minerName=");
		builder.append(minerName);
		builder.append(nl);
		builder.append("ascciCoinBaseField=");
		builder.append(ascciCoinBaseField);
		builder.append(nl);
		builder.append("]");
		return builder.toString();
	}

}
