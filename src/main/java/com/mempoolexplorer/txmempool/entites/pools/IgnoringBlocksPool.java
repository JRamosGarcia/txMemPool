package com.mempoolexplorer.txmempool.entites.pools;

import java.util.Map;
import java.util.Optional;

import com.mempoolexplorer.txmempool.entites.IgnoringBlock;

public interface IgnoringBlocksPool {

	void add(IgnoringBlock igBlock);

	Optional<IgnoringBlock> getIgnoringBlock(Integer height);

	Optional<IgnoringBlock> getLast();

	Map<Integer, IgnoringBlock> getIgnoringBlocksMap();

}
