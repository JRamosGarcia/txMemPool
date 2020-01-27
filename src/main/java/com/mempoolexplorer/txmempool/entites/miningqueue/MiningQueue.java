package com.mempoolexplorer.txmempool.entites.miningqueue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.components.TxMemPool;

/**
 * MiningQueue is a one-shot class. Is expected to be created one time and
 * queryied many, until a new one is created due to mempool refreshing.
 * 
 * NOTE: We use the words "parent(s)" or "ancestor(s)" equally
 * 
 * MiningQueue is created using the mempool's txs stream ordered by descending
 * sat/vByte INCLUDING TX'S ANCESTORS. (This is used for CPFP or Child Pays For
 * Parent)
 * 
 * Constructor uses a coinBaseVSizeList, which is a template of blocks with that
 * coinbaseVSize. More QueuedBlocks could be created up to maxNumBlocks.
 * 
 */
public class MiningQueue {

	// This is the block list.
	private ArrayList<QueuedBlock> blockList = new ArrayList<>();
	private int maxNumBlocks = 0;

	private MiningQueue() {
	}

	public static MiningQueue buildFrom(List<Integer> coinBaseTxWeightList, TxMemPool txMemPool,
			Integer maxTransactionsNumber, Integer maxNumBlocks) {
		MiningQueue mq = new MiningQueue();
		mq.maxNumBlocks = Math.max(coinBaseTxWeightList.size(), maxNumBlocks);
		for (int index = 0; index < coinBaseTxWeightList.size(); index++) {
			mq.blockList.add(new QueuedBlock(index, coinBaseTxWeightList.get(index)));
		}

		txMemPool.getDescendingTxStream().limit(maxTransactionsNumber).forEach(tx -> {
			mq.addTx(tx, txMemPool);
		});
		return mq;
	}

	public static MiningQueue empty() {
		MiningQueue mq = new MiningQueue();
		return mq;
	}

	public Optional<QueuedBlock> getQueuedBlock(int index) {
		if (index < blockList.size()) {
			return Optional.of(blockList.get(index));
		}
		return Optional.empty();
	}

	// tx comes ordered in descending Sat/vByte
	private void addTx(Transaction tx, TxMemPool txMemPool) {

		if (getTxToBeMined(tx.getTxId()).isPresent()) {
			// This tx is another's parent that has been yet included in a block. Ignore it
			return;
		}

		Set<String> allParentsOfTx = txMemPool.getAllParentsOf(tx);// Excluding itself
		if (allParentsOfTx.isEmpty()) {
			addTxWithNoParentsTx(tx);
		} else {
			addTxWithParents(tx, txMemPool, allParentsOfTx);
		}
	}

	private void addTxWithNoParentsTx(Transaction noParentsTx) {
		Optional<QueuedBlock> blockToFill = getQueuedBlockToFill(noParentsTx);
		if (blockToFill.isPresent()) {
			blockToFill.get().addTx(noParentsTx);// It's a simple tx, no parents.
		}
	}

	private void addTxWithParents(Transaction tx, TxMemPool txMemPool, Set<String> allParentsOfTx) {
		List<Transaction> notInAnyBlock = getNotInAnyQueuedBlockTxListOf(allParentsOfTx, txMemPool);
		int notInAnyBlockWeight = notInAnyBlock.stream().mapToInt(trx -> trx.getWeight()).sum();

		int txEffectiveWeightInCurrentBlock = tx.getWeight() + notInAnyBlockWeight;

		Optional<QueuedBlock> blockToFill = getQueuedBlockToFill(txEffectiveWeightInCurrentBlock, allParentsOfTx);

		if (blockToFill.isPresent()) {
			notInAnyBlock.stream().forEach(trx -> {
				blockToFill.get().addTx(trx);
			});
			blockToFill.get().addTx(tx);
		}
	}

	private Optional<QueuedBlock> getQueuedBlockToFill(Transaction noParentsTx) {
		Iterator<QueuedBlock> it = blockList.iterator();
		while (it.hasNext()) {
			QueuedBlock block = it.next();
			if (block.getFreeSpace() >= noParentsTx.getWeight()) {// tx with no parents!
				return Optional.of(block);
			}
		}
		return createOrEmpty();
	}

	private Optional<QueuedBlock> getQueuedBlockToFill(int effectiveWeight, Set<String> allParentsOfTx) {
		List<TxToBeMined> inAnyQueuedBlockTxList = getInAnyQueuedBlockTxListOf(allParentsOfTx);
		Iterator<QueuedBlock> it = blockList.iterator();
		while (it.hasNext()) {
			QueuedBlock block = it.next();
			if (block.getFreeSpace() >= effectiveWeight) {
				// We cannot put a Tx if any of its parents is mined in a block after this one
				if (notAnyTxAfterQueuedBlockPosition(block.getPosition(), inAnyQueuedBlockTxList)) {
					return Optional.of(block);
				}
			}
		}
		return createOrEmpty();
	}

	private Optional<QueuedBlock> createOrEmpty() {
		if (blockList.size() < maxNumBlocks) {
			QueuedBlock qb = new QueuedBlock(blockList.size(), 0);
			blockList.add(qb);
			return Optional.of(qb);
		}
		return Optional.empty();
	}

	// return true if there is no TxToBeMined in txToBeMinedList which is going to
	// be mined in a block after blockPosition
	private boolean notAnyTxAfterQueuedBlockPosition(int blockPosition, List<TxToBeMined> txToBeMinedList) {
		for (TxToBeMined txToBeMined : txToBeMinedList) {
			if (txToBeMined.getContainingBlock().getPosition() > blockPosition) {
				return false;
			}
		}
		return true;
	}

	// Returns the list of txs that are in {@value allParentsOfTx} but not are in
	// any QueuedBlock
	private List<Transaction> getNotInAnyQueuedBlockTxListOf(Set<String> allParentsOfTx, TxMemPool txMemPool) {
		return allParentsOfTx.stream().filter(txId -> getTxToBeMined(txId).isEmpty()).map(txId -> txMemPool.getTx(txId))
				.filter(Optional::isPresent).map(op -> op.get()).collect(Collectors.toList());
	}

	// Returns the list of txs that are in {@value allParentsOfTx} and in any
	// QueuedBlock
	private List<TxToBeMined> getInAnyQueuedBlockTxListOf(Set<String> allParentsOfTx) {
		return allParentsOfTx.stream().map(txId -> getTxToBeMined(txId)).filter(Optional::isPresent).map(op -> op.get())
				.collect(Collectors.toList());
	}

	private Optional<TxToBeMined> getTxToBeMined(String txId) {
		for (QueuedBlock block : blockList) {
			Optional<TxToBeMined> optTxToBeMined = block.getTx(txId);
			if (optTxToBeMined.isPresent()) {
				return optTxToBeMined;
			}
		}
		return Optional.empty();
	}
}
