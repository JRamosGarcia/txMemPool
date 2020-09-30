package com.mempoolexplorer.txmempool.controllers.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteLiveMiningQueueGraphData {

	private Instant lastModTime;
	
	private int numTxsInMempool;
	
	private int numTxsInMiningQueue;
	
	private int vSizeInLast10minutes;

	List<CandidateBlockRecap> candidateBlockRecapList = new ArrayList<>();

	// This list can be disordered because a small tx with low satVByte filling gaps
	// of big tx with high satVByte, or simply due to CPFP
	List<CandidateBlockHistogram> candidateBlockHistogramList = new ArrayList<>();

}