package com.mempoolexplorer.txmempool.entites.miningqueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Ancestors of a paying tx (CPFP) are put before paying tx child. then paying
 * tx child is inserted. This ensures an almost descending MiningQueue by
 * satVByte
 * 
 * Constructor uses a coinBaseVSizeList, which is a template of blocks with that
 * coinbaseVSize. More CandidateBlocks could be created up to maxNumBlocks.
 * 
 */
public class MiningQueue {
	private static Logger logger = LoggerFactory.getLogger(MiningQueue.class);

	// This is the block list.
	private ArrayList<CandidateBlock> blockList = new ArrayList<>();
	private int maxNumBlocks = 0;
	private double lastSatVByte = Double.MAX_VALUE;
	private boolean hadErrors = false;
	// This maps doubles this class size but enable fast lookups.
	private Map<String, TxToBeMined> globalTxsMap = new HashMap<>();

	private MiningQueue() {
	}

	public static MiningQueue buildFrom(List<Integer> coinBaseTxWeightList, TxMemPool txMemPool,
			Integer maxTransactionsNumber, Integer maxNumBlocks) {
		logger.info("Creating new MiningQueue...");
		MiningQueue mq = new MiningQueue();
		mq.maxNumBlocks = Math.max(coinBaseTxWeightList.size(), maxNumBlocks);
		for (int index = 0; index < coinBaseTxWeightList.size(); index++) {
			mq.blockList.add(new CandidateBlock(index, coinBaseTxWeightList.get(index)));
		}

		txMemPool.getDescendingTxStream().limit(maxTransactionsNumber).forEach(tx -> {
			mq.addTx(tx, txMemPool);
			checkIsDescending(tx, mq);
		});
		calculatePrecedingTxsCount(mq);
		logger.info("New MiningQueue created.");
		return mq;
	}

	public boolean isHadErrors() {
		return hadErrors;
	}

	// Checks if txMemPool is giving txs in descending order
	private static void checkIsDescending(Transaction tx, MiningQueue mq) {
		if (tx.getSatvByteIncludingAncestors() > mq.lastSatVByte) {
			mq.hadErrors = true;
		} else {
			mq.lastSatVByte = tx.getSatvByteIncludingAncestors();
		}
	}

	private static void calculatePrecedingTxsCount(MiningQueue mq) {
		int txCount = 0;
		for (CandidateBlock block : mq.blockList) {
			block.setPrecedingTxsCount(txCount);
			txCount += block.numTxs();
		}
	}

	public static MiningQueue empty() {
		MiningQueue mq = new MiningQueue();
		return mq;
	}

	public int getNumCandidateBlocks() {
		return blockList.size();
	}

	public Optional<CandidateBlock> getCandidateBlock(int index) {
		if (index < blockList.size()) {
			return Optional.of(blockList.get(index));
		}
		return Optional.empty();
	}

	// searches for a TxToBeMined
	public Optional<TxToBeMined> getTxToBeMined(String txId) {
		return Optional.ofNullable(globalTxsMap.get(txId));
	}

	public boolean contains(String txId) {
		return (globalTxsMap.get(txId) != null);
	}

	// tx comes ordered in descending Sat/vByte
	private void addTx(Transaction tx, TxMemPool txMemPool) {

		if (contains(tx.getTxId())) {
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
		Optional<CandidateBlock> blockToFill = getCandidateBlockToFill(noParentsTx);
		if (blockToFill.isPresent()) {
			TxToBeMined txToBeMined = blockToFill.get().addTx(noParentsTx, Optional.empty());// It's a simple tx, no
																								// parents.
			globalTxsMap.put(noParentsTx.getTxId(), txToBeMined);
		}
	}

	private void addTxWithParents(Transaction tx, TxMemPool txMemPool, Set<String> allParentsOfTx) {
		List<Transaction> notInAnyBlock = getNotInAnyCandidateBlockTxListOf(allParentsOfTx, txMemPool);
		int notInAnyBlockWeight = notInAnyBlock.stream().mapToInt(trx -> trx.getWeight()).sum();

		int txEffectiveWeightInCurrentBlock = tx.getWeight() + notInAnyBlockWeight;

		Optional<CandidateBlock> blockToFill = getCandidateBlockToFill(txEffectiveWeightInCurrentBlock, allParentsOfTx);

		if (blockToFill.isPresent()) {
			notInAnyBlock.stream().forEach(trx -> {
				TxToBeMined txToBeMined = blockToFill.get().addTx(trx, Optional.of(tx));
				globalTxsMap.put(trx.getTxId(), txToBeMined);

			});
			TxToBeMined txToBeMined = blockToFill.get().addTx(tx, Optional.empty());
			globalTxsMap.put(tx.getTxId(), txToBeMined);
		}
	}

	private Optional<CandidateBlock> getCandidateBlockToFill(Transaction noParentsTx) {
		Iterator<CandidateBlock> it = blockList.iterator();
		while (it.hasNext()) {
			CandidateBlock block = it.next();
			if (block.getFreeSpace() >= noParentsTx.getWeight()) {// tx with no parents!
				return Optional.of(block);
			}
		}
		return createOrEmpty();
	}

	private Optional<CandidateBlock> getCandidateBlockToFill(int effectiveWeight, Set<String> allParentsOfTx) {
		List<TxToBeMined> inAnyCandidateBlockTxList = getInAnyCandidateBlockTxListOf(allParentsOfTx);
		Iterator<CandidateBlock> it = blockList.iterator();
		while (it.hasNext()) {
			CandidateBlock block = it.next();
			if (block.getFreeSpace() >= effectiveWeight) {
				// We cannot put a Tx if any of its parents is mined in a block after this one
				if (notAnyTxAfterCandidateBlockPosition(block.getPosition(), inAnyCandidateBlockTxList)) {
					return Optional.of(block);
				}
			}
		}
		return createOrEmpty();
	}

	private Optional<CandidateBlock> createOrEmpty() {
		if (blockList.size() < maxNumBlocks) {
			CandidateBlock cb = new CandidateBlock(blockList.size(), 0);
			blockList.add(cb);
			return Optional.of(cb);
		}
		return Optional.empty();
	}

	// return true if there is no TxToBeMined in txToBeMinedList which is going to
	// be mined in a block after blockPosition
	private boolean notAnyTxAfterCandidateBlockPosition(int blockPosition, List<TxToBeMined> txToBeMinedList) {
		for (TxToBeMined txToBeMined : txToBeMinedList) {
			if (txToBeMined.getContainingBlock().getPosition() > blockPosition) {
				return false;
			}
		}
		return true;
	}

	// Returns the list of txs that are in {@value allParentsOfTx} but not are in
	// any CandidateBlock
	private List<Transaction> getNotInAnyCandidateBlockTxListOf(Set<String> allParentsOfTx, TxMemPool txMemPool) {
		return allParentsOfTx.stream().filter(txId -> !contains(txId)).map(txId -> txMemPool.getTx(txId))
				.filter(Optional::isPresent).map(op -> op.get()).collect(Collectors.toList());
	}

	// Returns the list of txs that are in {@value allParentsOfTx} and in any
	// CandidateBlock
	private List<TxToBeMined> getInAnyCandidateBlockTxListOf(Set<String> allParentsOfTx) {
		return allParentsOfTx.stream().map(txId -> getTxToBeMined(txId)).filter(Optional::isPresent).map(op -> op.get())
				.collect(Collectors.toList());
	}

}