package com.mempoolexplorer.txmempool.entites;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Document(collection = "ignoringBlocks")
public class IgnoringBlock {

	@JsonIgnore
	@Id
	private String dbKey;

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
		this.dbKey = builDBKey();
	}

	public static String builDBKey(int height, AlgorithmType algoType) {
		return height + "-" + algoType.toString();
	}

	private String builDBKey() {
		return builDBKey(minedBlockData.getHeight(), algorithmUsed);
	}

}
