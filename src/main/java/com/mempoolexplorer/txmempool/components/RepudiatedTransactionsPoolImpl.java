package com.mempoolexplorer.txmempool.components;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.mempoolexplorer.txmempool.entites.IgnoredTransaction;

@Component
public class RepudiatedTransactionsPoolImpl implements RepudiatedTransactionsPool {

	// This map never gets garbage collected. But won't be a problem
	private Map<String, IgnoredTransaction> ignoredTransactionMap = new ConcurrentHashMap<>();

	@Override
	public void put(IgnoredTransaction igTx) {
		ignoredTransactionMap.put(igTx.getTx().getTxId(), igTx);
	}

	@Override
	public Optional<IgnoredTransaction> getRepudiatedTransaction(String txId) {
		return Optional.of(ignoredTransactionMap.get(txId));
	}

	@Override
	public List<IgnoredTransaction> getRepudiatedTransactionList() {
		return ignoredTransactionMap.values().stream().collect(Collectors.toList());
	}

}
