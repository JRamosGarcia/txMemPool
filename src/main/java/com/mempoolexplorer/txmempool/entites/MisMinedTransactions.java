package com.mempoolexplorer.txmempool.entites;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.NotInMemPoolTx;
import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.entites.miningqueue.CandidateBlock;
import com.mempoolexplorer.txmempool.utils.SysProps;

/**
 * Class containing the mismached transactions between minedBlock and
 * mininigQueue
 */
public class MisMinedTransactions {

	private Block block;// Really mined
	private MinedBlockData minedBlockData;

	private CandidateBlock candidateBlock;// our Candidate
	private CandidateBlockData candidateBlockData;

	// Ok
	private FeeableMapWithData<Transaction> minedAndInMemPoolMapWD = new FeeableMapWithData<>(
			SysProps.EXPECTED_NUM_TX_IN_BLOCK);

	// Suspicious transactions of not been mined
	private FeeableMapWithData<NotMinedTransaction> notMinedButInCandidateBlockMapWD = new FeeableMapWithData<>(
			SysProps.EXPECTED_MISMINED);

	// Suspicious transactions of replacing others that should be mined
	private FeeableMapWithData<Transaction> minedInMempoolButNotInCandidateBlockMapWD = new FeeableMapWithData<>(
			SysProps.EXPECTED_MISMINED);

	// Suspicious transactions of not been broadcasted, only for check mempool
	// coherence
	private Set<String> minedButNotInMemPoolSet = new HashSet<>(SysProps.EXPECTED_MISMINED);

	// Suspicious transactions of not been broadcasted statistics
	private FeeableMapWithData<NotInMemPoolTx> minedButNotInMemPoolMapWD = new FeeableMapWithData<>(
			SysProps.EXPECTED_MISMINED);

	private Boolean coherentSets = true;

	private long lostReward;
	private long lostRewardExcludingNotInMempoolTx;

	public MisMinedTransactions(TxMemPool txMemPool, CandidateBlock candidateBlock, Block block) {

		this.block = block;
		this.candidateBlock = candidateBlock;

		block.getTxIds().stream().forEach(txId -> {
			Optional<Transaction> optTx = txMemPool.getTx(txId);
			if (optTx.isPresent()) {
				minedAndInMemPoolMapWD.put(optTx.get());
				if (!candidateBlock.containsKey(txId)) {
					minedInMempoolButNotInCandidateBlockMapWD.put(optTx.get());
				}
			} else {
				minedButNotInMemPoolSet.add(txId);
			}
		});

		// In mempool and candidateBlock but not in block
		calculateNotMinedButInCandidateBlock(candidateBlock, minedAndInMemPoolMapWD, notMinedButInCandidateBlockMapWD);

		// Mined but not in mempool
		block.getNotInMemPoolTransactions().values().forEach(nimTx -> minedButNotInMemPoolMapWD.put(nimTx));

		// Check for sanity
		checkNotInMemPoolTxs();

		calculateMinedBlockData();
		calculateCandidateBlockData();
		calculateLostReward();
	}

	private void calculateLostReward() {
		long notMinedReward = notMinedButInCandidateBlockMapWD.getFeeableData().getTotalBaseFee().orElse(0L);
		long minedReward = minedInMempoolButNotInCandidateBlockMapWD.getFeeableData().getTotalBaseFee().orElse(0L);
		long notInMemPoolReward = minedButNotInMemPoolMapWD.getFeeableData().getTotalBaseFee().orElse(0L);
		lostReward = notMinedReward - (minedReward + notInMemPoolReward);
		lostRewardExcludingNotInMempoolTx = notMinedReward - minedReward;
	}

	private void calculateCandidateBlockData() {
		FeeableData feeableData = new FeeableData();
		feeableData.checkOther(minedAndInMemPoolMapWD.getFeeableData());
		feeableData.checkOther(minedButNotInMemPoolMapWD.getFeeableData());
		candidateBlockData = new CandidateBlockData(candidateBlock, feeableData);
	}

	private void calculateMinedBlockData() {
		FeeableData feeableData = new FeeableData();
		feeableData.checkFees(candidateBlock.getEntriesStream().map(entry -> entry.getValue()));
		minedBlockData = new MinedBlockData(block, feeableData);
	}

