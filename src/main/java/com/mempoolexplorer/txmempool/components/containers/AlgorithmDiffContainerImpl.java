package com.mempoolexplorer.txmempool.components.containers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.mempoolexplorer.txmempool.entites.AlgorithmDifferences;

@Component
public class AlgorithmDiffContainerImpl implements AlgorithmDiffContainer {

	private Map<Integer, AlgorithmDifferences> heightToAlgoDiffMap = new HashMap<>();
	private AlgorithmDifferences last;

	// TODO: it does not get Garbage collected.
	@Override
	public void put(AlgorithmDifferences ad) {
		last = ad;
		heightToAlgoDiffMap.put(ad.getBlockHeight(), ad);
	}

	@Override
	public Map<Integer, AlgorithmDifferences> getHeightToAlgoDiffMap() {
		return heightToAlgoDiffMap;
	}

	@Override 
	public Optional<AlgorithmDifferences> getLast(){
		return Optional.ofNullable(last);
	}

}
