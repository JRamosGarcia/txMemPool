package com.mempoolexplorer.txmempool.controllers.entities;

import java.util.ArrayList;
import java.util.List;

import com.mempoolexplorer.txmempool.entites.miningqueue.SatVByte_NumTXs;

public class LiveMiningQueueGraphData {

	// This list can be disordered because a small tx with low satVByte filling gaps
	// of big tx with high satVByte
	private List<SatVByte_NumTXs> satVByteNumTXsList = new ArrayList<>();

	private List<Integer> blockPositionList = new ArrayList<>();

	private int blocksAccurateUpToBlock = 0;

	private int vSizeInLast10minutes;

	public List<SatVByte_NumTXs> getSatVByteNumTXsList() {
		return satVByteNumTXsList;
	}

	public void setSatVByteNumTXsList(List<SatVByte_NumTXs> satVByteNumTXsList) {
		this.satVByteNumTXsList = satVByteNumTXsList;
	}

	public List<Integer> getBlockPositionList() {
		return blockPositionList;
	}

	public void setBlockPositionList(List<Integer> blockPositionList) {
		this.blockPositionList = blockPositionList;
	}

	public int getBlocksAccurateUpToBlock() {
		return blocksAccurateUpToBlock;
	}

	public void setBlocksAccurateUpToBlock(int blocksAccurateUpToBlock) {
		this.blocksAccurateUpToBlock = blocksAccurateUpToBlock;
	}

	public int getvSizeInLast10minutes() {
		return vSizeInLast10minutes;
	}

	public void setvSizeInLast10minutes(int vSizeInLast10minutes) {
		this.vSizeInLast10minutes = vSizeInLast10minutes;
	}

}