	private void calculateNotMinedButInCandidateBlock(CandidateBlock candidateBlock,
			FeeableMapWithData<Transaction> minedAndInMemPoolTxMap,
			FeeableMapWithData<NotMinedTransaction> notMinedButInCandidateBlockMap) {

		candidateBlock.getEntriesStream().filter(e -> !minedAndInMemPoolTxMap.containsKey(e.getKey())).map(e -> {
			return new NotMinedTransaction(e.getValue().getTx(), e.getValue().getPositionInBlock());
		}).forEach(nmt -> notMinedButInCandidateBlockMap.put(nmt));

	}

	private void checkNotInMemPoolTxs() {
		Set<String> blockSet = new HashSet<String>();
		blockSet.addAll(block.getNotInMemPoolTransactions().keySet());
		blockSet.add(block.getCoinBaseTx().getTxId());
		if (blockSet.size() != minedButNotInMemPoolSet.size()) {
			coherentSets = false;
			return;
		}
		if (!blockSet.stream().filter(txId -> !minedButNotInMemPoolSet.contains(txId)).collect(Collectors.toList())
				.isEmpty()
				|| !minedButNotInMemPoolSet.stream().filter(txId -> !blockSet.contains(txId))
						.collect(Collectors.toList()).isEmpty()) {
			coherentSets = false;
			return;
		}
		coherentSets = true;
	}

	public MinedBlockData getMinedBlockData() {
		return minedBlockData;
	}

	public CandidateBlockData getCandidateBlockData() {
		return candidateBlockData;
	}

	public FeeableMapWithData<Transaction> getMinedAndInMemPoolMapWD() {
		return minedAndInMemPoolMapWD;
	}

	public FeeableMapWithData<NotMinedTransaction> getNotMinedButInCandidateBlockMapWD() {
		return notMinedButInCandidateBlockMapWD;
	}

	public FeeableMapWithData<Transaction> getMinedInMempoolButNotInCandidateBlockMapWD() {
		return minedInMempoolButNotInCandidateBlockMapWD;
	}

	public Set<String> getMinedButNotInMemPoolSet() {
		return minedButNotInMemPoolSet;
	}

	public FeeableMapWithData<NotInMemPoolTx> getMinedButNotInMemPoolMapWD() {
		return minedButNotInMemPoolMapWD;
	}

	public Boolean getCoherentSets() {
		return coherentSets;
	}

	public long getLostReward() {
		return lostReward;
	}

	public long getLostRewardExcludingNotInMempoolTx() {
		return lostRewardExcludingNotInMempoolTx;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MisMinedTransactions ["
				// + "block=");
				// builder.append(block);
				// builder.append(SysProps.NL);
				// builder.append(", "
				+ "minedBlockData=");
		builder.append(minedBlockData);
		builder.append(SysProps.NL);
		//builder.append(", candidateBlock=");
		//builder.append(candidateBlock);
		//builder.append(SysProps.NL);
		builder.append(", candidateBlockData=");
		builder.append(candidateBlockData);
		builder.append(SysProps.NL);
		builder.append(", minedAndInMemPoolMapWD=");
		builder.append(minedAndInMemPoolMapWD);
		builder.append(SysProps.NL);
		builder.append(", notMinedButInCandidateBlockMapWD=");
		builder.append(notMinedButInCandidateBlockMapWD);
		builder.append(SysProps.NL);
		builder.append(", minedInMempoolButNotInCandidateBlockMapWD=");
		builder.append(minedInMempoolButNotInCandidateBlockMapWD);
		builder.append(SysProps.NL);
		builder.append(", minedButNotInMemPoolSet=");
		builder.append(minedButNotInMemPoolSet);
		builder.append(SysProps.NL);
		builder.append(", minedButNotInMemPoolMapWD=");
		builder.append(minedButNotInMemPoolMapWD);
		builder.append(SysProps.NL);
		builder.append(", coherentSets=");
		builder.append(coherentSets);
		builder.append(SysProps.NL);
		builder.append(", lostReward=");
		builder.append(lostReward);
		builder.append(SysProps.NL);
		builder.append(", lostRewardExcludingNotInMempoolTx=");
		builder.append(lostRewardExcludingNotInMempoolTx);
		builder.append("]");
		return builder.toString();
	}

}
