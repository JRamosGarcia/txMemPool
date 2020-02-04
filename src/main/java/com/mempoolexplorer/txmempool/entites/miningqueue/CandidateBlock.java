package com.mempoolexplorer.txmempool.entites.miningqueue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.utils.SysProps;

public class CandidateBlock {
	private int position = 0;// Position of this block in queue
	private int weight = 0;
	private int totalFees = 0;
	private int coinBaseWeight = 0;
	private int precedingTxsCount = 0; // Sum of all txs in preceding blocks

	private Map<String, TxToBeMined> txMap = new HashMap<String, TxToBeMined>(SysProps.HM_INITIAL_CAPACITY_FOR_BLOCK);

	// It's descending ordered because adds are ordered
	private LinkedList<TxToBeMined> txList = new LinkedList<>();

	private int nextTxPositionInBlock = 0;

	public CandidateBlock(int position, int coinBaseWeight) {
		this.position = position;
		this.coinBaseWeight = coinBaseWeight;
	}

	// Returns TxToBeMined created and added
	public TxToBeMined addTx(Transaction tx, Optional<Transaction> payingChildTx,
			Optional<List<Transaction>> reducedBy) {
		weight += tx.getWeight();
		totalFees += tx.getFees().getBase();
		TxToBeMined txToBeMined = new TxToBeMined(tx, payingChildTx, reducedBy, this, nextTxPositionInBlock++);
		txMap.put(tx.getTxId(), txToBeMined);
		txList.add(txToBeMined);
		return txToBeMined;
	}

	public Optional<TxToBeMined> getLastTx() {
		if (txList.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(txList.getLast());
	}

	public int getFreeSpace() {
		return SysProps.MAX_BLOCK_WEIGHT - coinBaseWeight - weight;
	}

	public Stream<Entry<String, TxToBeMined>> getEntriesStream() {
		return txMap.entrySet().stream();
	}

	// Remember that all candidateBlocks stream is disordered because of filling big
	// tx's space with other smaller tx
	public Stream<TxToBeMined> getOrderedStream() {
		return txList.stream();
	}

	public Optional<TxToBeMined> getTx(String txId) {
		return Optional.ofNullable(txMap.get(txId));
	}

	public boolean containsKey(String txId) {
		return txMap.containsKey(txId);
	}

	public int numTxs() {
		return txList.size();
	}

	public int getPosition() {
		return position;
	}

	public int getWeight() {
		return weight;
	}

	public int getTotalFees() {
		return totalFees;
	}

	public int getCoinBaseWeight() {
		return coinBaseWeight;
	}

	public int getPrecedingTxsCount() {
		return precedingTxsCount;
	}

	public void setPrecedingTxsCount(int precedingTxsCount) {
		this.precedingTxsCount = precedingTxsCount;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CandidateBlock [position=");
		builder.append(position);
		builder.append(", weight=");
		builder.append(weight);
		builder.append(", totalFees=");
		builder.append(totalFees);
		builder.append(", coinBaseWeight=");
		builder.append(coinBaseWeight);
		builder.append(", txList=");
		for (TxToBeMined txToBeMined : txList) {
			builder.append("txId: ");
			builder.append(txToBeMined.getTx().getTxId());
			builder.append(", pos: ");
			builder.append(txToBeMined.getPositionInBlock());
			builder.append(", s/vBIncAn: ");
			builder.append(txToBeMined.getTx().getSatvByteIncludingAncestors());
			builder.append(", (" + txToBeMined.getTx().getTxAncestry().getAncestorCount() + ","
					+ txToBeMined.getTx().getTxAncestry().getDescendantCount() + ")");
			if (txToBeMined.getPayingChildTx().isPresent()) {
				builder.append(", cp: " + txToBeMined.getPayingChildTx().get().getTxId());
			}
			if (txToBeMined.getReducedBy().isPresent()) {
				builder.append(", rb: ");
				Iterator<Transaction> it = txToBeMined.getReducedBy().get().iterator();
				while (it.hasNext()) {
					Transaction nextTx = it.next();
					builder.append(nextTx.getTxId());
					if (it.hasNext()) {
						builder.append(", ");
					}
				}
			}
			builder.append(SysProps.NL);
		}
		builder.append("]");
		return builder.toString();
	}

}
