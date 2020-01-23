package com.mempoolexplorer.txmempool.components;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.mempool.TxPoolChanges;
import com.mempoolexplorer.txmempool.entites.mempool.TxKey;

@Component
public class TxMemPoolImpl implements TxMemPool {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private ConcurrentSkipListMap<TxKey, Transaction> txMemPool = new ConcurrentSkipListMap<>();

	// This is very anoying but necessary since txPoolChanges.getRemovedTxsId() are
	// Strings, not TxKeys. :-(
	private ConcurrentHashMap<String, TxKey> txKeyMap = new ConcurrentHashMap<>();

	private boolean updateFullTxMemPool = true;

	@Override
	public void refresh(TxPoolChanges txPoolChanges) {
		// We do no update mining queue if we are receiving the full txMemPool
		if (txPoolChanges.getChangeCounter() == 0) {
			if (updateFullTxMemPool) {
				logger.info("Receiving full txMemPool due to bitcoindAdapter/txMemPool (re)start. "
						+ "Dropping last txMemPool (if any) It can take a while...");
				drop();
				updateFullTxMemPool = false;
			}
			refreshTxMemPool(txPoolChanges);
		} else {
			if (!updateFullTxMemPool) {
				logger.info("Full txMemPool received!");
			}
			updateFullTxMemPool = true;// Needed if bitcoindAdapter restarts
			refreshTxMemPool(txPoolChanges);

			logTxPoolChanges(txPoolChanges);
			logger.info("{} transactions in txMemPool.", txKeyMap.size());
		}
	}

	@Override
	public Integer getTxNumber() {
		return txKeyMap.size();
	}

	@Override
	public Stream<Transaction> getDescendingTxStream() {
		return txMemPool.descendingMap().entrySet().stream().map(e -> e.getValue());
	}

	@Override
	public boolean contains(String txId) {
		return txKeyMap.contains(txId);
	}

	@Override
	public Optional<Transaction> getTx(String txId) {
		TxKey txKey = txKeyMap.get(txId);
		if (txKey == null)
			return Optional.empty();
		Transaction transaction = txMemPool.get(txKey);
		return Optional.ofNullable(transaction);
	}

	// TODO: must be tested
	@Override
	public Set<String> getAllParentsOf(Transaction tx) {
		// recursive witchcraft
		List<String> txDepends = tx.getTxAncestry().getDepends();
		if (!txDepends.isEmpty()) {
			Set<String> parentSet = txDepends.stream().collect(Collectors.toSet());
			Set<String> granpaSet = parentSet.stream().map(txId -> txKeyMap.get(txId)).filter(txKey -> txKey != null)
					.map(txKey -> txMemPool.get(txKey)).filter(trx -> trx != null).map(trx -> getAllParentsOf(trx))
					.flatMap(pSet -> pSet.stream()).collect(Collectors.toSet());
			parentSet.addAll(granpaSet);
			return parentSet;
		}
		return new HashSet<>();
	}

	private void drop() {
		txMemPool = new ConcurrentSkipListMap<>();
		txKeyMap = new ConcurrentHashMap<>();
	}

	private void refreshTxMemPool(TxPoolChanges txPoolChanges) {
		txPoolChanges.getNewTxs().stream().forEach(tx -> {
			TxKey txKey = new TxKey(tx.getTxId(), tx.getSatvByteIncludingAncestors(), tx.getTimeInSecs());
			txKeyMap.put(tx.getTxId(), txKey);
			txMemPool.put(txKey, tx);
		});
		txPoolChanges.getRemovedTxsId().stream().forEach(txId -> {
			TxKey txKey = txKeyMap.remove(txId);
			if (null != txKey) {
				txMemPool.remove(txKey);
			} else {
				logger.info("Removing non existing tx from mempool, txId: {}", txId);
			}
		});
		txPoolChanges.getTxAncestryChangesMap().entrySet().stream().forEach(entry -> {
			TxKey txKey = txKeyMap.get(entry.getKey());
			if (null == txKey) {
				logger.info("Non existing txKey in txKeyMap for update, txId: {}", entry.getKey());
				return;
			}
			Transaction oldTx = txMemPool.get(txKey);
			if (null == oldTx) {
				logger.info("Non existing tx in txMemPool for update, txId: {}", entry.getKey());
				return;
			}
			// This is safe since tx.getSatvByte() is calculated through ancestor fee/vSize
			// and it does not change between old and new Fee or TxAncestry classes.
			oldTx.setFees(entry.getValue().getFees());
			oldTx.setTxAncestry(entry.getValue().getTxAncestry());
		});

	}

	private void logTxPoolChanges(TxPoolChanges txpc) {
		StringBuilder sb = new StringBuilder();
		sb.append("TxPoolChanges(");
		sb.append(txpc.getChangeCounter());
		sb.append("): ");
		sb.append(txpc.getNewTxs().size());
		sb.append(" new transactions, ");
		sb.append(txpc.getRemovedTxsId().size());
		sb.append(" removed transactions, ");
		sb.append(txpc.getTxAncestryChangesMap().size());
		sb.append(" updated transactions.");
		logger.info(sb.toString());
	}

}
