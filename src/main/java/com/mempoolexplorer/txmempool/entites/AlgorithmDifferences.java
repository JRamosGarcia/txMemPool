package com.mempoolexplorer.txmempool.entites;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.components.containers.BlockTemplateContainer;
import com.mempoolexplorer.txmempool.entites.blocktemplate.BlockTemplate;
import com.mempoolexplorer.txmempool.entites.miningqueue.CandidateBlock;

public class AlgorithmDifferences {

	// This three sets are disjoint. inBTNotInCB and inCBNotInBT does NOT contain
	// any elements from notInMemPool
	private int blockHeight;
	private Set<String> notInMemPool = new HashSet<>();
	private Set<String> inBTNotInCB = new HashSet<>();
	private Set<String> inCBNotInBT = new HashSet<>();

	public AlgorithmDifferences(TxMemPool txMemPool, BlockTemplateContainer blockTemplateContainer,
			CandidateBlock candidateBlock, int blockHeight) {
		this.blockHeight = blockHeight;

		BlockTemplate bt = blockTemplateContainer.getBlockTemplate();

		bt.getBlockTemplateTxMap().keySet().forEach(txId -> {
			Optional<Transaction> opTx = txMemPool.getTx(txId);
			if (opTx.isEmpty()) {
				notInMemPool.add(txId);
			} else {
				if (!candidateBlock.containsKey(txId)) {
					inBTNotInCB.add(txId);
				}
			}
		});

		candidateBlock.getEntriesStream().map(entry -> entry.getKey()).forEach(txId -> {
			Optional<Transaction> opTx = txMemPool.getTx(txId);
			if (opTx.isPresent()) {
				if (!bt.getBlockTemplateTxMap().containsKey(txId)) {
					inCBNotInBT.add(txId);
				}
			}
		});
	}

	public int getBlockHeight() {
		return blockHeight;
	}

	public Set<String> getNotInMemPool() {
		return notInMemPool;
	}

	public Set<String> getInBTNotInCB() {
		return inBTNotInCB;
	}

	public Set<String> getInCBNotInBT() {
		return inCBNotInBT;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AlgorithmDifferences [blockHeight=");
		builder.append(blockHeight);
		builder.append(", notInMemPool.size=");
		builder.append(notInMemPool.size());
		builder.append(", inBTNotInCB.size=");
		builder.append(inBTNotInCB.size());
		builder.append(", inCBNotInBT.size=");
		builder.append(inCBNotInBT.size());
		builder.append("]");
		return builder.toString();
	}

}
