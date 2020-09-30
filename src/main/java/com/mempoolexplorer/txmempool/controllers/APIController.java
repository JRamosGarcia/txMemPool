package com.mempoolexplorer.txmempool.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mempoolexplorer.txmempool.components.containers.LiveMiningQueueContainer;
import com.mempoolexplorer.txmempool.controllers.entities.CandidateBlockHistogram;
import com.mempoolexplorer.txmempool.controllers.entities.CompleteLiveMiningQueueGraphData;
import com.mempoolexplorer.txmempool.controllers.entities.PrunedLiveMiningQueueGraphData;
import com.mempoolexplorer.txmempool.controllers.entities.PrunedSatVByteHistogramElement;
import com.mempoolexplorer.txmempool.controllers.entities.SatVByteHistogramElement;
import com.mempoolexplorer.txmempool.controllers.exceptions.ServiceNotReadyYetException;

@RestController
@RequestMapping("/api")
public class APIController {

	@Autowired
	private LiveMiningQueueContainer liveMiningQueueContainer;

	@GetMapping("/candidateBlocks")
	public PrunedLiveMiningQueueGraphData getCandidateBlocks() throws ServiceNotReadyYetException {
		if (liveMiningQueueContainer.atomicGet() == null) {
			throw new ServiceNotReadyYetException();
		}

		CompleteLiveMiningQueueGraphData complete = liveMiningQueueContainer.atomicGet().getLiveMiningQueueGraphData();

		PrunedLiveMiningQueueGraphData pruned = from(complete);

		pruned.setCandidateBlockRecapList(complete.getCandidateBlockRecapList());
		return pruned;
	}

	@GetMapping("/block/{blockIndex}")
	public PrunedLiveMiningQueueGraphData getBlockIndex(@PathVariable("blockIndex") Integer blockIndex)
			throws ServiceNotReadyYetException {
		if (liveMiningQueueContainer.atomicGet() == null) {
			throw new ServiceNotReadyYetException();
		}

		CompleteLiveMiningQueueGraphData complete = liveMiningQueueContainer.atomicGet().getLiveMiningQueueGraphData();
		PrunedLiveMiningQueueGraphData pruned = from(complete);

		// No cache yet
		pruned.setCandidateBlockRecapList(complete.getCandidateBlockRecapList());

		if (blockIndex >= 0 && complete.getCandidateBlockHistogramList().size() > blockIndex) {
			pruned.setPrunedCandidateBlockHistogram(
					from(complete.getCandidateBlockHistogramList().get(blockIndex).getHistogramList()));
			pruned.setSelectedCandidateBlock(blockIndex);
		}

		return pruned;
	}

	@GetMapping("/histogram/{blockIndex}/{satVByte}")
	public PrunedLiveMiningQueueGraphData getHistogram(@PathVariable("blockIndex") Integer blockIndex,
			@PathVariable("satVByte") Integer satVByte) throws ServiceNotReadyYetException {
		if (liveMiningQueueContainer.atomicGet() == null) {
			throw new ServiceNotReadyYetException();
		}

		CompleteLiveMiningQueueGraphData complete = liveMiningQueueContainer.atomicGet().getLiveMiningQueueGraphData();
		PrunedLiveMiningQueueGraphData pruned = from(complete);

		// No cache yet
		pruned.setCandidateBlockRecapList(complete.getCandidateBlockRecapList());

		// No cache yet
		if (blockIndex >= 0 && complete.getCandidateBlockHistogramList().size() > blockIndex) {

			CandidateBlockHistogram candidateBlockHistogram = complete.getCandidateBlockHistogramList().get(blockIndex);

			pruned.setPrunedCandidateBlockHistogram(from(candidateBlockHistogram.getHistogramList()));
			pruned.setSelectedCandidateBlock(blockIndex);

			SatVByteHistogramElement satVByteHistogramElement = candidateBlockHistogram.getHistogramMap().get(satVByte);
			if (satVByteHistogramElement != null) {
				pruned.setSelectedSatVByte(satVByte);
				pruned.setPrunedTxs(satVByteHistogramElement.getPrunedTxs());
			}
		}
		return pruned;
	}

	private List<PrunedSatVByteHistogramElement> from(List<SatVByteHistogramElement> list) {
		return list.stream()
				.map(e -> new PrunedSatVByteHistogramElement(e.getModSatVByte(), e.getNumTxs(), e.getWeight()))
				.collect(Collectors.toList());
	}

	private PrunedLiveMiningQueueGraphData from(CompleteLiveMiningQueueGraphData complete) {
		PrunedLiveMiningQueueGraphData pruned = new PrunedLiveMiningQueueGraphData();
		pruned.setLastModTime(complete.getLastModTime());
		pruned.setNumTxsInMempool(complete.getNumTxsInMempool());
		pruned.setNumTxsInMiningQueue(complete.getNumTxsInMiningQueue());
		pruned.setVSizeInLast10minutes(complete.getVSizeInLast10minutes());

		CandidateBlockHistogram firstCandidateBlockHistogram = complete.getCandidateBlockHistogramList().get(0);
		if (firstCandidateBlockHistogram != null) {
			SatVByteHistogramElement firstSatVByteHistogramElement = firstCandidateBlockHistogram.getHistogramList()
					.get(0);
			pruned.setMaxModSatVByte(firstSatVByteHistogramElement.getModSatVByte());
		}
		return pruned;
	}

}
