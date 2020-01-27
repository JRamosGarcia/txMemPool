package com.mempoolexplorer.txmempool.entites;

public class RepudiatingBlockStats {

	private MaxMinFeeTransactions maxMinFee;
	private Integer numTxs;
	private Long totalFees;
	private Integer totalWeight;

	public RepudiatingBlockStats(MaxMinFeeTransactions maxMinFee, Integer numTxs, Long totalFees, Integer totalWeight) {
		super();
		this.maxMinFee = maxMinFee;
		this.numTxs = numTxs;
		this.totalFees = totalFees;
		this.totalWeight = totalWeight;
	}

	public MaxMinFeeTransactions getMaxMinFee() {
		return maxMinFee;
	}

	public Integer getNumTxs() {
		return numTxs;
	}

	public Long getTotalFees() {
		return totalFees;
	}

	public Integer getTotalWeight() {
		return totalWeight;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RepudiatingBlockStats [maxMinFee=");
		builder.append(maxMinFee);
		builder.append(", numTxs=");
		builder.append(numTxs);
		builder.append(", totalFees=");
		builder.append(totalFees);
		builder.append(", totalWeight=");
		builder.append(totalWeight);
		builder.append("]");
		return builder.toString();
	}

}
