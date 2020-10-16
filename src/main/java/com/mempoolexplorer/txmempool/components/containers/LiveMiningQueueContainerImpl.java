package com.mempoolexplorer.txmempool.components.containers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.components.alarms.AlarmLogger;
import com.mempoolexplorer.txmempool.controllers.entities.CandidateBlockHistogram;
import com.mempoolexplorer.txmempool.controllers.entities.CandidateBlockRecap;
import com.mempoolexplorer.txmempool.controllers.entities.CompleteLiveMiningQueueGraphData;
import com.mempoolexplorer.txmempool.controllers.entities.TxIdAndWeight;
import com.mempoolexplorer.txmempool.controllers.entities.SatVByteHistogramElement;
import com.mempoolexplorer.txmempool.entites.miningqueue.CandidateBlock;
import com.mempoolexplorer.txmempool.entites.miningqueue.LiveMiningQueue;
import com.mempoolexplorer.txmempool.entites.miningqueue.MiningQueue;
import com.mempoolexplorer.txmempool.properties.TxMempoolProperties;

@Component
public class LiveMiningQueueContainerImpl implements LiveMiningQueueContainer {

	@Autowired
	private TxMempoolProperties txMempoolProperties;

	@Autowired
	private TxMemPool txMemPool;

	@Autowired
	private AlarmLogger alarmLogger;

	private AtomicReference<LiveMiningQueue> liveMiningQueueRef = new AtomicReference<>();

	private int numRefreshedWatcher = Integer.MAX_VALUE;// Counter for not refreshing miningQueue all the time, first
														// time we refresh

	@Override
	public LiveMiningQueue atomicGet() {
		return liveMiningQueueRef.get();
	}

	@Override
	public Optional<MiningQueue> refreshIfNeeded() {
		if (numRefreshedWatcher >= txMempoolProperties.getRefreshCountToCreateNewMiningQueue()) {
			numRefreshedWatcher = 0;
			return Optional.of(updateLiveMiningQueue());
		}
		numRefreshedWatcher++;
		return Optional.empty();
	}

	@Override
	public MiningQueue forceRefresh() {
		numRefreshedWatcher = 0;
		return updateLiveMiningQueue();
	}

	// Create LiveMiningQueue. All Blocks are taken from MiningQueue which are
	// accurate (CPFP and block space left by big txs are taken into account). and
	// remaining blocks not in mining queue (not calculated) are not shown.
	private MiningQueue updateLiveMiningQueue() {
		MiningQueue newMiningQueue = MiningQueue.buildFrom(new ArrayList<>(), txMemPool,
				txMempoolProperties.getMiningQueueNumTxs(), txMempoolProperties.getMiningQueueMaxNumBlocks());
		if (newMiningQueue.isHadErrors()) {
			alarmLogger.addAlarm("Mining Queue had errors, in updateLiveMiningQueue");
		}
		this.liveMiningQueueRef
				.set(new LiveMiningQueue(buildLiveMiningQueueGraphDataFrom(newMiningQueue), newMiningQueue));
		return newMiningQueue;
	}

	private CompleteLiveMiningQueueGraphData buildLiveMiningQueueGraphDataFrom(MiningQueue mq) {
		CompleteLiveMiningQueueGraphData lmq = new CompleteLiveMiningQueueGraphData();
		lmq.setLastModTime(Instant.now().toEpochMilli());
		lmq.setNumTxsInMempool(txMemPool.getTxNumber());
		lmq.setNumTxsInMiningQueue(mq.getNumTxs());
		lmq.setCandidateBlockRecapList(createCandidateBlockRecapList(mq));
		lmq.setCandidateBlockHistogramList(createCandidateBlockHistogramList(mq));
		lmq.setWeightInLast10minutes(calculateWeightInLast10minutes());
		return lmq;
	}

	private List<CandidateBlockRecap> createCandidateBlockRecapList(MiningQueue mq) {
		List<CandidateBlockRecap> cbrList = new ArrayList<>();
		IntStream.range(0, mq.getNumCandidateBlocks()).mapToObj(i -> mq.getCandidateBlock(i)).map(ocb -> ocb.get())
				.forEach(cb -> {
					CandidateBlockRecap cbr = new CandidateBlockRecap(cb.getWeight(), cb.getTotalFees(),
							cb.getNumTxs());
					cbrList.add(cbr);
				});
		return cbrList;
	}

	private List<CandidateBlockHistogram> createCandidateBlockHistogramList(MiningQueue mq) {
		List<CandidateBlockHistogram> cbhList = new ArrayList<>();
		IntStream.range(0, mq.getNumCandidateBlocks()).mapToObj(i -> mq.getCandidateBlock(i)).map(ocb -> ocb.get())
				.forEach(cb -> {
					CandidateBlockHistogram cbh = createHistogramFor(cb);
					cbhList.add(cbh);
				});
		return cbhList;
	}

	private CandidateBlockHistogram createHistogramFor(CandidateBlock cb) {
		CandidateBlockHistogram cbh = new CandidateBlockHistogram();

		cb.getOrderedStream().forEach(txtbm -> {
			int weight = txtbm.getTx().getWeight();
			addTx(txtbm.getTxId(), txtbm.getModifiedSatVByte(), weight, cbh);
		});

		return cbh;
	}

	private int calculateWeightInLast10minutes() {
		int totalWeight = txMemPool.getTxsAfter(Instant.now().minusSeconds(TimeUnit.MINUTES.toSeconds(10)))
				.mapToInt(tx -> tx.getWeight()).sum();
		return Math.round(totalWeight);
	}

	private void addTx(String txId, double modSatVByte, int weight, CandidateBlockHistogram cbh) {

		SatVByteHistogramElement histogram = cbh.getHistogramMap().get((int) modSatVByte);
		if (histogram == null) {
			addNewPair(txId, modSatVByte, weight, cbh);
		} else {
			histogram.setNumTxs(histogram.getNumTxs() + 1);
			histogram.setWeight(histogram.getWeight() + weight);
			histogram.getTxIdAndWeightList().add(new TxIdAndWeight(txId, weight));
		}
	}

	private void addNewPair(String txId, double modSatVByte, int weight, CandidateBlockHistogram cbh) {
		List<TxIdAndWeight> prunedTxs = new ArrayList<>();
		prunedTxs.add(new TxIdAndWeight(txId, weight));
		SatVByteHistogramElement pair = new SatVByteHistogramElement((int) modSatVByte, 1, weight, prunedTxs);
		cbh.getHistogramMap().put((int) modSatVByte, pair);
		cbh.getHistogramList().add(pair);
	}
}
