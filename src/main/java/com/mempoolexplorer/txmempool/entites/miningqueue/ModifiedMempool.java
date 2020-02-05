package com.mempoolexplorer.txmempool.entites.miningqueue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.entites.mempool.TxKey;

public class ModifiedMempool {

	private static Logger logger = LoggerFactory.getLogger(ModifiedMempool.class);

	private Map<String, TxKey> txKeyMap = new HashMap<>();

	private SortedMap<TxKey, ModifiedTx> txMemPool = new TreeMap<>();

	public void put(ModifiedTx tx) {
		// Removes previous tx in map if any. Modifications in-map are not allowed
		remove(tx.getTx().getTxId());
		TxKey txKey = new TxKey(tx.getTx().getTxId(), tx.getRealAncestorSatVByte(), tx.getTx().getTimeInSecs());
		txKeyMap.put(tx.getTx().getTxId(), txKey);
		txMemPool.put(txKey, tx);
	}

	public Optional<ModifiedTx> remove(String txId) {
		TxKey txKey = txKeyMap.remove(txId);
		if (null != txKey) {
			Optional<ModifiedTx> opModTx = Optional.ofNullable(txMemPool.remove(txKey));
			if (opModTx.isEmpty()) {
				logger.info("Non existing TxWithRealSatVByte in ModifiedMempool for remove, txId: {}", txId);
			}
			return opModTx;
		}
		return Optional.empty();
	}

	public Optional<ModifiedTx> getBestThan(Transaction tx) {
		if(txMemPool.isEmpty()) {
			return Optional.empty();
		}
		// Get lastKey, if not present throws NotSuchElementException!!!
		TxKey lastKey = txMemPool.lastKey();
		// if tx is already in our map, return lastKey, it will be better because this
		// map is ordered
		if (txKeyMap.containsKey(tx.getTxId())) {
			return Optional.of(txMemPool.get(lastKey));
		}
		// Compare tx and lastKey to return lastKey if better
		TxKey txKey = new TxKey(tx.getTxId(), tx.getSatvByteIncludingAncestors(), tx.getTimeInSecs());
		if (lastKey.compareTo(txKey) > 0) {
			return Optional.of(txMemPool.get(lastKey));
		}
		return Optional.empty();
	}

	public void substractFeesTo(List<Transaction> notInAnyCandidateBlockTxListOf, Long fee) {
		for (Transaction tx : notInAnyCandidateBlockTxListOf) {
			Optional<ModifiedTx> opModTx = get(tx.getTxId());
			if (opModTx.isPresent()) {
				ModifiedTx modTx = opModTx.get();
				modTx.setRealAncestorFees(modTx.getRealAncestorFees() - fee);
				put(modTx);
			} else {
				ModifiedTx modTx = new ModifiedTx(tx, tx.getAncestorFees() - fee);
				put(modTx);
			}
		}

	}

	private Optional<ModifiedTx> get(String txId) {
		TxKey txKey = txKeyMap.get(txId);
		if (txKey == null) {
			return Optional.empty();
		}
		return Optional.of(txMemPool.get(txKey));
	}

}