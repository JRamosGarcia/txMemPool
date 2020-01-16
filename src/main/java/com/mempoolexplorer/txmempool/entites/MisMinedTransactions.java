package com.mempoolexplorer.txmempool.entites;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
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
	MaxMinFeeTransactionMap<Transaction> minedAndInMemPool = new MaxMinFeeTransactionMap<>();

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

	private String nl = SysProps.NL;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(nl);
		builder.append("MisMinedTransactions:");
		builder.append(nl);
		builder.append("---------------------");
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
		builder.append("minedInMempoolButNotInCandidateBlock: ("
				+ minedInMempoolButNotInCandidateBlock.getTxMap().size() + "#tx, ");
		builder.append(calculateVSize(minedInMempoolButNotInCandidateBlock.getTxMap().values().stream()) + "vBytes)");
		buildTransactionLogStr(builder, minedInMempoolButNotInCandidateBlock, true);
		builder.append("minedButNotInMemPool: (#" + minedButNotInMemPool.size() + ")");
		builder.append(nl + "[" + nl);
		builder.append(String.join(nl, minedButNotInMemPool.stream().collect(Collectors.toList())));
		builder.append(nl + "]");
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
						+ Instant.ofEpochSecond(tx.getTimeInSecs()).atOffset(ZoneOffset.UTC).toString() + ", SatBytes: "
						+ tx.getSatBytes());
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
					+ ", SatBytes: " + nmt.getTx().getSatBytes() + ", PosInBlock: " + nmt.getOrdinalpositionInBlock());
		}
		builder.append(nl + "]" + nl);
	}

	private Integer calculateVSize(Stream<Transaction> txs) {
		return txs.mapToInt(tx -> tx.getvSize()).sum();
	}
}
