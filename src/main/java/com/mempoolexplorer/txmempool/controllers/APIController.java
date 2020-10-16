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
import com.mempoolexplorer.txmempool.controllers.entities.PrunedTx;
import com.mempoolexplorer.txmempool.controllers.entities.SatVByteHistogramElement;
import com.mempoolexplorer.txmempool.controllers.entities.TxIdAndWeight;
import com.mempoolexplorer.txmempool.controllers.exceptions.ServiceNotReadyYetException;

/**
 * @author tomillo
 *
 *         This api returns a PrunedLiveMiningQueueGraphData in all its methods.
 *         Depending on the method called, the last modification that the client
 *         has, and wheather the client already have or not that item, the
 *         returned class will have more or less properties filled. The general
 *         rules are: - Return no data if the client is updated and already has
 *         the data is asking for. - Return all -dependant- data if client is
 *         not updated, that is, if client is searching for mining Queue then
 *         only mining Queue is return. but if a block is queried and client is
 *         not updated then miningQueue data and block data is returned. Also if
 *         client is querying an histogram and it is not updated then mining
 *         queue and block data containing that histogram is returned.
 */
@RestController
@RequestMapping("/api")
public class APIController {

	@Autowired
	private LiveMiningQueueContainer liveMiningQueueContainer;

	@GetMapping("/miningQueue/{lastModTime}/{clientHaveIt}")
	public PrunedLiveMiningQueueGraphData getMiningQueue(@PathVariable("lastModTime") long clientLastModTime,
			@PathVariable("clientHaveIt") boolean clientHaveIt) throws ServiceNotReadyYetException {
		if (liveMiningQueueContainer.atomicGet() == null) {
			throw new ServiceNotReadyYetException();
		}

		CompleteLiveMiningQueueGraphData complete = liveMiningQueueContainer.atomicGet().getLiveMiningQueueGraphData();
		PrunedLiveMiningQueueGraphData pruned = new PrunedLiveMiningQueueGraphData();
		addCommonData(complete, pruned);

		if (complete.getLastModTime() > clientLastModTime || !clientHaveIt) {
			// An update has happened
			addMiningQueueDataToPruned(pruned, complete);
		}
		return pruned;
	}

	@GetMapping("/block/{blockIndex}/{lastModTime}/{clientHaveIt}")
	public PrunedLiveMiningQueueGraphData getBlockIndex(@PathVariable("blockIndex") Integer blockIndex,
			@PathVariable("lastModTime") long clientLastModTime, @PathVariable("clientHaveIt") boolean clientHaveIt)
			throws ServiceNotReadyYetException {
		if (liveMiningQueueContainer.atomicGet() == null) {
			throw new ServiceNotReadyYetException();
		}

		CompleteLiveMiningQueueGraphData complete = liveMiningQueueContainer.atomicGet().getLiveMiningQueueGraphData();
		PrunedLiveMiningQueueGraphData pruned = new PrunedLiveMiningQueueGraphData();
		addCommonData(complete, pruned);

		if (!clientHaveIt) {
			// Searching for block data that client does not have
			addBlockDataToPruned(blockIndex, complete, pruned);
			if (complete.getLastModTime() > clientLastModTime) {
				// An update has happened, adds rest of data
				addMiningQueueDataToPruned(pruned, complete);
			}
		} else if (complete.getLastModTime() > clientLastModTime) {
			// Clients search data that already have but could be updated (and it did)
			addMiningQueueDataToPruned(pruned, complete);
			addBlockDataToPruned(blockIndex, complete, pruned);
		}
		return pruned;
	}

	@GetMapping("/histogram/{blockIndex}/{satVByte}/{lastModTime}/{clientHaveIt}")
	public PrunedLiveMiningQueueGraphData getHistogram(@PathVariable("blockIndex") Integer blockIndex,
			@PathVariable("satVByte") Integer satVByte, @PathVariable("lastModTime") long clientLastModTime,
			@PathVariable("clientHaveIt") boolean clientHaveIt) throws ServiceNotReadyYetException {
		if (liveMiningQueueContainer.atomicGet() == null) {
			throw new ServiceNotReadyYetException();
		}

		CompleteLiveMiningQueueGraphData complete = liveMiningQueueContainer.atomicGet().getLiveMiningQueueGraphData();
		PrunedLiveMiningQueueGraphData pruned = new PrunedLiveMiningQueueGraphData();
		addCommonData(complete, pruned);

		if (!clientHaveIt) {
			// Searching for histogram data that client does not have
			addHistogramDataToPruned(blockIndex, satVByte, complete, pruned);
			if (complete.getLastModTime() > clientLastModTime) {
				// An update has happened, adds rest of data
				addMiningQueueDataToPruned(pruned, complete);
				addBlockDataToPruned(blockIndex, complete, pruned);
			}
		} else if (complete.getLastModTime() > clientLastModTime) {
			// Clients search data that already have but could be updated (and it did)
			addMiningQueueDataToPruned(pruned, complete);
			addBlockDataToPruned(blockIndex, complete, pruned);
			addHistogramDataToPruned(blockIndex, satVByte, complete, pruned);
		}
		return pruned;
	}

