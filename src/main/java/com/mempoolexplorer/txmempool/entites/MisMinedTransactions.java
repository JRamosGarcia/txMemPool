package com.mempoolexplorer.txmempool.entites;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.entites.miningqueue.QueuedBlock;
import com.mempoolexplorer.txmempool.utils.SysProps;

/**
 * Class containing the mismached transactions between minedBlock and
 * mininigQueue
 */
public class MisMinedTransactions {

	private Integer blockHeight;

	private Integer numTxInMinedBlock;

	private Instant blockChangeTime;// Mined time set by us, not mining operators.

	// Suspicious transactions of not been mined
	private MaxMinFeeTransactionMap<NotMinedTransaction> notMinedButInCandidateBlock = new MaxMinFeeTransactionMap<>();

	// Suspicious transactions of replacing others that should be mined
	private MaxMinFeeTransactionMap<Transaction> minedInMempoolButNotInCandidateBlock = new MaxMinFeeTransactionMap<>();

	// Suspicious transactions of not been broadcasted
	private Set<String> minedButNotInMemPool = new HashSet<>();

	// Ok
	private MaxMinFeeTransactionMap<Transaction> minedAndInMemPool = new MaxMinFeeTransactionMap<Transaction>();

	private QueuedBlock queuedBlock;
	
	private Boolean coherentSets = true;

	public static MisMinedTransactions from(TxMemPool txMemPool, QueuedBlock queuedBlock, Block block,
			List<Integer> coinBaseTxVSizeList) {

		// In block, but not in memPool nor queuedBlock
		Set<String> minedButNotInMemPool = new HashSet<>();
		// In block and memPool
		MaxMinFeeTransactionMap<Transaction> minedAndInMemPool = new MaxMinFeeTransactionMap<>(
				SysProps.EXPECTED_NUM_TX_IN_BLOCK);
		// In block and memPool but not in queuedBlock
		MaxMinFeeTransactionMap<Transaction> minedInMempoolButNotInCandidateBlockMap = new MaxMinFeeTransactionMap<>(
				SysProps.EXPECTED_MAX_REPUDIATED_TXS);

		block.getTxIds().stream().forEach(txId -> {
			Optional<Transaction> optTx = txMemPool.getTx(txId);
			if (optTx.isPresent()) {
				minedAndInMemPool.put(optTx.get());
				if (!queuedBlock.containsKey(txId)) {
					minedInMempoolButNotInCandidateBlockMap.put(optTx.get());
				}
			} else {
				minedButNotInMemPool.add(txId);
			}
		});

		// In mempool and queuedBlock but not in block
		MaxMinFeeTransactionMap<NotMinedTransaction> notMinedButInCandidateBlockMap = calculateNotMinedButInCandidateBlock(
				queuedBlock, minedAndInMemPool.getTxMap());

		MisMinedTransactions mmt = new MisMinedTransactions();
		mmt.setCoherentSets(checkNotInMemPoolTxs(block, minedButNotInMemPool));
		mmt.setBlockChangeTime(block.getChangeTime());
		mmt.setBlockHeight(block.getHeight());
		mmt.setNumTxInMinedBlock(block.getTxIds().size());
		mmt.setMinedButNotInMemPool(minedButNotInMemPool);
		mmt.setMinedAndInMemPool(minedAndInMemPool);
		mmt.setNotMinedButInCandidateBlock(notMinedButInCandidateBlockMap);
		mmt.setMinedInMempoolButNotInCandidateBlock(minedInMempoolButNotInCandidateBlockMap);
		mmt.setQueuedBlock(queuedBlock);
		return mmt;
	}

	private static MaxMinFeeTransactionMap<NotMinedTransaction> calculateNotMinedButInCandidateBlock(
			QueuedBlock queuedBlock, Map<String, Transaction> minedAndInMemPoolTxMap) {

		MaxMinFeeTransactionMap<NotMinedTransaction> notMinedButInCandidateBlockMap = new MaxMinFeeTransactionMap<>(
				SysProps.EXPECTED_MAX_REPUDIATED_TXS);

		queuedBlock.getEntriesStream().filter(e -> !minedAndInMemPoolTxMap.containsKey(e.getKey())).map(e -> {
			return new NotMinedTransaction(e.getValue().getTx(), e.getValue().getPositionInBlock());
		}).forEach(nmt -> notMinedButInCandidateBlockMap.put(nmt));

		return notMinedButInCandidateBlockMap;
	}

	private static boolean checkNotInMemPoolTxs(Block block, Set<String> minedButNotInMemPool) {
		Set<String> blockSet = new HashSet<String>();
		blockSet.addAll(block.getNotInMemPoolTransactions().keySet());
		blockSet.add(block.getCoinBaseTx().getTxId());
		if (blockSet.size() != minedButNotInMemPool.size()) {
			return false;
		}
		if (!blockSet.stream().filter(txId -> !minedButNotInMemPool.contains(txId)).collect(Collectors.toList())
				.isEmpty()
				|| !minedButNotInMemPool.stream().filter(txId -> !blockSet.contains(txId)).collect(Collectors.toList())
						.isEmpty()) {
			return false;
		}
		return true;
	}

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

	public Instant getBlockChangeTime() {
		return blockChangeTime;
	}

	public void setBlockChangeTime(Instant blockChangeTime) {
		this.blockChangeTime = blockChangeTime;
	}

	public MaxMinFeeTransactionMap<NotMinedTransaction> getNotMinedButInCandidateBlock() {
		return notMinedButInCandidateBlock;
	}

