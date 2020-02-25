package com.mempoolexplorer.txmempool.entites;

import java.util.HashSet;
import java.util.Set;

public class IgnoringBlock {

	private AlgorithmType algorithmUsed;
	private Set<String> inCandidateBlockButNotInMemPool = new HashSet<>();
	private MinedBlockData minedBlockData;
	private CandidateBlockData candidateBlockData;
	private FeeableData minedAndInMemPoolData;
	private FeeableData notMinedButInCandidateBlockData;
	private TimeSinceEnteredStatistics notMinedButInCandidateBlockMPTStatistics;
	private FeeableData minedInMempoolButNotInCandidateBlockData;
	private FeeableData minedButNotInMemPoolData;
	private long lostReward;
	private long lostRewardExcludingNotInMempoolTx;
	private int numTxInMempool;

	public IgnoringBlock() {

	}

	public IgnoringBlock(MisMinedTransactions mmt) {
		this.algorithmUsed = mmt.getAlgorithmUsed();
		this.inCandidateBlockButNotInMemPool = mmt.getInCandidateBlockButNotInMemPool();
		this.minedBlockData = mmt.getMinedBlockData();
		this.candidateBlockData = mmt.getCandidateBlockData();
		this.minedAndInMemPoolData = mmt.getMinedAndInMemPoolMapWD().getFeeableData();
		this.notMinedButInCandidateBlockData = mmt.getNotMinedButInCandidateBlockMapWD().getFeeableData();
		this.notMinedButInCandidateBlockMPTStatistics = mmt.getNotMinedButInCandidateBlockMPTStatistics();
		this.minedInMempoolButNotInCandidateBlockData = mmt.getMinedInMempoolButNotInCandidateBlockMapWD()
				.getFeeableData();
		this.minedButNotInMemPoolData = mmt.getMinedButNotInMemPoolMapWD().getFeeableData();
		this.lostReward = mmt.getLostReward();
		this.lostRewardExcludingNotInMempoolTx = mmt.getLostRewardExcludingNotInMempoolTx();
		this.numTxInMempool = mmt.getNumTxInMempool();
	}

	public AlgorithmType getAlgorithmUsed() {
		return algorithmUsed;
	}

	public Set<String> getInCandidateBlockButNotInMemPool() {
		return inCandidateBlockButNotInMemPool;
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

	public TimeSinceEnteredStatistics getNotMinedButInCandidateBlockMPTStatistics() {
		return notMinedButInCandidateBlockMPTStatistics;
	}

	public FeeableData getMinedInMempoolButNotInCandidateBlockData() {
		return minedInMempoolButNotInCandidateBlockData;
	}

	public FeeableData getMinedButNotInMemPoolData() {
		return minedButNotInMemPoolData;
	}

	public long getLostReward() {
		return lostReward;
	}

	public long getLostRewardExcludingNotInMempoolTx() {
		return lostRewardExcludingNotInMempoolTx;
	}

	public int getNumTxInMempool() {
		return numTxInMempool;
	}

}
