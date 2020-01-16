package com.mempoolexplorer.txmempool.entites;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.utils.SysProps;

/**
 * MiningQueue is a one-shot class. Is expected to be created one time and
 * queryied many, until a new one is created due to mempool refreshing.
 */
public class MiningQueue {

	public class TxToBeMined {
		private Transaction tx;
		private QueuedBlock containingBlock;
		private int positionInBlock;

		public TxToBeMined(Transaction tx, QueuedBlock containedBlock, int positionInBlock) {
			super();
			this.tx = tx;
			this.containingBlock = containedBlock;
			this.positionInBlock = positionInBlock;
		}

		public Transaction getTx() {
			return tx;
		}

		public QueuedBlock getContainingBlock() {
			return containingBlock;
		}

		public int getPositionInBlock() {
			return positionInBlock;
		}

	}

	public class QueuedBlock {
		private int position = 0;
		private int vSize = 0;
		private Map<String, TxToBeMined> txMap = new HashMap<String, TxToBeMined>(
				SysProps.HM_INITIAL_CAPACITY_FOR_BLOCK);
		private List<TxToBeMined> txList = new ArrayList<>(SysProps.EXPECTED_NUM_TX_IN_BLOCK);// It's descending ordered
																								// because
																								// adds are ordered

		public QueuedBlock(int position) {
			this.position = position;
		}

		private int currentTxPositionInBlock = 0;

		public boolean addTx(Transaction tx) {
			int nextvSize = this.vSize + tx.getvSize();
			if (nextvSize > MAX_BLOCK_SIZE)
				return false;
			this.vSize = nextvSize;
			// Circular references are not a problem for modern GCs
			TxToBeMined txToBeMined = new TxToBeMined(tx, this, currentTxPositionInBlock++);
			txMap.put(tx.getTxId(), txToBeMined);
			txList.add(txToBeMined);
			return true;
		}

		public Map<String, TxToBeMined> getTxMap() {
			return txMap;
		}

		public List<TxToBeMined> getTxList() {
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

	private static final Integer MAX_BLOCK_SIZE = 1000000;

	private ArrayList<QueuedBlock> blockList = new ArrayList<>();
	// private Map<String, TxToBeMined> txToBeMinedMap = new HashMap<String,
	// TxToBeMined>();

	private QueuedBlock currentBlock = new QueuedBlock(0);

	private Boolean isDirty = Boolean.FALSE;

	public MiningQueue() {
		super();
		blockList.add(currentBlock);
	}

	// tx comes orderer in descending Sat/vByte
	public void addTx(Transaction tx) {
		if (!currentBlock.addTx(tx)) {
			currentBlock = new QueuedBlock(currentBlock.getPosition() + 1);
			blockList.add(currentBlock);
			currentBlock.addTx(tx);
		}
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
	public void substractCoinBaseTxVSize(int numConsecutiveBlocks, Integer cbvSize) {
		QueuedBlock queuedBlock = blockList.get(numConsecutiveBlocks);
		int blockVSize = queuedBlock.getvSize();
		int maxVSize = MAX_BLOCK_SIZE - cbvSize;
		int listSize = queuedBlock.getTxList().size();
		while ((listSize > 0) && (blockVSize > maxVSize)) {
			int txI = listSize - 1;
			TxToBeMined txToBeMined = queuedBlock.getTxList().get(txI);
			int txSize = txToBeMined.getTx().getvSize();
			queuedBlock.getTxList().remove(txI);
			queuedBlock.getTxMap().remove(txToBeMined.getTx().getTxId());
			blockVSize -= txSize;
			listSize = queuedBlock.getTxList().size();
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
