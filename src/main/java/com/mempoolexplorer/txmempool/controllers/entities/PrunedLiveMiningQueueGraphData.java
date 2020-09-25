package com.mempoolexplorer.txmempool.controllers.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrunedLiveMiningQueueGraphData {

	private int numTxs;

	private int vSizeInLast10minutes;

	private List<CandidateBlockRecap> candidateBlockRecapList = new ArrayList<>();

	private int selectedCandidateBlock;

	private CandidateBlockHistogram candidateBlockHistogram;

	private int selectedCandidateBlockHistogram;

	private SatVByteHistogramElement SatVByteHistogramElement;
}
