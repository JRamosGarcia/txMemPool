package com.mempoolexplorer.txmempool.entites;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.NotInMemPoolTx;
import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.entites.miningqueue.CandidateBlock;
import com.mempoolexplorer.txmempool.utils.AsciiUtils;
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
	private Set<String> minedButNotInMemPoolSet = new HashSet<>();

	// Suspicious transactions of not been broadcasted statistics
	private MaxMinFeeTransactionMap<NotInMemPoolTx> minedButNotInMemPoolMap = new MaxMinFeeTransactionMap<>();

	// Ok
	private MaxMinFeeTransactionMap<Transaction> minedAndInMemPool = new MaxMinFeeTransactionMap<Transaction>();

	private CandidateBlock candidateBlock;// our Candidate

	private Block block;// Really mined

	private Boolean coherentSets = true;

	public static MisMinedTransactions from(TxMemPool txMemPool, CandidateBlock candidateBlock, Block block) {

		// In block, but not in memPool nor candidateBlock
		Set<String> minedButNotInMemPoolSet = new HashSet<>();
		// In block and memPool
		MaxMinFeeTransactionMap<Transaction> minedAndInMemPool = new MaxMinFeeTransactionMap<>(
				SysProps.EXPECTED_NUM_TX_IN_BLOCK);
		// In block and memPool but not in candidateBlock
		MaxMinFeeTransactionMap<Transaction> minedInMempoolButNotInCandidateBlockMap = new MaxMinFeeTransactionMap<>(
				SysProps.EXPECTED_MAX_IGNORED_TXS);

		block.getTxIds().stream().forEach(txId -> {
			Optional<Transaction> optTx = txMemPool.getTx(txId);
			if (optTx.isPresent()) {
				minedAndInMemPool.put(optTx.get());
				if (!candidateBlock.containsKey(txId)) {
					minedInMempoolButNotInCandidateBlockMap.put(optTx.get());
				}
			} else {
				minedButNotInMemPoolSet.add(txId);
			}
		});

		// In mempool and candidateBlock but not in block
		MaxMinFeeTransactionMap<NotMinedTransaction> notMinedButInCandidateBlockMap = calculateNotMinedButInCandidateBlock(
				candidateBlock, minedAndInMemPool.getTxMap());

		// Mined but not in mempool
		MaxMinFeeTransactionMap<NotInMemPoolTx> minedButNotInMemPoolMap = new MaxMinFeeTransactionMap<>();
		block.getNotInMemPoolTransactions().values().forEach(nimTx -> minedButNotInMemPoolMap.put(nimTx));

		MisMinedTransactions mmt = new MisMinedTransactions();
		mmt.setCoherentSets(checkNotInMemPoolTxs(block, minedButNotInMemPoolSet));
		mmt.setBlockChangeTime(block.getChangeTime());
		mmt.setBlockHeight(block.getHeight());
		mmt.setNumTxInMinedBlock(block.getTxIds().size());
		mmt.setMinedButNotInMemPoolSet(minedButNotInMemPoolSet);
		mmt.setMinedButNotInMemPoolMap(minedButNotInMemPoolMap);
		mmt.setMinedAndInMemPool(minedAndInMemPool);
		mmt.setNotMinedButInCandidateBlock(notMinedButInCandidateBlockMap);
		mmt.setMinedInMempoolButNotInCandidateBlock(minedInMempoolButNotInCandidateBlockMap);
		mmt.setCandidateBlock(candidateBlock);
		mmt.setBlock(block);
		return mmt;
	}

	private static MaxMinFeeTransactionMap<NotMinedTransaction> calculateNotMinedButInCandidateBlock(
			CandidateBlock candidateBlock, Map<String, Transaction> minedAndInMemPoolTxMap) {

		MaxMinFeeTransactionMap<NotMinedTransaction> notMinedButInCandidateBlockMap = new MaxMinFeeTransactionMap<>(
				SysProps.EXPECTED_MAX_IGNORED_TXS);

		candidateBlock.getEntriesStream().filter(e -> !minedAndInMemPoolTxMap.containsKey(e.getKey())).map(e -> {
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

	public Set<String> getMinedButNotInMemPoolSet() {
		return minedButNotInMemPoolSet;
	}

	public void setMinedButNotInMemPoolSet(Set<String> minedButNotInMemPoolSet) {
		this.minedButNotInMemPoolSet = minedButNotInMemPoolSet;
	}

	public MaxMinFeeTransactionMap<NotInMemPoolTx> getMinedButNotInMemPoolMap() {
		return minedButNotInMemPoolMap;
	}

	public void setMinedButNotInMemPoolMap(MaxMinFeeTransactionMap<NotInMemPoolTx> minedButNotInMemPoolMap) {
		this.minedButNotInMemPoolMap = minedButNotInMemPoolMap;
	}

	public MaxMinFeeTransactionMap<Transaction> getMinedAndInMemPool() {
		return minedAndInMemPool;
	}

	public void setMinedAndInMemPool(MaxMinFeeTransactionMap<Transaction> minedAndInMemPool) {
		this.minedAndInMemPool = minedAndInMemPool;
	}

	public CandidateBlock getCandidateBlock() {
		return candidateBlock;
	}

	public void setCandidateBlock(CandidateBlock candidateBlock) {
		this.candidateBlock = candidateBlock;
	}

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
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
		builder.append(calculateWUnits(minedAndInMemPool.getTxMap().values().stream()) + "wUnits)");
		buildTransactionLogStr(builder, minedAndInMemPool, false);
		builder.append("notMinedButInCandidateBlock: (" + notMinedButInCandidateBlock.getTxMap().size() + "#tx, ");
		builder.append(
				calculateWUnits(notMinedButInCandidateBlock.getTxMap().values().stream().map(nmtx -> nmtx.getTx()))
						+ "wUnits)");
		buildNotMinedTransactionLogStr(builder);
		builder.append(nl);
		builder.append("CandidateBlock: ");
		builder.append(candidateBlock);
		builder.append(nl);
		builder.append("minedInMempoolButNotInCandidateBlock: ("
				+ minedInMempoolButNotInCandidateBlock.getTxMap().size() + "#tx, ");
		builder.append(calculateWUnits(minedInMempoolButNotInCandidateBlock.getTxMap().values().stream()) + "wUnits)");
		buildTransactionLogStr(builder, minedInMempoolButNotInCandidateBlock, true);
		builder.append("minedButNotInMemPoolSet: (#" + minedButNotInMemPoolSet.size() + ")");
		builder.append(nl + "[" + nl);
		builder.append(String.join(nl, minedButNotInMemPoolSet.stream().collect(Collectors.toList())));
		builder.append(nl + "]");
		builder.append(nl);
		builder.append("CoinbaseTxId: " + block.getCoinBaseTx().getTxId());
		builder.append(nl);
		builder.append("CoinbaseField: " + block.getCoinBaseTx().getvInField());
		builder.append(nl);
		builder.append("CoinbaseWeight: " + block.getCoinBaseTx().getWeight());
		builder.append(nl);

		builder.append("Ascci: " + AsciiUtils.hexToAscii(block.getCoinBaseTx().getvInField()));
		builder.append(nl);
		builder.append("block.notInmempool: [");
		Iterator<NotInMemPoolTx> it = block.getNotInMemPoolTransactions().values().iterator();
		while (it.hasNext()) {
			NotInMemPoolTx tx = it.next();
			builder.append(nl);
			builder.append(tx.toString());
		}
		builder.append(nl);
		builder.append("]");
		builder.append("block.notInMempool.maxMinFee: ");
		builder.append(minedButNotInMemPoolMap.getMaxMinFee().toString());
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

	private Integer calculateWUnits(Stream<Transaction> txs) {
		return txs.mapToInt(tx -> tx.getWeight()).sum();
	}
}
