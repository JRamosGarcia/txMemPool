package com.mempoolexplorer.txmempool.entites;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;

/**
 * Class containing the mismached transactions between minedBlock and
 * mininigQueue
 */
public class MisMinedTransactions {

	Integer blockHeight;

	Integer numTxInMinedBlock;

	Instant blockChangeTime;// Mined time set by us, not mining operators.

	// Suspicious transactions of not been mined
	Map<String, NotMinedTransaction> notMinedButInMiningQueueBlock = new HashMap<>();

	// Suspicious transactions of replacing others that should be mined
	Map<String, Transaction> minedButNotInMiningQueueBlock = new HashMap<>();

	// Suspicious transactions of not been broadcasted
	Set<String> minedButNotInMemPool = new HashSet<>();

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

	public Map<String, NotMinedTransaction> getNotMinedButInMiningQueueBlock() {
		return notMinedButInMiningQueueBlock;
	}

	public void setNotMinedButInMiningQueueBlock(Map<String, NotMinedTransaction> notMinedButInMiningQueueFirstBlock) {
		this.notMinedButInMiningQueueBlock = notMinedButInMiningQueueFirstBlock;
	}

	public Map<String, Transaction> getMinedButNotInMiningQueueBlock() {
		return minedButNotInMiningQueueBlock;
	}

	public void setMinedButNotInMiningQueueBlock(Map<String, Transaction> minedButNotInMiningQueueBlock) {
		this.minedButNotInMiningQueueBlock = minedButNotInMiningQueueBlock;
	}

	public Set<String> getMinedButNotInMemPool() {
		return minedButNotInMemPool;
	}

	public void setMinedButNotInMemPool(Set<String> minedButNotInMemPool) {
		this.minedButNotInMemPool = minedButNotInMemPool;
	}

	@Override
	public String toString() {
		String nl = System.getProperty("line.separator");
		StringBuilder builder = new StringBuilder();
		builder.append(nl);
		builder.append("MisMinedTransactions:");
		builder.append(nl);
		builder.append("---------------------");
		builder.append(nl);
		builder.append(" blockHeight: ");
		builder.append(blockHeight);
		builder.append(nl);
		builder.append("numTxInMinedBlock: ");
		builder.append(numTxInMinedBlock);
		builder.append(nl);
		builder.append("blockChangeTime: ");
		builder.append(blockChangeTime);
		builder.append(nl);
		builder.append("notMinedButInMiningQueueBlock: ");
		builder.append(nl + "[" + nl);
		builder.append(String.join(nl, notMinedButInMiningQueueBlock.keySet().stream().collect(Collectors.toList())));
		builder.append(nl + "]" + nl);
		builder.append("minedButNotInMiningQueueBlock: ");
		builder.append(nl + "[" + nl);
		builder.append(String.join(nl, minedButNotInMiningQueueBlock.keySet().stream().collect(Collectors.toList())));
		builder.append(nl + "]" + nl);
		builder.append("minedButNotInMemPool: ");
		builder.append(nl + "[" + nl);
		builder.append(String.join(nl, minedButNotInMemPool.stream().collect(Collectors.toList())));
		builder.append(nl + "]" + nl);
		return builder.toString();
	}

}
