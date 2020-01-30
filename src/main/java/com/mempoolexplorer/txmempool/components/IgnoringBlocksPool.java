package com.mempoolexplorer.txmempool.components;

import java.util.Optional;

import com.mempoolexplorer.txmempool.entites.IgnoringBlock;

public interface IgnoringBlocksPool {

	void add(IgnoringBlock igBlock);

	Optional<IgnoringBlock> getIgnoringBlock(Integer height);

	Optional<IgnoringBlock> getLast();

}
