package com.mempoolexplorer.txmempool.entites;

public class RepudiatingBlockStats {

	private MaxMinFeeTransactions maxMinFee;
	private Integer numTxs;
	private Long totalFees;
	private Integer totalvSize;

	public RepudiatingBlockStats(MaxMinFeeTransactions maxMinFee, Integer numTxs, Long totalFees, Integer totalvSize) {
		super();
		this.maxMinFee = maxMinFee;
		this.numTxs = numTxs;
		this.totalFees = totalFees;
		this.totalvSize = totalvSize;
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

	public Integer getTotalvSize() {
		return totalvSize;
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
		builder.append(", totalvSize=");
		builder.append(totalvSize);
		builder.append("]");
		return builder.toString();
	}

}
