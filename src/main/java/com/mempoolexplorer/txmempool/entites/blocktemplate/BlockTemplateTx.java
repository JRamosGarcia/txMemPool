package com.mempoolexplorer.txmempool.entites.blocktemplate;

public class BlockTemplateTx {

	private String txId;
	private long fee;
	private int sigops;
	private int weight;

	public BlockTemplateTx() {
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public long getFee() {
		return fee;
	}

	public void setFee(long fee) {
		this.fee = fee;
	}

	public int getSigops() {
		return sigops;
	}

	public void setSigops(int sigops) {
		this.sigops = sigops;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BlockTemplateTx [txId=");
		builder.append(txId);
		builder.append(", fee=");
		builder.append(fee);
		builder.append(", sigops=");
		builder.append(sigops);
		builder.append(", weight=");
		builder.append(weight);
		builder.append("]");
		return builder.toString();
	}

}
