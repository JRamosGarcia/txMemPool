package com.mempoolexplorer.txmempool.entites.pools;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mempoolexplorer.txmempool.entites.IgnoringBlock;
import com.mempoolexplorer.txmempool.properties.TxMempoolProperties;

public class IgnoringBlocksPoolImpl implements IgnoringBlocksPool {

	private TxMempoolProperties txMempoolProperties;

	private Map<Integer, IgnoringBlock> ignoringBlocksMap = new ConcurrentHashMap<>();
	private IgnoringBlock last;

	public IgnoringBlocksPoolImpl(TxMempoolProperties txMempoolProperties) {
		super();
		this.txMempoolProperties = txMempoolProperties;
	}

	@Override
	public void add(IgnoringBlock igBlock) {
		last = igBlock;
		ignoringBlocksMap.put(igBlock.getMinedBlockData().getHeight(), igBlock);
		// Kind of circular buffer
		ignoringBlocksMap
				.remove(igBlock.getMinedBlockData().getHeight() - txMempoolProperties.getLiveMiningQueueGraphSize());
	}

	@Override
	public Optional<IgnoringBlock> getIgnoringBlock(Integer height) {
		return Optional.ofNullable(ignoringBlocksMap.get(height));
	}

	@Override
	public Optional<IgnoringBlock> getLast() {
		return Optional.ofNullable(last);
	}

	@Override
	public Map<Integer, IgnoringBlock> getIgnoringBlocksMap() {
		return ignoringBlocksMap;
	}

	@Override
	public void drop() {
		ignoringBlocksMap = new ConcurrentHashMap<>();
		last = null;
	}
}
