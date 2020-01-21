package com.mempoolexplorer.txmempool.entites;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.utils.SysProps;

/**
 * MiningQueue is a one-shot class. Is expected to be created one time and
 * queryied many, until a new one is created due to mempool refreshing.
 */
public class MiningQueue {

	public class TxToBeMined {
		private Transaction tx;
		private Integer effectiveVSize;
		private QueuedBlock containingBlock;
		private int positionInBlock;

		public TxToBeMined(Transaction tx, QueuedBlock containedBlock, Integer effectiveVSize) {
			super();
			this.tx = tx;
			this.containingBlock = containedBlock;
			this.effectiveVSize = effectiveVSize;
			this.positionInBlock = 0;
		}

		public Transaction getTx() {
			return tx;
		}

		public Integer getEffectiveVSize() {
			return effectiveVSize;
		}

		public QueuedBlock getContainingBlock() {
			return containingBlock;
		}

		public int getPositionInBlock() {
			return positionInBlock;
		}

		public void setPositionInBlock(int positionInBlock) {
			this.positionInBlock = positionInBlock;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TxToBeMined other = (TxToBeMined) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (tx == null) {
				if (other.tx != null)
					return false;
			} else if (!tx.equals(other.tx))
				return false;
			return true;
		}

		private MiningQueue getEnclosingInstance() {
			return MiningQueue.this;
		}

	}

	public class QueuedBlock {
		private int position = 0;
		private int vSize = 0;
		private Map<String, TxToBeMined> txMap = new HashMap<String, TxToBeMined>(
				SysProps.HM_INITIAL_CAPACITY_FOR_BLOCK);

		// It's descending ordered because adds are ordered
		private LinkedList<TxToBeMined> txList = new LinkedList<>();

		public QueuedBlock(int position) {
			this.position = position;
		}

		public void addTx(Transaction tx, int txEfectiveSize) {
			vSize += txEfectiveSize;
			TxToBeMined txToBeMined = new TxToBeMined(tx, this, txEfectiveSize);
			txMap.put(tx.getTxId(), txToBeMined);
			txList.add(txToBeMined);

		}

		// remove a transaction already in this block
		public void remove(TxToBeMined txtbm) {
			vSize -= txtbm.getEffectiveVSize();
			txMap.remove(txtbm.getTx().getTxId());
			txList.remove(txtbm);
		}

		public int getFreeSpace() {
			return MAX_BLOCK_SIZE - vSize;
		}

		public Map<String, TxToBeMined> getTxMap() {
			return txMap;
		}

		public LinkedList<TxToBeMined> getTxList() {
			return txList;
		}

		public int getvSize() {
			return vSize;
		}

		public void setvSize(int vSize) {
			this.vSize = vSize;
		}

		public int getPosition() {
			return position;
		}

	}

	// Max block size (containing coinbase tx)
	private static final Integer MAX_BLOCK_SIZE = 1000000;

	// This is the block list.
	private ArrayList<QueuedBlock> blockList = new ArrayList<>();

	// These are the parent transactions which are mined due to dependecies and it's
	// fee is already included in a Tx
	private Set<String> parentsAlreadyInBlockTxIdSet = new HashSet<>();

	// These are the transactions used for padding a block.
	private Set<String> paddingTransactionTxIdSet = new HashSet<>();

	private QueuedBlock currentBlock = new QueuedBlock(0);

	private Boolean isDirty = Boolean.FALSE;

	/**
	 * Creates an empty mining queue
	 */
	public MiningQueue() {
		super();
		blockList.add(currentBlock);

	}

	/**
	 * Creates a miningQueue from txMemPool with maxTransactionsNumber
	 * 
	 * @param txMemPool
	 * @param maxTransactionsNumber
	 */
	public MiningQueue(TxMemPool txMemPool, Integer maxTransactionsNumber) {
		super();
		blockList.add(currentBlock);

		txMemPool.getDescendingTxStream().limit(maxTransactionsNumber).forEach(tx -> {
			Set<String> allParentsOfTx = txMemPool.getAllParentsOf(tx);// Excluding itself
			addTx(tx, allParentsOfTx);
		});
		setCurrentPositionInBlocks();
	}

	private void setCurrentPositionInBlocks() {
		blockList.stream().forEach(block -> {
			int counter = 0;
			Iterator<TxToBeMined> it = block.getTxList().iterator();
			while (it.hasNext()) {
				TxToBeMined txtbm = it.next();
				txtbm.setPositionInBlock(counter++);
			}
		});
	}

