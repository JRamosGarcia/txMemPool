package com.mempoolexplorer.txmempool.components;

import java.util.List;
import java.util.Optional;

import com.mempoolexplorer.txmempool.entites.IgnoredTransaction;

public interface RepudiatedTransactionsPool {

	void put(IgnoredTransaction igTx);

	Optional<IgnoredTransaction> getRepudiatedTransaction(String txId);

	List<IgnoredTransaction> getRepudiatedTransactionList();

}
