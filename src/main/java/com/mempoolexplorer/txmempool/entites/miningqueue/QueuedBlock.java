package com.mempoolexplorer.txmempool.entites.miningqueue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.utils.SysProps;

public class QueuedBlock {
	private int position = 0;
	private int vSize = 0;
	private Map<String, TxToBeMined> txMap = new HashMap<String, TxToBeMined>(SysProps.HM_INITIAL_CAPACITY_FOR_BLOCK);

	// It's descending ordered because adds are ordered
	private LinkedList<TxToBeMined> txList = new LinkedList<>();

	private int nextTxPositionInBlock = 0;

	private int coinBaseVSize = 0;

	public QueuedBlock(int position, int coinBaseVSize) {
		this.position = position;
		this.coinBaseVSize = coinBaseVSize;
	}

	public void addTx(Transaction tx) {
		vSize += tx.getvSize();
		TxToBeMined txToBeMined = new TxToBeMined(tx, this, nextTxPositionInBlock++);
		txMap.put(tx.getTxId(), txToBeMined);
		txList.add(txToBeMined);

	}

	public Transaction removeLastTx() {
		TxToBeMined pollLast = txList.pollLast();
		vSize -= pollLast.getTx().getvSize();
		nextTxPositionInBlock--;
		txMap.remove(pollLast.getTx().getTxId());
		return pollLast.getTx();
	}

	public TxToBeMined getLastTx() {
		return txList.getLast();
	}

	public int getFreeSpace() {
		return SysProps.MAX_BLOCK_SIZE - coinBaseVSize - vSize;
	}

	public Stream<Entry<String, TxToBeMined>> getEntriesStream(){
		return txMap.entrySet().stream();
	}
	
	public Optional<TxToBeMined> getTx(String txId) {
		return Optional.ofNullable(txMap.get(txId));
	}

	public boolean contains(String txId) {
		return txMap.containsKey(txId);
	}

	public int getvSize() {
		return vSize;
	}

	public int getPosition() {
		return position;
	}

	public int getCoinBaseVSize() {
		return coinBaseVSize;
	}

}
