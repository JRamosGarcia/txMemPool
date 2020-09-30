package com.mempoolexplorer.txmempool.controllers.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrunedLiveMiningQueueGraphData {

	private Instant lastModTime;

	private int numTxsInMempool;

	private int numTxsInMiningQueue;

	private int vSizeInLast10minutes;

	private int maxModSatVByte;// Maximum Modified SatVByte for drawing purpouses

	private List<CandidateBlockRecap> candidateBlockRecapList = new ArrayList<>();

	private int selectedCandidateBlock = -1;

	@JsonProperty("candidateBlockHistogram")
	private List<PrunedSatVByteHistogramElement> prunedCandidateBlockHistogram;

	private int selectedSatVByte = -1;

	@JsonProperty("satVByteHistogramElement")
	private List<PrunedTx> prunedTxs;
}
