package com.mempoolexplorer.txmempool.entites;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.NotInMemPoolTx;
import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.entites.blocktemplate.BlockTemplate;
import com.mempoolexplorer.txmempool.entites.miningqueue.CandidateBlock;
import com.mempoolexplorer.txmempool.entites.miningqueue.TxContainer;
import com.mempoolexplorer.txmempool.utils.SysProps;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Class containing the mismached transactions between minedBlock and
 * mininigQueue
 */
@Slf4j
@Getter
@ToString
public class MisMinedTransactions {

	private Block block;// Really mined
	private MinedBlockData minedBlockData;

	private CandidateBlockData candidateBlockData;

	// Ok
	private FeeableMapWithData<Transaction> minedAndInMemPoolMapWD = new FeeableMapWithData<>(
			SysProps.HM_INITIAL_CAPACITY_FOR_BLOCK);

	// Suspicious transactions of not been mined
	private FeeableMapWithData<NotMinedTransaction> notMinedButInCandidateBlockMapWD = new FeeableMapWithData<>(
			SysProps.HM_INITIAL_CAPACITY_FOR_EXPECTED_MISMINED);

	// Statistics about the time since notMinedButInCandidateBlockMapWD txs entered
	// in mempool.
	// This is helpful to find miners with connectivity issues (as the great
	// firewall of China).
	private TimeSinceEnteredStatistics notMinedButInCandidateBlockMPTStatistics;

	// Suspicious transactions of replacing others that should be mined
	private FeeableMapWithData<Transaction> minedInMempoolButNotInCandidateBlockMapWD = new FeeableMapWithData<>(
			SysProps.HM_INITIAL_CAPACITY_FOR_EXPECTED_MISMINED);

	// Suspicious transactions of not been broadcasted, only for check mempool
	// coherence
	private Set<String> minedButNotInMemPoolSet = new HashSet<>(SysProps.HM_INITIAL_CAPACITY_FOR_EXPECTED_MISMINED);

	// Only in the case of ALGORITHM_BITCOID, blockTemplate has some tx that are not
	// in mempool due to race conditions
	private Set<String> inCandidateBlockButNotInMemPool = new HashSet<>();

	// Suspicious transactions of not been broadcasted statistics
	private FeeableMapWithData<NotInMemPoolTx> minedButNotInMemPoolMapWD = new FeeableMapWithData<>(
			SysProps.HM_INITIAL_CAPACITY_FOR_EXPECTED_MISMINED);

	private long lostReward;
	private long lostRewardExcludingNotInMempoolTx;
	private int numTxInMempool;
	private AlgorithmType algorithmUsed;

	// Constructor in case of BlockTemplate
	public MisMinedTransactions(TxMemPool txMemPool, BlockTemplate blockTemplate, Block block, List<String> blockTxIds,
			CoinBaseData coinBaseData) {
		algorithmUsed = AlgorithmType.BITCOIND;
		this.block = block;
		this.numTxInMempool = txMemPool.getTxNumber();

		calculateDataFromBlock(txMemPool, blockTemplate, blockTxIds);

		// In mempool and candidateBlock but not in block
		calculateDataFromBlockTemplate(blockTemplate, txMemPool, block);

		calculateOtherData(block, coinBaseData);

	}

	// Constructor in case of CandidateBlock
	public MisMinedTransactions(TxMemPool txMemPool, CandidateBlock candidateBlock, Block block,
			List<String> blockTxIds, CoinBaseData coinBaseData) {
		algorithmUsed = AlgorithmType.OURS;
		this.block = block;
		this.numTxInMempool = txMemPool.getTxNumber();

		calculateDataFromBlock(txMemPool, candidateBlock, blockTxIds);

		// In mempool and candidateBlock but not in block
		calculateDataFrom(candidateBlock);

		calculateOtherData(block, coinBaseData);

	}

