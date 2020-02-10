package com.mempoolexplorer.txmempool.components.containers;

import java.util.Map;
import java.util.Optional;

import com.mempoolexplorer.txmempool.entites.AlgorithmDifferences;

public interface AlgorithmDiffContainer {

	void put(AlgorithmDifferences ad);

	Map<Integer, AlgorithmDifferences> getHeightToAlgoDiffMap();

	Optional<AlgorithmDifferences> getLast();

}