package com.mempoolexplorer.txmempool.components.containers;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.entites.miningqueue.MiningQueue;
import com.mempoolexplorer.txmempool.properties.TxMempoolProperties;

@Component
public class LiveMiningQueueContainerImpl implements LiveMiningQueueContainer {

	@Autowired
	private TxMempoolProperties txMempoolProperties;

	private AtomicReference<MiningQueue> miningQueueRef = new AtomicReference<>(MiningQueue.empty());

	private int numRefreshedWatcher = 0;// Counter for not refreshing miningQueue all the time

	@Override
	public void atomicSet(MiningQueue mq) {
		miningQueueRef.set(mq);
	}

	@Override
	public MiningQueue atomicGet() {
		return miningQueueRef.get();
	}

	@Override
	public void refreshIfNeeded(TxMemPool txMemPool) {
		if (numRefreshedWatcher >= txMempoolProperties.getRefreshCountToCreateNewMiningQueue()) {
			numRefreshedWatcher = 0;
			updateMiningQueue(txMemPool);
		}
		numRefreshedWatcher++;
	}

	@Override
	public void forceRefresh(TxMemPool txMemPool) {
		updateMiningQueue(txMemPool);
		numRefreshedWatcher = 0;
	}

	private void updateMiningQueue(TxMemPool txMemPool) {
		MiningQueue newMiningQueue = MiningQueue.buildFrom(new ArrayList<>(), txMemPool,
				txMempoolProperties.getMiningQueueNumTxs(), txMempoolProperties.getMiningQueueMaxNumBlocks());
		this.miningQueueRef.set(newMiningQueue);
	}

}
