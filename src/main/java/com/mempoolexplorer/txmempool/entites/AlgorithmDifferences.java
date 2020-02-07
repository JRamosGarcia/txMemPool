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

	private Set<String> notInMemPool = new HashSet<>();

	private Set<String> inBTNotInCB = new HashSet<>();

	private Set<String> inCBNotInBT = new HashSet<>();

	public AlgorithmDifferences(TxMemPool txMemPool, BlockTemplateContainer blockTemplateContainer,
			CandidateBlock candidateBlock) {

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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AlgorithmDifferences [notInMemPool=");
		builder.append(notInMemPool);
		builder.append("size: ");
		builder.append(notInMemPool.size());
		builder.append(", inBTNotInCB=");
		builder.append(inBTNotInCB);
		builder.append("size: ");
		builder.append(inBTNotInCB.size());
		builder.append(", inCBNotInBT=");
		builder.append(inCBNotInBT);
		builder.append("size: ");
		builder.append(inCBNotInBT.size());
		builder.append("]");
		return builder.toString();
	}

}
