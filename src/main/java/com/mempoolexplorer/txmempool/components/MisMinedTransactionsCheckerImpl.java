package com.mempoolexplorer.txmempool.components;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.txmempool.components.alarms.AlarmLogger;
import com.mempoolexplorer.txmempool.entites.MisMinedTransactions;
import com.mempoolexplorer.txmempool.properties.TxMempoolProperties;
import com.mempoolexplorer.txmempool.utils.SysProps;

@Component
public class MisMinedTransactionsCheckerImpl implements MisMinedTransactionsChecker {

	@Autowired
	private AlarmLogger alarmLogger;

	@Autowired
	private TxMempoolProperties txMempoolProperties;

	@Override
	public void check(MisMinedTransactions mmt) {

		checkMinedBlockData(mmt);
		checkCandidateBlockData(mmt);
		checkNotInMemPoolTxs(mmt);
		checkLostReward(mmt);
		checkConectivity(mmt);
		crossChecks(mmt);
	}

	private void checkConectivity(MisMinedTransactions mmt) {
		int numTxNotInMemPool = mmt.getMinedButNotInMemPoolMapWD().getFeeableData().getNumTxs().orElse(0);
		if (numTxNotInMemPool >= txMempoolProperties.getNumTxMinedButNotInMemPoolToRaiseAlarm()) {
			alarmLogger.addAlarm("NumTxMinedButNotInMemPool is: " + numTxNotInMemPool);
		}
	}

	// Checks if block.weight = sum of tx weight+coinbase+blockHeaderWeight
	private void checkMinedBlockData(MisMinedTransactions mmt) {
		int minedWeight = mmt.getMinedBlockData().getFeeableData().getTotalWeight().orElse(0);
		int coinbaseWeight = mmt.getMinedBlockData().getCoinBaseTx().getWeight();
		if (mmt.getMinedBlockData().getWeight() != (minedWeight + coinbaseWeight + SysProps.BLOCK_HEADER_WEIGHT)) {
			alarmLogger.addAlarm(
					"mmt.getMinedBlockData().getWeight() != (minedWeight + coinbaseWeight + SysProps.BLOCK_HEADER_WEIGHT)");
		}
		int minedAndInMemPoolWeight = mmt.getMinedAndInMemPoolMapWD().getFeeableData().getTotalWeight().orElse(0);
		int minedBotNotInMemPoolWeight = mmt.getMinedButNotInMemPoolMapWD().getFeeableData().getTotalWeight().orElse(0);
		if (minedWeight != (minedAndInMemPoolWeight + minedBotNotInMemPoolWeight + coinbaseWeight
				+ SysProps.BLOCK_HEADER_WEIGHT)) {
			alarmLogger.addAlarm(
					"minedWeight!=(minedAndInMemPoolWeight+minedBotNotInMemPoolWeight+coinbaseWeight+SysProps.BLOCK_HEADER_WEIGHT)");
		}
	}

	// Checks if fees, numTx and totalWeight in candidateBlock are the same as
	// counting/sum by this program
	private void checkCandidateBlockData(MisMinedTransactions mmt) {
		long candidateTotalFees = mmt.getCandidateBlockData().getFeeableData().getTotalBaseFee().orElse(0L);
		if (mmt.getCandidateBlockData().getTotalFees() != candidateTotalFees) {
			alarmLogger.addAlarm("mmt.getCandidateBlockData().getTotalFees() != candidateTotalFees");
		}
		int numTx = mmt.getCandidateBlockData().getFeeableData().getNumTxs().orElse(0);
		if (mmt.getCandidateBlockData().getNumTxs() != numTx) {
			alarmLogger.addAlarm("mmt.getCandidateBlockData().getNumTxs() != numTx");
		}
		int totalWeight = mmt.getCandidateBlockData().getFeeableData().getTotalWeight().orElse(0);
		if (mmt.getCandidateBlockData().getWeight() != totalWeight) {
			alarmLogger.addAlarm("mmt.getCandidateBlockData().getWeight() != totalWeight");
		}
	}

	private void crossChecks(MisMinedTransactions mmt) {
		int candidateBlockWeight = mmt.getCandidateBlockData().getWeight();
		int minedAndInMemPoolWeight = mmt.getMinedAndInMemPoolMapWD().getFeeableData().getTotalWeight().orElse(0);
		int notMinedButInCandidateBlockWeight = mmt.getNotMinedButInCandidateBlockMapWD().getFeeableData()
				.getTotalWeight().orElse(0);
		int minedInMempoolButNotInCandidateBlockWeight = mmt.getMinedInMempoolButNotInCandidateBlockMapWD()
				.getFeeableData().getTotalWeight().orElse(0);

		if ((candidateBlockWeight - minedAndInMemPoolWeight) != (notMinedButInCandidateBlockWeight
				- minedInMempoolButNotInCandidateBlockWeight)) {
			alarmLogger.addAlarm(
					"(candidateBlockWeight- minedAndInMemPoolWeight ) != (notMinedButInCandidateBlockWeight-minedInMempoolButNotInCandidateBlockWeight)");
		}

		long candidateBlockFees = mmt.getCandidateBlockData().getTotalFees();
		long minedAndInMemPoolFees = mmt.getMinedAndInMemPoolMapWD().getFeeableData().getTotalBaseFee().orElse(0L);
		long notMinedButInCandidateBlockFees = mmt.getNotMinedButInCandidateBlockMapWD().getFeeableData().getTotalBaseFee().orElse(0L);
		long minedInMempoolButNotInCandidateBlockFees = mmt.getMinedInMempoolButNotInCandidateBlockMapWD()
				.getFeeableData().getTotalBaseFee().orElse(0L);

		if ((candidateBlockFees - minedAndInMemPoolFees) != (notMinedButInCandidateBlockFees
				- minedInMempoolButNotInCandidateBlockFees)) {
			alarmLogger.addAlarm(
					"(candidateBlockFees- minedAndInMemPoolFees ) != (notMinedButInCandidateBlockFees-minedInMempoolButNotInCandidateBlockFees)");
		}
	}

	// Check if block.notInMemPoolTxSet+coinbase = minedButNotInMemPoolSet (for
	// mempool coherence)
	private void checkNotInMemPoolTxs(MisMinedTransactions mmt) {
		Set<String> blockSet = new HashSet<String>();
		blockSet.addAll(mmt.getBlock().getNotInMemPoolTransactions().keySet());
		blockSet.add(mmt.getBlock().getCoinBaseTx().getTxId());
		if (blockSet.size() != mmt.getMinedButNotInMemPoolSet().size()) {
			alarmLogger.addAlarm("blockSet.size() != mmt.getMinedButNotInMemPoolSet().size()");
			return;
		}
		if (!blockSet.stream().filter(txId -> !mmt.getMinedButNotInMemPoolSet().contains(txId))
				.collect(Collectors.toList()).isEmpty()
				|| !mmt.getMinedButNotInMemPoolSet().stream().filter(txId -> !blockSet.contains(txId))
						.collect(Collectors.toList()).isEmpty()) {
			alarmLogger.addAlarm("blockSet and minedButNotInMemPoolSet are not equals");
		}
	}

	// Check if getLostRewardExcludingNotInMempoolTx is negative
	private void checkLostReward(MisMinedTransactions mmt) {
		if (mmt.getLostRewardExcludingNotInMempoolTx() < 0L) {
			alarmLogger
					.addAlarm("Lost reward excluding not in mempool Tx: " + mmt.getLostRewardExcludingNotInMempoolTx()
							+ ", in block: " + mmt.getMinedBlockData().getHeight());
		}
	}
}