	public void setNotMinedButInCandidateBlock(
			MaxMinFeeTransactionMap<NotMinedTransaction> notMinedButInCandidateBlock) {
		this.notMinedButInCandidateBlock = notMinedButInCandidateBlock;
	}

	public MaxMinFeeTransactionMap<Transaction> getMinedInMempoolButNotInCandidateBlock() {
		return minedInMempoolButNotInCandidateBlock;
	}

	public void setMinedInMempoolButNotInCandidateBlock(
			MaxMinFeeTransactionMap<Transaction> minedInMempoolButNotInCandidateBlock) {
		this.minedInMempoolButNotInCandidateBlock = minedInMempoolButNotInCandidateBlock;
	}

	public Set<String> getMinedButNotInMemPool() {
		return minedButNotInMemPool;
	}

	public void setMinedButNotInMemPool(Set<String> minedButNotInMemPool) {
		this.minedButNotInMemPool = minedButNotInMemPool;
	}

	public MaxMinFeeTransactionMap<Transaction> getMinedAndInMemPool() {
		return minedAndInMemPool;
	}

	public void setMinedAndInMemPool(MaxMinFeeTransactionMap<Transaction> minedAndInMemPool) {
		this.minedAndInMemPool = minedAndInMemPool;
	}

	public QueuedBlock getQueuedBlock() {
		return queuedBlock;
	}

	public void setQueuedBlock(QueuedBlock queuedBlock) {
		this.queuedBlock = queuedBlock;
	}

	public Boolean getCoherentSets() {
		return coherentSets;
	}

	public void setCoherentSets(Boolean coherentSets) {
		this.coherentSets = coherentSets;
	}

	private String nl = SysProps.NL;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(nl);
		builder.append("MisMinedTransactions:");
		builder.append(nl);
		builder.append("---------------------");
		builder.append(nl);
		builder.append("COHERENTSETS: ");
		builder.append(coherentSets);
		builder.append(nl);
		builder.append("blockHeight: ");
		builder.append(blockHeight);
		builder.append(nl);
		builder.append("numTxInMinedBlock: ");
		builder.append(numTxInMinedBlock);
		builder.append(nl);
		builder.append("blockChangeTime: ");
		builder.append(blockChangeTime);
		builder.append(nl);
		builder.append(nl);
		builder.append("minedAndInMemPool: (" + minedAndInMemPool.getTxMap().size() + "#tx, ");
		builder.append(calculateVSize(minedAndInMemPool.getTxMap().values().stream()) + "vBytes)");
		buildTransactionLogStr(builder, minedAndInMemPool, false);
		builder.append("notMinedButInCandidateBlock: (" + notMinedButInCandidateBlock.getTxMap().size() + "#tx, ");
		builder.append(
				calculateVSize(notMinedButInCandidateBlock.getTxMap().values().stream().map(nmtx -> nmtx.getTx()))
						+ "vBytes)");
		buildNotMinedTransactionLogStr(builder);
		builder.append(nl);
		builder.append("QueuedBlock: ");
		builder.append(queuedBlock);
		builder.append(nl);
		builder.append("minedInMempoolButNotInCandidateBlock: ("
				+ minedInMempoolButNotInCandidateBlock.getTxMap().size() + "#tx, ");
		builder.append(calculateVSize(minedInMempoolButNotInCandidateBlock.getTxMap().values().stream()) + "vBytes)");
		buildTransactionLogStr(builder, minedInMempoolButNotInCandidateBlock, true);
		builder.append("minedButNotInMemPool: (#" + minedButNotInMemPool.size() + ")");
		builder.append(nl + "[" + nl);
		builder.append(String.join(nl, minedButNotInMemPool.stream().collect(Collectors.toList())));
		builder.append(nl + "]");
		builder.append(nl);
		builder.append("COHERENTSETS: ");
		builder.append(coherentSets);
		return builder.toString();
	}

	private void buildTransactionLogStr(StringBuilder builder, MaxMinFeeTransactionMap<Transaction> mmftp,
			boolean logAllTxs) {
		builder.append(nl + "[" + nl);
		builder.append(mmftp.getMaxMinFee().toString());
		if (logAllTxs) {
			Iterator<Transaction> it = mmftp.getTxMap().values().iterator();
			while (it.hasNext()) {
				Transaction tx = it.next();
				builder.append(nl);
				builder.append(tx.getTxId() + ", "
						+ Instant.ofEpochSecond(tx.getTimeInSecs()).atOffset(ZoneOffset.UTC).toString() + ", SatvByte: "
						+ tx.getSatvByteIncludingAncestors());
			}
		}
		builder.append(nl + "]" + nl);
	}

	private void buildNotMinedTransactionLogStr(StringBuilder builder) {
		builder.append(nl + "[" + nl);
		builder.append(notMinedButInCandidateBlock.getMaxMinFee().toString());
		Iterator<NotMinedTransaction> it = notMinedButInCandidateBlock.getTxMap().values().iterator();
		while (it.hasNext()) {
			NotMinedTransaction nmt = it.next();
			builder.append(nl);
			builder.append(nmt.getTx().getTxId() + ", "
					+ Instant.ofEpochSecond(nmt.getTx().getTimeInSecs()).atOffset(ZoneOffset.UTC).toString()
					+ ", SatvByte: " + nmt.getTx().getSatvByteIncludingAncestors() + ", PosInBlock: "
					+ nmt.getOrdinalpositionInBlock());
		}
		builder.append(nl + "]" + nl);
	}

	private Integer calculateVSize(Stream<Transaction> txs) {
		return txs.mapToInt(tx -> tx.getvSize()).sum();
	}
}
