package com.mempoolexplorer.txmempool.controllers.entities;

import java.util.ArrayList;
import java.util.List;

import com.mempoolexplorer.txmempool.entites.miningqueue.SatVByte_NumTXsList;

public class LiveMiningQueueGraphData {

	private SatVByte_NumTXsList satVByteNumTXsList = new SatVByte_NumTXsList(0);

	private List<Integer> blockPositionList = new ArrayList<>();

	private int blocksAccurateUpToBlock = 0;
	
	public SatVByte_NumTXsList getSatVByteNumTXsList() {
		return satVByteNumTXsList;
	}

	public void setSatVByteNumTXsList(SatVByte_NumTXsList satVByteNumTXsList) {
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

}