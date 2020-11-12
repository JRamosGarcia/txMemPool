package com.mempoolexplorer.txmempool.components.containers;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import com.mempoolexplorer.txmempool.entites.AlgorithmDiff;

@Component
public class LiveAlgorithmDiffContainerImpl implements LiveAlgorithmDiffContainer {

	private AtomicReference<AlgorithmDiff> liveAlgoDiffRef = new AtomicReference<AlgorithmDiff>();

	@Override
	public Optional<AlgorithmDiff> getliveAlgorithmDiff() {
		return Optional.ofNullable(liveAlgoDiffRef.get());
	}

	@Override
	public void setLiveAlgorithmDiff(AlgorithmDiff liveAlgorithmDiff) {
		liveAlgoDiffRef.set(liveAlgorithmDiff);
	}

	@Override
	public void drop() {
		setLiveAlgorithmDiff(AlgorithmDiff.empty());
	}

}
