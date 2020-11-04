package com.mempoolexplorer.txmempool.controllers.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mempoolexplorer.txmempool.entites.IgnoredTransaction;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TxIgnoredData {

	private List<PrunedIgnoringBlock> ignoringBlocks = new ArrayList<>();
	private double totalSVByteLost;
	private long totalFeesLost;

	public static TxIgnoredData from(IgnoredTransaction ignoredTransaction) {
		TxIgnoredData txIgData = new TxIgnoredData();
		txIgData.setTotalFeesLost(ignoredTransaction.getTotalFeesLost());
		txIgData.setTotalSVByteLost(ignoredTransaction.getTotalSatvBytesLost());
		List<PrunedIgnoringBlock> ignoringBlocks = ignoredTransaction.getIgnoringBlockList().stream().map(ib -> {
			PrunedIgnoringBlock pib = new PrunedIgnoringBlock();
			pib.setHeight(ib.getMinedBlockData().getHeight());
			pib.setTxsInMinedBlock(ib.getMinedBlockData().getFeeableData().getNumTxs().orElse(0));
			pib.setTxsInCandidateBlock(ib.getCandidateBlockData().getNumTxs());
			pib.setPosInCandidateBlock(
					ignoredTransaction.getPositionInBlockHeightMap().get(ib.getMinedBlockData().getHeight()).orElse(0));
			pib.setTime(ib.getMinedBlockData().getChangeTime().toEpochMilli());
			pib.setCoinBaseData(ib.getMinedBlockData().getCoinBaseData());
			return pib;
		}).collect(Collectors.toList());
		txIgData.setIgnoringBlocks(ignoringBlocks);
		return txIgData;
	}

}
