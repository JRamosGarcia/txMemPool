package com.mempoolexplorer.txmempool.components;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.txmempool.entites.IgnoringBlock;
import com.mempoolexplorer.txmempool.properties.TxMempoolProperties;

@Component
public class IgnoringBlocksPoolImpl implements IgnoringBlocksPool {

	@Autowired
	private TxMempoolProperties txMempoolProperties;

	private Map<Integer, IgnoringBlock> ignoringBlocksMap = new ConcurrentHashMap<>();
	private IgnoringBlock last;

	@Override
	public void add(IgnoringBlock igBlock) {
		last = igBlock;
		ignoringBlocksMap.put(igBlock.getBlockHeight(), igBlock);
		// Kind of circular buffer
		ignoringBlocksMap.remove(igBlock.getBlockHeight() - txMempoolProperties.getLiveMiningQueueMaxSatByteListSize());
	}

	@Override
	public Optional<IgnoringBlock> getIgnoringBlock(Integer height) {
		return Optional.ofNullable(ignoringBlocksMap.get(height));
	}
	
	@Override
	public Optional<IgnoringBlock> getLast() {
		return Optional.ofNullable(last);
	}
}
