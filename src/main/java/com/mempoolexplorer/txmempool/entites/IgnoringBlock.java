package com.mempoolexplorer.txmempool.entites;

import java.util.List;

public class IgnoringBlock {

	public static final String UNKNOWN = "Unknown";

	private MinedBlockData minedBlockData;
	private CandidateBlockData candidateBlockData;
	private FeeableData minedAndInMemPoolData;
	private FeeableData notMinedButInCandidateBlockData;
	private FeeableData minedInMempoolButNotInCandidateBlockData;
	private FeeableData minedButNotInMemPoolData;
	private List<String> consistencyErrors;
	private String minerName = UNKNOWN;// If not known, "Unknown"
	private long lostReward;
	private long lostRewardExcludingNotInMempoolTx;

	public IgnoringBlock() {

	}

	public IgnoringBlock(MisMinedTransactions mmt) {
		this.minedBlockData = mmt.getMinedBlockData();
		this.candidateBlockData = mmt.getCandidateBlockData();
		this.minedAndInMemPoolData = mmt.getMinedAndInMemPoolMapWD().getFeeableData();
		this.notMinedButInCandidateBlockData = mmt.getNotMinedButInCandidateBlockMapWD().getFeeableData();
		this.minedInMempoolButNotInCandidateBlockData = mmt.getMinedInMempoolButNotInCandidateBlockMapWD()
				.getFeeableData();
		this.minedButNotInMemPoolData = mmt.getMinedButNotInMemPoolMapWD().getFeeableData();
		this.consistencyErrors = mmt.getConsistencyErrors();
		this.lostReward = mmt.getLostReward();
		this.lostRewardExcludingNotInMempoolTx = mmt.getLostRewardExcludingNotInMempoolTx();
	}

	public MinedBlockData getMinedBlockData() {
		return minedBlockData;
	}

	public CandidateBlockData getCandidateBlockData() {
		return candidateBlockData;
	}

	public FeeableData getMinedAndInMemPoolData() {
		return minedAndInMemPoolData;
	}

	public FeeableData getNotMinedButInCandidateBlockData() {
		return notMinedButInCandidateBlockData;
	}

	public FeeableData getMinedInMempoolButNotInCandidateBlockData() {
		return minedInMempoolButNotInCandidateBlockData;
	}

	public FeeableData getMinedButNotInMemPoolData() {
		return minedButNotInMemPoolData;
	}

	public List<String> getConsistencyErrors() {
		return consistencyErrors;
	}

	public String getMinerName() {
		return minerName;
	}

	public long getLostReward() {
		return lostReward;
	}

	public long getLostRewardExcludingNotInMempoolTx() {
		return lostRewardExcludingNotInMempoolTx;
	}

}
