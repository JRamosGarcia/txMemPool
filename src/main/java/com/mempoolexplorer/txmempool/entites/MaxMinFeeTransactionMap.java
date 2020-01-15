package com.mempoolexplorer.txmempool.entites;

import java.util.HashMap;
import java.util.Map;

/**
 * Map that stores transactions (whatever the type while implementing Feeable) and the max/min of satoshis per byte.
 */
public class MaxMinFeeTransactionMap<T extends Feeable> {
	MaxMinFeeTransactions maxMinFee = new MaxMinFeeTransactions();

	private Map<String, T> txMap = new HashMap<>();

	public MaxMinFeeTransactionMap() {
	}

	public MaxMinFeeTransactionMap(Map<String, T> map) {
		txMap = map;
		txMap.values().stream().forEach(tx -> maxMinFee.checkFees(tx));
	}

	public T put(T tx) {
		maxMinFee.checkFees(tx);
		return txMap.put(tx.getTxId(), tx);
	}

	public Map<String, T> getTxMap() {
		return txMap;
	}

	public MaxMinFeeTransactions getMaxMinFee() {
		return maxMinFee;
	}

}