	// tx comes ordered in descending Sat/vByte
	public void addTx(Transaction tx, Set<String> allParentsOfTx) {
		if (parentsAlreadyInBlockTxIdSet.contains(tx.getTxId())) {
			// This tx is another's parent. It's fees are already accounted, ignore it.
			return;
		}
		if (allParentsOfTx.isEmpty()) {
			addSimpleTx(tx, allParentsOfTx);// Shortcut for efficiency. but below is the general case
		}
		// These two list do not overlap any element
		List<TxToBeMined> alreadyInCurrentBlockTxList = getAlreadyInCurrentBlockTxListOf(allParentsOfTx);
		List<TxToBeMined> alreadyInQueueBNICBTxList = getAlreadyInQueueButNotInCurrentBlockTxListFrom(allParentsOfTx);
		int alreadyInQueueBNICBParentsVSize = alreadyInQueueBNICBTxList.stream()
				.mapToInt(txtbm -> txtbm.getTx().getTxAncestry().getAncestorSize()).sum();

		// Effective size due to previous mining
		int txEffectiveVSizeInCurrentBlock = tx.getTxAncestry().getAncestorSize() - alreadyInQueueBNICBParentsVSize;

		if (currentBlock.getFreeSpace() > txEffectiveVSizeInCurrentBlock) {

			alreadyInCurrentBlockTxList.stream().forEach(txtbm -> {
				currentBlock.remove(txtbm);
			});

			currentBlock.addTx(tx, txEffectiveVSizeInCurrentBlock);

			parentsAlreadyInBlockTxIdSet.addAll(allParentsOfTx);
		} else {
			currentBlock = new QueuedBlock(currentBlock.getPosition() + 1);
			blockList.add(currentBlock);
			addTx(tx, allParentsOfTx);// Recursive only one level
		}
	}

	/**
	 * Adds a simple transaction, without parents in mempool
	 * 
	 * @param tx             txs comes ordered in descending Sat/vByte
	 * @param allParentsOfTx
	 */
	private void addSimpleTx(Transaction tx, Set<String> allParentsOfTx) {
		if (currentBlock.getFreeSpace() >= tx.getTxAncestry().getAncestorSize()) {
			currentBlock.addTx(tx, tx.getTxAncestry().getAncestorSize());
			parentsAlreadyInBlockTxIdSet.addAll(allParentsOfTx);
		} else {
			currentBlock = new QueuedBlock(currentBlock.getPosition() + 1);
			blockList.add(currentBlock);
			addSimpleTx(tx, allParentsOfTx);// Recursive only one level
		}
	}

	private List<TxToBeMined> getAlreadyInCurrentBlockTxListOf(Set<String> allParentsOfTx) {
		return allParentsOfTx.stream().map(txId -> currentBlock.getTxMap().get(txId)).collect(Collectors.toList());
	}

	private List<TxToBeMined> getAlreadyInQueueButNotInCurrentBlockTxListFrom(Set<String> allParentsOfTx) {
		return allParentsOfTx.stream().filter(txId -> !currentBlock.getTxMap().containsKey(txId))
				.map(txId -> getTxToBeMined(txId)).filter(Optional::isPresent).map(Optional::get)
				.collect(Collectors.toList());
	}

	public Optional<TxToBeMined> getTxToBeMined(String txId) {
		for (QueuedBlock block : blockList) {
			TxToBeMined txToBeMined = block.txMap.get(txId);
			if (txToBeMined != null)
				return Optional.of(txToBeMined);
		}
		return Optional.empty();
	}

	/**
	 * Removes txs from block tail up to coinbase vSize,
	 * 
	 * @param numConsecutiveBlocks The block we must substract the transactions to
	 * @param cbvSize              Coinbase vSize
	 */
	// TODO: Removed tx should be put in next block
	public void substractCoinBaseTxVSize(int numConsecutiveBlocks, Integer cbvSize) {
		QueuedBlock queuedBlock = blockList.get(numConsecutiveBlocks);
		int blockVSize = queuedBlock.getvSize();
		int maxVSize = MAX_BLOCK_SIZE - cbvSize;
		LinkedList<TxToBeMined> qbTxList = queuedBlock.getTxList();
		while ((qbTxList.size() > 0) && (blockVSize > maxVSize)) {
			TxToBeMined txToBeMined = qbTxList.pollLast();
			int txSize = txToBeMined.getEffectiveVSize();
			queuedBlock.getTxMap().remove(txToBeMined.getTx().getTxId());
			blockVSize -= txSize;
		}
		queuedBlock.setvSize(blockVSize);
	}

	public Map<String, NotMinedTransaction> calculateNotMinedButInCandidateBlock(int numConsecutiveBlocks,
			Map<String, Transaction> minedTransactionMap) {

		QueuedBlock queuedBlock = blockList.get(numConsecutiveBlocks);

		return queuedBlock.txMap.entrySet().stream().filter(e -> !minedTransactionMap.containsKey(e.getKey()))
				.map(e -> {
					return new NotMinedTransaction(e.getValue().getTx(), e.getValue().getPositionInBlock());
				}).collect(Collectors.toMap(nmTx -> nmTx.getTx().getTxId(), nmTx -> nmTx));
	}

	public Map<String, Transaction> calculateMinedInMempoolButNotInCandidateBlock(int numConsecutiveBlocks,
			Map<String, Transaction> minedTransactionMap) {
		QueuedBlock queuedBlock = blockList.get(numConsecutiveBlocks);

		return minedTransactionMap.entrySet().stream().filter(e -> !queuedBlock.txMap.containsKey(e.getKey()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	public Boolean getIsDirty() {
		return isDirty;
	}

	public void setIsDirty() {
		isDirty = Boolean.TRUE;
	}
}
