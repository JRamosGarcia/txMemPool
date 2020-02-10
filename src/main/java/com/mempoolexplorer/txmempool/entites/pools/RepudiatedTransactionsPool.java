package com.mempoolexplorer.txmempool.entites.pools;

import java.util.List;
import java.util.Optional;

import com.mempoolexplorer.txmempool.entites.IgnoredTransaction;

public interface RepudiatedTransactionsPool {

	void put(IgnoredTransaction igTx);

	Optional<IgnoredTransaction> getRepudiatedTransaction(String txId);

	List<IgnoredTransaction> getRepudiatedTransactionList();

}
