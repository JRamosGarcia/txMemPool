package com.mempoolexplorer.txmempool.entites;

import com.mempoolexplorer.txmempool.entites.miningqueue.CandidateBlock;

public class CandidateBlockData {
	private int position = 0;// Position of this block in queue
	private int weight = 0;
	private long totalFees = 0;
	private int coinBaseWeight = 0;
	private int precedingTxsCount = 0; // Sum of all txs in preceding blocks

	private FeeableData feeableData = new FeeableData();

	public CandidateBlockData(CandidateBlock candidateBlock, FeeableData feeableData) {
		this.position = candidateBlock.getPosition();
		this.weight = candidateBlock.getWeight();
		this.totalFees = candidateBlock.getTotalFees();
		this.coinBaseWeight = candidateBlock.getCoinBaseWeight();
		this.precedingTxsCount = candidateBlock.getPrecedingTxsCount();
		this.feeableData = feeableData;
	}

	public int getPosition() {
		return position;
	}

	public int getWeight() {
		return weight;
	}

	public long getTotalFees() {
		return totalFees;
	}

	public int getCoinBaseWeight() {
		return coinBaseWeight;
	}

	public int getPrecedingTxsCount() {
		return precedingTxsCount;
	}

	public FeeableData getFeeableData() {
		return feeableData;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CandidateBlockData [position=");
		builder.append(position);
		builder.append(", weight=");
		builder.append(weight);
		builder.append(", totalFees=");
		builder.append(totalFees);
		builder.append(", coinBaseWeight=");
		builder.append(coinBaseWeight);
		builder.append(", precedingTxsCount=");
		builder.append(precedingTxsCount);
		builder.append(", feeableData=");
		builder.append(feeableData);
		builder.append("]");
		return builder.toString();
	}

	
}