	@GetMapping("/tx/{blockIndex}/{satVByte}/{txIndex}/{lastModTime}/{clientHaveIt}")
	public PrunedLiveMiningQueueGraphData getTxByIndex(@PathVariable("blockIndex") Integer blockIndex,
			@PathVariable("satVByte") Integer satVByte, @PathVariable("txIndex") int txIndex,
			@PathVariable("lastModTime") long clientLastModTime, @PathVariable("clientHaveIt") boolean clientHaveIt)
			throws ServiceNotReadyYetException {

		CompleteLiveMiningQueueGraphData complete = liveMiningQueueContainer.atomicGet().getLiveMiningQueueGraphData();
		PrunedLiveMiningQueueGraphData pruned = new PrunedLiveMiningQueueGraphData();
		addCommonData(complete, pruned);

		if (!clientHaveIt) {
			// Searching for histogram data that client does not have
			addTxByIndexToPruned(blockIndex, satVByte, txIndex, complete, pruned);
			if (complete.getLastModTime() > clientLastModTime) {
				// An update has happened, adds rest of data
				addHistogramDataToPruned(blockIndex, satVByte, complete, pruned);
				addMiningQueueDataToPruned(pruned, complete);
				addBlockDataToPruned(blockIndex, complete, pruned);
			}
		} else if (complete.getLastModTime() > clientLastModTime) {
			// Clients search data that already have but could be updated (and it did)
			addMiningQueueDataToPruned(pruned, complete);
			addBlockDataToPruned(blockIndex, complete, pruned);
			addHistogramDataToPruned(blockIndex, satVByte, complete, pruned);
			addTxByIndexToPruned(blockIndex, satVByte, txIndex, complete, pruned);
		}
		return pruned;

	}

	private void addCommonData(CompleteLiveMiningQueueGraphData complete, PrunedLiveMiningQueueGraphData pruned) {
		pruned.setLastModTime(complete.getLastModTime());
		pruned.setWeightInLast10minutes(complete.getWeightInLast10minutes());
	}

	private void addMiningQueueDataToPruned(PrunedLiveMiningQueueGraphData pruned,
			CompleteLiveMiningQueueGraphData complete) {
		pruned.setCandidateBlockRecapList(complete.getCandidateBlockRecapList());
	}

	private void addBlockDataToPruned(Integer blockIndex, CompleteLiveMiningQueueGraphData complete,
			PrunedLiveMiningQueueGraphData pruned) {
		if (blockIndex >= 0 && complete.getCandidateBlockHistogramList().size() > blockIndex) {
			pruned.setPrunedCandidateBlockHistogram(
					from(complete.getCandidateBlockHistogramList().get(blockIndex).getHistogramList()));
			pruned.setBlockSelected(blockIndex);
		}
	}

	private void addHistogramDataToPruned(Integer blockIndex, Integer satVByte,
			CompleteLiveMiningQueueGraphData complete, PrunedLiveMiningQueueGraphData pruned) {
		if (blockIndex >= 0 && complete.getCandidateBlockHistogramList().size() > blockIndex) {
			CandidateBlockHistogram candidateBlockHistogram = complete.getCandidateBlockHistogramList().get(blockIndex);
			SatVByteHistogramElement satVByteHistogramElement = candidateBlockHistogram.getHistogramMap().get(satVByte);
			if (satVByteHistogramElement != null) {
				pruned.setSatVByteSelected(satVByte);
				pruned.setPrunedTxs(prunedFrom(satVByteHistogramElement.getTxIdAndWeightList()));
			}
		}
	}

	private void addTxByIndexToPruned(Integer blockIndex, Integer satVByte, int txIndex,
			CompleteLiveMiningQueueGraphData complete, PrunedLiveMiningQueueGraphData pruned) {
		if (blockIndex >= 0 && complete.getCandidateBlockHistogramList().size() > blockIndex) {
			CandidateBlockHistogram candidateBlockHistogram = complete.getCandidateBlockHistogramList().get(blockIndex);
			SatVByteHistogramElement satVByteHistogramElement = candidateBlockHistogram.getHistogramMap().get(satVByte);
			if (satVByteHistogramElement != null) {
				List<TxIdAndWeight> txIdAndWeightList = satVByteHistogramElement.getTxIdAndWeightList();
				if (txIdAndWeightList.size() > txIndex) {
					pruned.setTxIdSelected(txIdAndWeightList.get(txIndex).getTxId());
					pruned.setTxIndexSelected(txIndex);
				}
			}
		}

	}

	private List<PrunedTx> prunedFrom(List<TxIdAndWeight> list) {
		return list.stream().map(e -> new PrunedTx(e.getWeight())).collect(Collectors.toList());
	}

	private List<PrunedSatVByteHistogramElement> from(List<SatVByteHistogramElement> list) {
		return list.stream()
				.map(e -> new PrunedSatVByteHistogramElement(e.getModSatVByte(), e.getNumTxs(), e.getWeight()))
				.collect(Collectors.toList());
	}

}
