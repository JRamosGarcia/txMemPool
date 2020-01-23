package com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain;

import com.mempoolexplorer.txmempool.entites.Feeable;

public class NotInMemPoolTx implements Feeable {

	private String txId;
	private Long fees;// in Satoshis. Sadly this does not take into account Ancestors
	private Integer vSize;// Sadly this does not take into account Ancestors

	public NotInMemPoolTx(String txId, Long fees, Integer vSize) {
		super();
		this.txId = txId;
		this.fees = fees;
		this.vSize = vSize;
	}

	@Override
	public String getTxId() {
		return txId;
	}

	// TODO: This two methods should not return the same but we don't have enough
	// information
	@Override
	public double getSatvByteIncludingAncestors() {
		return ((double) fees / ((double) vSize));
	}

	@Override
	public double getSatvByte() {
		return ((double) fees / ((double) vSize));
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NotInMemPoolTx [txId=");
		builder.append(txId);
		builder.append(", fees=");
		builder.append(fees);
		builder.append(", vSize=");
		builder.append(vSize);
		builder.append("]");
		return builder.toString();
	}

}
