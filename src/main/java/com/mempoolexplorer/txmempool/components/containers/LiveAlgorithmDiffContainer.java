package com.mempoolexplorer.txmempool.components.containers;

import java.util.Optional;

import com.mempoolexplorer.txmempool.entites.AlgorithmDiff;

public interface LiveAlgorithmDiffContainer {

	void setLiveAlgorithmDiff(AlgorithmDiff liveAlgorithmDiff);

	Optional<AlgorithmDiff> getliveAlgorithmDiff();

	void drop();
}