package com.mempoolexplorer.txmempool.components.containers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.mempoolexplorer.txmempool.entites.AlgorithmDiff;

@Component
public class AlgorithmDiffContainerImpl implements AlgorithmDiffContainer {

	private Map<Integer, AlgorithmDiff> heightToAlgoDiffMap = new HashMap<>();
	private AlgorithmDiff last;

	// TODO: it does not get Garbage collected.
	@Override
	public void put(AlgorithmDiff ad) {
		last = ad;
		heightToAlgoDiffMap.put(ad.getBlockHeight(), ad);
	}

	@Override
	public Map<Integer, AlgorithmDiff> getHeightToAlgoDiffMap() {
		return heightToAlgoDiffMap;
	}

	@Override
	public Optional<AlgorithmDiff> getLast() {
		return Optional.ofNullable(last);
	}

}
