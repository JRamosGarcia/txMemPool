package com.mempoolexplorer.txmempool.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.mempool.TxPoolChanges;
import com.mempoolexplorer.txmempool.entites.MiningQueue;
import com.mempoolexplorer.txmempool.entites.MisMinedTransactions;
import com.mempoolexplorer.txmempool.entites.NotMinedTransaction;
import com.mempoolexplorer.txmempool.properties.TxMempoolProperties;

@Component
public class TxMemPoolImpl implements TxMemPool {

	private class TxKey implements Comparable<TxKey> {
		private String txId;
		private Double satBytes;
		private Long firstSeenInSecs;

		public TxKey(String txId, Double satBytes, Long firstSeenInSecs) {
			super();
			this.txId = txId;
			this.satBytes = satBytes;
			this.firstSeenInSecs = firstSeenInSecs;
		}

		public String getTxId() {
			return txId;
		}

		public Double getSatBytes() {
			return satBytes;
		}

		public Long getFirstSeenInSecs() {
			return firstSeenInSecs;
		}

		@Override
		public int hashCode() {
			return txId.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TxKey other = (TxKey) obj;
//			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
//				return false;
			if (firstSeenInSecs == null) {
				if (other.firstSeenInSecs != null)
					return false;
			} else if (!firstSeenInSecs.equals(other.firstSeenInSecs))
				return false;
			if (satBytes == null) {
				if (other.satBytes != null)
					return false;
			} else if (!satBytes.equals(other.satBytes))
				return false;
			if (txId == null) {
				if (other.txId != null)
					return false;
			} else if (!txId.equals(other.txId))
				return false;
			return true;
		}

		@Override
		public int compareTo(TxKey o) {
			int satBytesCmp = this.getSatBytes().compareTo(o.getSatBytes());
			if (satBytesCmp != 0)
				return satBytesCmp;
			int firstSeenSecCmp = this.getFirstSeenInSecs().compareTo(o.getFirstSeenInSecs());
			if (firstSeenSecCmp != 0)
				return firstSeenSecCmp;
			return this.getTxId().compareTo(o.getTxId());
		}

//		private TxMemPoolImpl getEnclosingInstance() {
//			return TxMemPoolImpl.this;
//		}

	}

	@Autowired
	private TxMempoolProperties txMempoolProperties;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private AtomicReference<MiningQueue> miningQueueRef = new AtomicReference<>(new MiningQueue());
	private ConcurrentSkipListMap<TxKey, Transaction> txMemPool = new ConcurrentSkipListMap<>();
	// This is very anoying but necessary since txPoolChanges.getRemovedTxsId() are
	// Strings, not TxKeys. :-(
	private ConcurrentHashMap<String, TxKey> txKeyMap = new ConcurrentHashMap<>();

	private int numRefreshedWatcher = 0;// Counter for not refreshing miningQueue all the time

	private boolean updateFullTxMemPool = true;

	@Override
	public void refresh(TxPoolChanges txPoolChanges) {
		// We do no update mining queue if we are receiving the full txMemPool
		if (txPoolChanges.getChangeCounter() == 0) {
			if (updateFullTxMemPool) {
				logger.info("Receiving full txMemPool due to bitcoindAdapter (re)start. "
						+ "Dropping last txMemPool (if any) It can take a while...");
				drop();
				updateFullTxMemPool = false;
			}
			refreshTxMemPool(txPoolChanges);
		} else {
			updateFullTxMemPool = true;// Needed if bitcoindAdapter restarts
			refreshTxMemPool(txPoolChanges);

			logTxPoolChanges(txPoolChanges);
			logger.info("{} transactions in txMemPool.", txKeyMap.size());

			if (numRefreshedWatcher >= txMempoolProperties.getRefreshCountToCreateNewMiningQueue()) {
				updateMiningQueue();
				numRefreshedWatcher = 0;
			} else {
				miningQueueRef.get().setIsDirty();
			}
			numRefreshedWatcher++;
		}

	}

	private void logTxPoolChanges(TxPoolChanges txpc) {
		StringBuilder sb = new StringBuilder();
		sb.append("TxPoolChanges(");
		sb.append(txpc.getChangeCounter());
		sb.append("): ");
		sb.append(txpc.getNewTxs().size());
		sb.append(" new transactions, ");
		sb.append(txpc.getRemovedTxsId().size());
		sb.append(" removed transactions.");
		logger.info(sb.toString());
	}

	private void refreshTxMemPool(TxPoolChanges txPoolChanges) {
		txPoolChanges.getNewTxs().stream().forEach(tx -> {
			TxKey txKey = new TxKey(tx.getTxId(), tx.getSatBytes(), tx.getTimeInSecs());
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
	}

	@Override
	public void updateMiningQueue() {
		MiningQueue newMiningQueue = new MiningQueue();
		txMemPool.descendingMap().entrySet().stream().limit(txMempoolProperties.getMiningQueueNumTxs()).forEach(e -> {
			newMiningQueue.addTx(e.getValue());
		});
		this.miningQueueRef.set(newMiningQueue);
	}

	@Override
	public MisMinedTransactions calculateMisMinedTransactions(Block block, int numConsecutiveBlocks) {

		if (miningQueueRef.get().getIsDirty()) {
			updateMiningQueue();
		}

		MiningQueue miningQueue = miningQueueRef.get();

		// In block, but not in memPool
		Set<String> minedButNotInMemPool = new HashSet<>();
		// In block and memPool
		Map<String, Transaction> minedTransactionMap = new HashMap<>();

		block.getTxs().stream().forEach(txId -> {
			TxKey txKey = txKeyMap.get(txId);
			if (null == txKey) {
				minedButNotInMemPool.add(txId);
			} else {
				minedTransactionMap.put(txId, txMemPool.get(txKeyMap.get(txId)));
			}
		});

		Map<String, NotMinedTransaction> notMinedButInMiningQueueBlock = miningQueue
				.calculateNotMinedButInMiningQueueBlock(numConsecutiveBlocks, minedTransactionMap);

		Map<String, Transaction> minedButNotInMiningQueueBlock = miningQueue
				.calculateMinedButNotInMiningQueueBlock(numConsecutiveBlocks, minedTransactionMap);

		MisMinedTransactions mmt = new MisMinedTransactions();
		mmt.setBlockChangeTime(block.getChangeTime());
		mmt.setBlockHeight(block.getHeight());
		mmt.setNumTxInMinedBlock(block.getTxs().size());
		mmt.setMinedButNotInMemPool(minedButNotInMemPool);
		mmt.setNotMinedButInMiningQueueBlock(notMinedButInMiningQueueBlock);
		mmt.setMinedButNotInMiningQueueBlock(minedButNotInMiningQueueBlock);
		return mmt;
	}

	@Override
	public Integer getTxNumber() {
		return txKeyMap.size();
	}

	public void drop() {
		txMemPool = new ConcurrentSkipListMap<>();
		txKeyMap = new ConcurrentHashMap<>();
		miningQueueRef.set(new MiningQueue());
	}

}
