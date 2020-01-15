package com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain;

import com.mempoolexplorer.txmempool.entites.Feeable;

public class NotInMemPoolTx implements Feeable {

	private String txId;
	private Long fees;// in Satoshis
	private Integer vSize;
	private Double satvBytes;

	public NotInMemPoolTx(String txId, Long fees, Integer vSize, Double satvBytes) {
		super();
		this.txId = txId;
		this.fees = fees;
		this.vSize = vSize;
		this.satvBytes = satvBytes;
	}

	@Override
	public String getTxId() {
		return txId;
	}

	@Override
	public double getSatvByte() {
		return satvBytes;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public Long getFees() {
		return fees;
	}

	public void setFees(Long fees) {
		this.fees = fees;
	}

	public Integer getvSize() {
		return vSize;
	}

	public void setvSize(Integer vSize) {
		this.vSize = vSize;
	}

	public Double getSatvBytes() {
		return satvBytes;
	}

	public void setSatvBytes(Double satvBytes) {
		this.satvBytes = satvBytes;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NotInMemPoolTx [txId=");
		builder.append(txId);
		builder.append(", fees=");
		builder.append(fees);
		builder.append(", vSize=");
		builder.append(vSize);
		builder.append(", satvBytes=");
		builder.append(satvBytes);
		builder.append("]");
		return builder.toString();
	}

}
