package com.mempoolexplorer.txmempool.entites.miningqueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 * Childrens of an already inserted parent tx must have different sat/vByte
 * INCLUDING TX'S ANCESTORS since some of them are already included. We use the
 * class ModifiedMempool to store that fees and weigth reduction since tx in
 * mempool must not be mutated.
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

	private ModifiedMempool modifiedMempool = new ModifiedMempool();

	private TxMemPool txMemPool;

	private MiningQueue() {
	}

	public static MiningQueue buildFrom(List<Integer> coinBaseTxWeightList, TxMemPool txMemPool,
			Integer maxTransactionsNumber, Integer maxNumBlocks) {
		logger.info("Creating new MiningQueue...");
		MiningQueue mq = new MiningQueue();
		mq.txMemPool = txMemPool;
		mq.maxNumBlocks = Math.max(coinBaseTxWeightList.size(), maxNumBlocks);
		for (int index = 0; index < coinBaseTxWeightList.size(); index++) {
			mq.blockList.add(new CandidateBlock(index, coinBaseTxWeightList.get(index)));
		}

		txMemPool.getDescendingTxStream().limit(maxTransactionsNumber).forEach(tx -> {
			mq.addTx(tx);
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

	public Stream<TxToBeMined> getGlobalTxStream() {
		return globalTxsMap.values().stream();
	}
	
	public static MiningQueue empty() {
		MiningQueue mq = new MiningQueue();
		return mq;
	}

	public int getNumTxs() {
		return globalTxsMap.size();
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

	// tx comes ordered in descending Sat/vByte including ancestors
	private void addTx(Transaction tx) {

		if (modifiedMempool.contains(tx.getTxId())) {
			// This tx is in modifiedMempool with fees and weigh updated.
			return;
		}
		Optional<ModifiedTx> bestThan = modifiedMempool.getBestThan(tx);
		while (bestThan.isPresent()) {
			addTxWithParents(bestThan.get().getTx(), bestThan.get().getRealAncestorSatVByte());
			modifiedMempool.remove(bestThan.get().getTx().getTxId());

			if (modifiedMempool.contains(tx.getTxId())) {
				// This tx is in modifiedMempool with fees and weigh updated.
				// Not sure if this code is recheable but feels safer.
				return;
			}

			bestThan = modifiedMempool.getBestThan(tx);
		}
		addTxWithParents(tx, tx.getSatvByteIncludingAncestors());
	}

	private void addTxWithParents(Transaction tx, double realSatVByte) {

		if (contains(tx.getTxId())) {
			// This tx is another's parent that has been yet included in a block. Ignore it
			return;
		}

		Set<String> allParentsOfTx = txMemPool.getAllParentsOf(tx);// Excluding itself
		Set<String> childrenSet = txMemPool.getAllChildrenOf(tx);// Excluding itself

		List<Transaction> notInAnyBlockParents = getNotInAnyCandidateBlockTxListOf(allParentsOfTx);
		List<Transaction> notInAnyBlockChildrens = getNotInAnyCandidateBlockTxListOf(childrenSet);

		int notInAnyBlockParentsSumWeight = notInAnyBlockParents.stream().mapToInt(trx -> trx.getWeight()).sum();
		long notInAnyBlockParentsSumFee = notInAnyBlockParents.stream().mapToLong(trx -> trx.getBaseFees()).sum();

		int txEffectiveWeightInCurrentBlock = tx.getWeight() + notInAnyBlockParentsSumWeight;

		Optional<CandidateBlock> blockToFill = getCandidateBlockToFill(txEffectiveWeightInCurrentBlock, allParentsOfTx);

		if (blockToFill.isPresent()) {
			notInAnyBlockParents.stream().forEach(trx -> {
				TxToBeMined txToBeMined = blockToFill.get().addTx(trx, Optional.of(tx),
						optionalList(notInAnyBlockChildrens), realSatVByte);
				globalTxsMap.put(trx.getTxId(), txToBeMined);

			});
			TxToBeMined txToBeMined = blockToFill.get().addTx(tx, Optional.empty(),
					optionalList(notInAnyBlockChildrens), realSatVByte);
			globalTxsMap.put(tx.getTxId(), txToBeMined);

			// Only if tx is really added
			modifiedMempool.substractParentDataToChildren(notInAnyBlockChildrens,
					tx.getBaseFees() + notInAnyBlockParentsSumFee, tx.getWeight() + notInAnyBlockParentsSumWeight);
		}
	}

	private Optional<List<Transaction>> optionalList(List<Transaction> txList) {
		if (txList == null || txList.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(txList);
	}

	private Optional<CandidateBlock> getCandidateBlockToFill(int effectiveWeight, Set<String> allParentsOfTx) {
		List<TxToBeMined> parentTxsAlreadyInABlockList = getInAnyCandidateBlockTxListOf(allParentsOfTx);
		Iterator<CandidateBlock> it = blockList.iterator();
		while (it.hasNext()) {
			CandidateBlock block = it.next();
			if (block.getFreeSpace() >= effectiveWeight) {
				// We cannot put a Tx if any of its parents is mined in a block after this one
				if (notAnyTxAfterCandidateBlockIndex(block.getIndex(), parentTxsAlreadyInABlockList)) {
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
	private boolean notAnyTxAfterCandidateBlockIndex(int blockIndex, List<TxToBeMined> parentTxsAlreadyInABlockList) {
		for (TxToBeMined txToBeMined : parentTxsAlreadyInABlockList) {
			if (txToBeMined.getContainingBlock().getIndex() > blockIndex) {
				return false;
			}
		}
		return true;
	}

	// Returns the list of txs that are in {@value txIdSet} but not are in
	// any CandidateBlock
	private List<Transaction> getNotInAnyCandidateBlockTxListOf(Set<String> txIdSet) {
		return txIdSet.stream().filter(txId -> !contains(txId)).map(txId -> txMemPool.getTx(txId))
				.flatMap(Optional::stream).collect(Collectors.toList());
	}

	// Returns the list of txs that are in {@value allParentsOfTx} and in any
	// CandidateBlock
	private List<TxToBeMined> getInAnyCandidateBlockTxListOf(Set<String> allParentsOfTx) {
		return allParentsOfTx.stream().map(txId -> getTxToBeMined(txId)).flatMap(Optional::stream)
				.collect(Collectors.toList());
	}

}
