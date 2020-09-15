package com.mempoolexplorer.txmempool.controllers.entities;

import java.util.ArrayList;
import java.util.List;

public class LiveMiningQueueGraphData {

	// This list can be disordered because a small tx with low satVByte filling gaps
	// of big tx with high satVByte
	private List<SatVByteHistogramElement> satVByteHistogram = new ArrayList<>();

	private List<Integer> blockPositionList = new ArrayList<>();

	private int numAccurateBlocks = 0;

	private int vSizeInLast10minutes;

	public List<SatVByteHistogramElement> getSatVByteHistogram() {
		return satVByteHistogram;
	}

	public void setSatVByteHistogram(List<SatVByteHistogramElement> satVByteHistogram) {
		this.satVByteHistogram = satVByteHistogram;
	}

	public List<Integer> getBlockPositionList() {
		return blockPositionList;
	}

	public void setBlockPositionList(List<Integer> blockPositionList) {
		this.blockPositionList = blockPositionList;
	}

	public int getNumAccurateBlocks() {
		return numAccurateBlocks;
	}

	public void setNumAccurateBlocks(int numAccurateBlocks) {
		this.numAccurateBlocks = numAccurateBlocks;
	}

	public int getvSizeInLast10minutes() {
		return vSizeInLast10minutes;
	}

	public void setvSizeInLast10minutes(int vSizeInLast10minutes) {
		this.vSizeInLast10minutes = vSizeInLast10minutes;
	}

}