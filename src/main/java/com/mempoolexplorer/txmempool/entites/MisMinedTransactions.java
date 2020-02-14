package com.mempoolexplorer.txmempool.entites;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.NotInMemPoolTx;
import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.entites.blocktemplate.BlockTemplate;
import com.mempoolexplorer.txmempool.entites.miningqueue.CandidateBlock;
import com.mempoolexplorer.txmempool.entites.miningqueue.TxContainer;
import com.mempoolexplorer.txmempool.utils.SysProps;

/**
 * Class containing the mismached transactions between minedBlock and
 * mininigQueue
 */
public class MisMinedTransactions {

	private final static Logger logger = LoggerFactory.getLogger(MisMinedTransactions.class);

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
	public MisMinedTransactions(TxMemPool txMemPool, BlockTemplate blockTemplate, Block block,
			CoinBaseData coinBaseData) {
		algorithmUsed = AlgorithmType.BITCOIND;
		this.block = block;
		this.numTxInMempool = txMemPool.getTxNumber();

		calculateDataFromBlock(txMemPool, blockTemplate, block);

		// In mempool and candidateBlock but not in block
		calculateDataFromBlockTemplate(blockTemplate, txMemPool, block);

		calculateOtherData(block, coinBaseData);

	}

	// Constructor in case of CandidateBlock
	public MisMinedTransactions(TxMemPool txMemPool, CandidateBlock candidateBlock, Block block,
			CoinBaseData coinBaseData) {
		algorithmUsed = AlgorithmType.OURS;
		this.block = block;
		this.numTxInMempool = txMemPool.getTxNumber();

		calculateDataFromBlock(txMemPool, candidateBlock, block);

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

	private void calculateDataFromBlock(TxMemPool txMemPool, TxContainer txContainer, Block block) {
		block.getTxIds().stream().forEach(txId -> {
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

		candidateBlock.getEntriesStream().peek(e -> feeableData.checkFeeable(e.getValue()))
				.filter(e -> !minedAndInMemPoolMapWD.containsKey(e.getKey())).map(e -> {
					return new NotMinedTransaction(e.getValue().getTx(),
							Optional.of(e.getValue().getPositionInBlock()));
				}).forEach(nmt -> notMinedButInCandidateBlockMapWD.put(nmt));

		candidateBlockData = new CandidateBlockData(candidateBlock, feeableData);

	}

	private void calculateDataFromBlockTemplate(BlockTemplate blockTemplate, TxMemPool txMemPool, Block block) {

		CandidateBlockData cbd = new CandidateBlockData();
		FeeableData feeableData = new FeeableData();

		// Map<String, Transaction> blockTemplateTxMap = new
		// HashMap<>(SysProps.HM_INITIAL_CAPACITY_FOR_BLOCK);

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
					logger.error("BlockTemplate.entry.getFee: {}, tx.getBaseFees:{}, for txId:{} ",
							e.getValue().getFee(), tx.getBaseFees(), tx.getTxId());
				}
				cbd.setTotalFees(cbd.getTotalFees() + e.getValue().getFee());
				// if(e.getValue().getWeight()!=tx.getWeight()) {
				cbd.setWeight(cbd.getWeight() + e.getValue().getWeight());

			}
		});

		cbd.setCoinBaseWeight(block.getCoinBaseTx().getWeight());
		cbd.setIndex(0);
		cbd.setPrecedingTxsCount(0);
		cbd.setFeeableData(feeableData);
		candidateBlockData = cbd;

	}

	public AlgorithmType getAlgorithmUsed() {
		return algorithmUsed;
	}

	public Set<String> getInCandidateBlockButNotInMemPool() {
		return inCandidateBlockButNotInMemPool;
	}

	public Block getBlock() {
		return block;
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
