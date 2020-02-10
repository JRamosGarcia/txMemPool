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
			addAlarm("NumTxMinedButNotInMemPool is: " + numTxNotInMemPool, mmt);
		}
	}

	// Checks if block.weight = sum of tx weight+coinbase+blockHeaderWeight
	private void checkMinedBlockData(MisMinedTransactions mmt) {
		int sumMinedWeight = mmt.getMinedBlockData().getFeeableData().getTotalWeight().orElse(0);
		int coinbaseWeight = mmt.getMinedBlockData().getCoinBaseTx().getWeight();
		if (mmt.getMinedBlockData().getWeight() != (sumMinedWeight + coinbaseWeight + SysProps.BLOCK_HEADER_WEIGHT)) {
			addAlarm(
					"mmt.getMinedBlockData().getWeight() != (minedWeight + coinbaseWeight + SysProps.BLOCK_HEADER_WEIGHT)"
							+ " in block: " + mmt.getMinedBlockData().getHeight(),
					mmt);
		}
		int minedWeight = mmt.getMinedBlockData().getWeight();
		int minedAndInMemPoolWeight = mmt.getMinedAndInMemPoolMapWD().getFeeableData().getTotalWeight().orElse(0);
		int minedBotNotInMemPoolWeight = mmt.getMinedButNotInMemPoolMapWD().getFeeableData().getTotalWeight().orElse(0);
		if (minedWeight != (minedAndInMemPoolWeight + minedBotNotInMemPoolWeight + coinbaseWeight
				+ SysProps.BLOCK_HEADER_WEIGHT)) {
			addAlarm(
					"minedWeight!=(minedAndInMemPoolWeight+minedBotNotInMemPoolWeight+coinbaseWeight+SysProps.BLOCK_HEADER_WEIGHT)"
							+ " in block: " + mmt.getMinedBlockData().getHeight(),
					mmt);
		}
	}

	// Checks if fees, numTx and totalWeight in candidateBlock are the same as
	// counting/sum by this program
	private void checkCandidateBlockData(MisMinedTransactions mmt) {
		long candidateTotalFees = mmt.getCandidateBlockData().getFeeableData().getTotalBaseFee().orElse(0L);
		if (mmt.getCandidateBlockData().getTotalFees() != candidateTotalFees) {
			addAlarm("mmt.getCandidateBlockData().getTotalFees() != candidateTotalFees" + " in block: "
					+ mmt.getMinedBlockData().getHeight(), mmt);

		}
		int numTx = mmt.getCandidateBlockData().getFeeableData().getNumTxs().orElse(0);
		if (mmt.getCandidateBlockData().getNumTxs() != numTx) {
			addAlarm("mmt.getCandidateBlockData().getNumTxs() != numTx" + " in block: "
					+ mmt.getMinedBlockData().getHeight(), mmt);

		}
		int totalWeight = mmt.getCandidateBlockData().getFeeableData().getTotalWeight().orElse(0);
		if (mmt.getCandidateBlockData().getWeight() != totalWeight) {
			addAlarm("mmt.getCandidateBlockData().getWeight() != totalWeight" + " in block: "
					+ mmt.getMinedBlockData().getHeight(), mmt);

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
			addAlarm(
					"(candidateBlockWeight- minedAndInMemPoolWeight ) != (notMinedButInCandidateBlockWeight-minedInMempoolButNotInCandidateBlockWeight)"
							+ " in block: " + mmt.getMinedBlockData().getHeight(),
					mmt);

		}

		long candidateBlockFees = mmt.getCandidateBlockData().getTotalFees();
		long minedAndInMemPoolFees = mmt.getMinedAndInMemPoolMapWD().getFeeableData().getTotalBaseFee().orElse(0L);
		long notMinedButInCandidateBlockFees = mmt.getNotMinedButInCandidateBlockMapWD().getFeeableData()
				.getTotalBaseFee().orElse(0L);
		long minedInMempoolButNotInCandidateBlockFees = mmt.getMinedInMempoolButNotInCandidateBlockMapWD()
				.getFeeableData().getTotalBaseFee().orElse(0L);

		if ((candidateBlockFees - minedAndInMemPoolFees) != (notMinedButInCandidateBlockFees
				- minedInMempoolButNotInCandidateBlockFees)) {
			addAlarm(
					"(candidateBlockFees- minedAndInMemPoolFees ) != (notMinedButInCandidateBlockFees-minedInMempoolButNotInCandidateBlockFees)"
							+ " in block: " + mmt.getMinedBlockData().getHeight(),
					mmt);

		}
	}

	// Check if block.notInMemPoolTxSet+coinbase = minedButNotInMemPoolSet (for
	// mempool coherence)
	private void checkNotInMemPoolTxs(MisMinedTransactions mmt) {
		Set<String> blockSet = new HashSet<String>();
		blockSet.addAll(mmt.getBlock().getNotInMemPoolTransactions().keySet());
		blockSet.add(mmt.getBlock().getCoinBaseTx().getTxId());
		if (blockSet.size() != mmt.getMinedButNotInMemPoolSet().size()) {
			addAlarm("blockSet.size() != mmt.getMinedButNotInMemPoolSet().size()", mmt);
			return;
		}
		if (!blockSet.stream().filter(txId -> !mmt.getMinedButNotInMemPoolSet().contains(txId))
				.collect(Collectors.toList()).isEmpty()
				|| !mmt.getMinedButNotInMemPoolSet().stream().filter(txId -> !blockSet.contains(txId))
						.collect(Collectors.toList()).isEmpty()) {
			addAlarm("blockSet and minedButNotInMemPoolSet are not equals" + " in block: "
					+ mmt.getMinedBlockData().getHeight(), mmt);

		}
	}

	// Check if getLostRewardExcludingNotInMempoolTx is negative
	private void checkLostReward(MisMinedTransactions mmt) {
		if (mmt.getLostRewardExcludingNotInMempoolTx() < 0L) {
			addAlarm("Lost reward excluding not in mempool Tx: " + mmt.getLostRewardExcludingNotInMempoolTx()
					+ ", in block: " + mmt.getMinedBlockData().getHeight(), mmt);
		}
	}

	private void addAlarm(String msg, MisMinedTransactions mmt) {
		String res = mmt.getAlgorithmUsed().toString();
		alarmLogger.addAlarm(res + "-" + msg);

	}
}
