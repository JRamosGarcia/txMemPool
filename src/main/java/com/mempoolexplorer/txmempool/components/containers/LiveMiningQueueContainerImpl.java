package com.mempoolexplorer.txmempool.components.containers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.controllers.entities.LiveMiningQueueGraphData;
import com.mempoolexplorer.txmempool.entites.miningqueue.LiveMiningQueue;
import com.mempoolexplorer.txmempool.entites.miningqueue.MiningQueue;
import com.mempoolexplorer.txmempool.entites.miningqueue.QueuedBlock;
import com.mempoolexplorer.txmempool.entites.miningqueue.SatVByte_NumTXsList;
import com.mempoolexplorer.txmempool.entites.miningqueue.TxToBeMined;
import com.mempoolexplorer.txmempool.properties.TxMempoolProperties;
import com.mempoolexplorer.txmempool.utils.SysProps;

@Component
public class LiveMiningQueueContainerImpl implements LiveMiningQueueContainer {

	@Autowired
	private TxMempoolProperties txMempoolProperties;

	@Autowired
	private TxMemPool txMemPool;

	private AtomicReference<LiveMiningQueue> liveMiningQueueRef = new AtomicReference<>();

	private int numRefreshedWatcher = 0;// Counter for not refreshing miningQueue all the time

	@Override
	public LiveMiningQueue atomicGet() {
		return liveMiningQueueRef.get();
	}

	@Override
	public void refreshIfNeeded() {
		if (numRefreshedWatcher >= txMempoolProperties.getRefreshCountToCreateNewMiningQueue()) {
			numRefreshedWatcher = 0;
			updateLiveMiningQueue();
		}
		numRefreshedWatcher++;
	}

	@Override
	public void forceRefresh() {
		updateLiveMiningQueue();
		numRefreshedWatcher = 0;
	}

	// Create LiveMiningQueue. Firsts Blocks are taken from MiningQueue which are
	// accurate (CPFP and block space left by big txs are taken into account). and
	// remaining blocks are inaccurate since are calculated from a txMempool
	// descending list(but faster)
	private void updateLiveMiningQueue() {
		MiningQueue newMiningQueue = MiningQueue.buildFrom(new ArrayList<>(), txMemPool,
				txMempoolProperties.getMiningQueueNumTxs(), txMempoolProperties.getMiningQueueMaxNumBlocks());

		this.liveMiningQueueRef
				.set(new LiveMiningQueue(buildLiveMiningQueueGraphDataFrom(newMiningQueue), newMiningQueue));
	}

	private LiveMiningQueueGraphData buildLiveMiningQueueGraphDataFrom(MiningQueue mq) {
		LiveMiningQueueGraphData lmq = new LiveMiningQueueGraphData();
		lmq.setSatVByteNumTXsList(createSatVByteNumTXsList(mq));
		lmq.setBlockPositionList(createBlockPositionList(mq));
		lmq.setBlocksAccurateUpToBlock(lmq.getBlockPositionList().size());
		addMemPoolTxsTo(lmq, mq);
		return lmq;
	}

	// Adds mempool transactions to LiveMiningQueue but not having into account CPFP
	// or remaining size left by big transactions (Quicker)
	private void addMemPoolTxsTo(LiveMiningQueueGraphData lmq, MiningQueue mq) {
		Optional<TxToBeMined> lastTxInMQ = mq.getLastTx();
		if (lastTxInMQ.isEmpty()) {
			return;
		}
		String lastTxIdInMQ = lastTxInMQ.get().getTx().getTxId();

		// Most complex iterator ever. note the call of satVByteNumTXsList.addTx while
		// iterating. Iterator has side effects!
		Iterator<Transaction> txIt = txMemPool.getDescendingTxStreamFrom(lastTxIdInMQ)
				.limit(txMempoolProperties.getLiveMiningQueueMaxTxs())
				// pass if tx has no children or not in mining queue
				.filter(tx -> tx.getTxAncestry().getSpentby().isEmpty() || (!mq.contains(tx.getTxId())))
				.takeWhile(tx -> lmq.getSatVByteNumTXsList().addTx((int) tx.getSatvByteIncludingAncestors()))
				.iterator();

		List<Integer> blockPositionList = lmq.getBlockPositionList();

		int blockWeight = 0;
		int txPosition = 0;
		if (!blockPositionList.isEmpty()) {
			txPosition = blockPositionList.get(blockPositionList.size() - 1);
		}

		while (txIt.hasNext()) {
			Transaction tx = txIt.next();
			int nextBlockSize = blockWeight + tx.getWeight();
			if (nextBlockSize > SysProps.MAX_BLOCK_WEIGHT) {
				blockPositionList.add(txPosition++);
				blockWeight = tx.getWeight();
			} else {
				txPosition++;
				blockWeight += tx.getWeight();
			}
		}

	}

	private List<Integer> createBlockPositionList(MiningQueue mq) {
		List<Integer> blockPositionList = new ArrayList<>();
		int lastBlockPos = 0;
		for (int i = 0; i < mq.getNumQueuedBlocks(); i++) {
			QueuedBlock queuedBlock = mq.getQueuedBlock(i).get();
			lastBlockPos += queuedBlock.numTxs();
			blockPositionList.add(lastBlockPos);
		}
		return blockPositionList;
	}

	private SatVByte_NumTXsList createSatVByteNumTXsList(MiningQueue mq) {

		SatVByte_NumTXsList satVByteNumTXsList = new SatVByte_NumTXsList(
				txMempoolProperties.getLiveMiningQueueMaxSatByteListSize());
		IntStream.range(0, mq.getNumQueuedBlocks()).mapToObj(i -> mq.getQueuedBlock(i)).map(oqb -> oqb.get())
				.flatMap(qb -> qb.getOrderedStream())
				.takeWhile(txtbm -> satVByteNumTXsList.addTx((int) txtbm.getTx().getSatvByteIncludingAncestors()))
				.forEach(txtbm -> {
				});
		return satVByteNumTXsList;
	}

}
