package com.mempoolexplorer.txmempool.entites;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

	//Statistics about the time since notMinedButInCandidateBlockMapWD txs entered in mempool.
	// This is helpful to find miners with connectivity issues (as the great
	// firewall of China).
	private TimeSinceEnteredStatistics notMinedButInCandidateBlockMPTStatistics;

	// Suspicious transactions of replacing others that should be mined
	private FeeableMapWithData<Transaction> minedInMempoolButNotInCandidateBlockMapWD = new FeeableMapWithData<>(
			SysProps.EXPECTED_MISMINED);

	// Suspicious transactions of not been broadcasted, only for check mempool
	// coherence
	private Set<String> minedButNotInMemPoolSet = new HashSet<>(SysProps.EXPECTED_MISMINED);

	// Suspicious transactions of not been broadcasted statistics
	private FeeableMapWithData<NotInMemPoolTx> minedButNotInMemPoolMapWD = new FeeableMapWithData<>(
			SysProps.EXPECTED_MISMINED);

	private long lostReward;
	private long lostRewardExcludingNotInMempoolTx;
	private int numTxInMempool;

	public MisMinedTransactions(TxMemPool txMemPool, CandidateBlock candidateBlock, Block block) {

		this.block = block;
		this.candidateBlock = candidateBlock;
		this.numTxInMempool = txMemPool.getTxNumber();

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

		calculateMinedBlockData();
		calculateCandidateBlockData();
		calculateLostReward();

		this.notMinedButInCandidateBlockMPTStatistics = new TimeSinceEnteredStatistics(
				minedBlockData.getChangeTime().getEpochSecond(),
				notMinedButInCandidateBlockMapWD.getFeeableMap().values().stream());

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
		feeableData.checkFees(candidateBlock.getEntriesStream().map(entry -> entry.getValue()));
		candidateBlockData = new CandidateBlockData(candidateBlock, feeableData);
	}

	private void calculateMinedBlockData() {
		FeeableData feeableData = new FeeableData();
		feeableData.checkOther(minedAndInMemPoolMapWD.getFeeableData());
		feeableData.checkOther(minedButNotInMemPoolMapWD.getFeeableData());
		minedBlockData = new MinedBlockData(block, feeableData);
	}

	private void calculateNotMinedButInCandidateBlock(CandidateBlock candidateBlock,
			FeeableMapWithData<Transaction> minedAndInMemPoolTxMap,
			FeeableMapWithData<NotMinedTransaction> notMinedButInCandidateBlockMap) {

		candidateBlock.getEntriesStream().filter(e -> !minedAndInMemPoolTxMap.containsKey(e.getKey())).map(e -> {
			return new NotMinedTransaction(e.getValue().getTx(), e.getValue().getPositionInBlock());
		}).forEach(nmt -> notMinedButInCandidateBlockMap.put(nmt));

	}

	public Block getBlock() {
		return block;
	}

	public MinedBlockData getMinedBlockData() {
		return minedBlockData;
	}

	public CandidateBlock getCandidateBlock() {
		return candidateBlock;
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

	public TimeSinceEnteredStatistics getNotMinedButInCandidateBlockMPTStatistics() {
		return notMinedButInCandidateBlockMPTStatistics;
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

	public long getLostReward() {
		return lostReward;
	}

	public long getLostRewardExcludingNotInMempoolTx() {
		return lostRewardExcludingNotInMempoolTx;
	}

	public int getNumTxInMempool() {
		return numTxInMempool;
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
		// builder.append(", candidateBlock=");
		// builder.append(candidateBlock);
		// builder.append(SysProps.NL);
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
		builder.append(", lostReward=");
		builder.append(lostReward);
		builder.append(SysProps.NL);
		builder.append(", lostRewardExcludingNotInMempoolTx=");
		builder.append(lostRewardExcludingNotInMempoolTx);
		builder.append(SysProps.NL);
		builder.append(", numTxInMempool=");
		builder.append(numTxInMempool);
		builder.append("]");
		return builder.toString();
	}

}