	private void calculateOtherData(Block block, CoinBaseData coinBaseData) {
		// Mined but not in mempool
		block.getNotInMemPoolTransactions().values().forEach(nimTx -> minedButNotInMemPoolMapWD.put(nimTx));

		calculateMinedBlockData(coinBaseData);
		calculateLostReward();

		this.notMinedButInCandidateBlockMPTStatistics = new TimeSinceEnteredStatistics(
				minedBlockData.getChangeTime().getEpochSecond(),
				notMinedButInCandidateBlockMapWD.getFeeableMap().values().stream());
	}

	private void calculateDataFromBlock(TxMemPool txMemPool, TxContainer txContainer, List<String> blockTxIds) {
		blockTxIds.stream().forEach(txId -> {
			Optional<Transaction> optTx = txMemPool.getTx(txId);
			if (optTx.isPresent()) {
				minedAndInMemPoolMapWD.put(optTx.get());
				if (!txContainer.containsKey(txId)) {
					minedInMempoolButNotInCandidateBlockMapWD.put(optTx.get());
				}
			} else {
				minedButNotInMemPoolSet.add(txId);
			}
		});
	}

	private void calculateLostReward() {
		long notMinedReward = notMinedButInCandidateBlockMapWD.getFeeableData().getTotalBaseFee().orElse(0L);
		long minedReward = minedInMempoolButNotInCandidateBlockMapWD.getFeeableData().getTotalBaseFee().orElse(0L);
		long notInMemPoolReward = minedButNotInMemPoolMapWD.getFeeableData().getTotalBaseFee().orElse(0L);
		lostReward = notMinedReward - (minedReward + notInMemPoolReward);
		lostRewardExcludingNotInMempoolTx = notMinedReward - minedReward;
	}

	private void calculateMinedBlockData(CoinBaseData coinBaseData) {
		FeeableData feeableData = new FeeableData();
		feeableData.checkOther(minedAndInMemPoolMapWD.getFeeableData());
		feeableData.checkOther(minedButNotInMemPoolMapWD.getFeeableData());
		minedBlockData = new MinedBlockData(block, feeableData, coinBaseData);
	}

	private void calculateDataFrom(CandidateBlock candidateBlock) {
		FeeableData feeableData = new FeeableData();

		candidateBlock.getEntriesStream().map(e -> {
			feeableData.checkFeeable(e.getValue());
			return e;
		}).filter(e -> !minedAndInMemPoolMapWD.containsKey(e.getKey()))
				.map(e -> new NotMinedTransaction(e.getValue().getTx(), Optional.of(e.getValue().getPositionInBlock())))
				.forEach(nmt -> notMinedButInCandidateBlockMapWD.put(nmt));

		candidateBlockData = new CandidateBlockData(candidateBlock, feeableData);

	}

	private void calculateDataFromBlockTemplate(BlockTemplate blockTemplate, TxMemPool txMemPool, Block block) {

		CandidateBlockData cbd = new CandidateBlockData();
		FeeableData feeableData = new FeeableData();

		blockTemplate.getBlockTemplateTxMap().entrySet().forEach(e -> {
			Optional<Transaction> opTx = txMemPool.getTx(e.getKey());
			if (opTx.isEmpty()) {
				inCandidateBlockButNotInMemPool.add(e.getKey());
			} else {
				Transaction tx = opTx.get();
				feeableData.checkFeeable(tx);
				if (!minedAndInMemPoolMapWD.containsKey(tx.getTxId())) {
					notMinedButInCandidateBlockMapWD.put(new NotMinedTransaction(tx, Optional.empty()));
				}

				cbd.setNumTxs(cbd.getNumTxs() + 1);
				if (e.getValue().getFee() != tx.getBaseFees()) {
					log.error("BlockTemplate.entry.getFee: {}, tx.getBaseFees:{}, for txId:{} ", e.getValue().getFee(),
							tx.getBaseFees(), tx.getTxId());
				}
				cbd.setTotalFees(cbd.getTotalFees() + e.getValue().getFee());
				cbd.setWeight(cbd.getWeight() + e.getValue().getWeight());

			}
		});

		cbd.setCoinBaseWeight(block.getCoinBaseTx().getWeight());
		cbd.setIndex(0);
		cbd.setPrecedingTxsCount(0);
		cbd.setFeeableData(feeableData);
		candidateBlockData = cbd;

	}

}
