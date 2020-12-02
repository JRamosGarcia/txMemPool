package com.mempoolexplorer.txmempool.bitcoindadapter.entites.mempool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.utils.SysProps;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TxPoolChanges {
	private List<Transaction> newTxs = new ArrayList<>();
	private List<String> removedTxsId = new ArrayList<>();
	private Map<String, TxAncestryChanges> txAncestryChangesMap = new HashMap<>(SysProps.EXPECTED_MAX_ANCESTRY_CHANGES);
}
