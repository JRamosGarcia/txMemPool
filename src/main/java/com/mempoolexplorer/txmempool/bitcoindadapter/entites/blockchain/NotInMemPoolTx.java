package com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain;

import com.mempoolexplorer.txmempool.entites.Feeable;

public class NotInMemPoolTx implements Feeable {

	private String txId;
	private Long fees;// in Satoshis. Sadly this does not take into account Ancestors
	private Integer weigth;// Sadly this does not take into account Ancestors

	public NotInMemPoolTx(String txId, Long fees, Integer weigth) {
		super();
		this.txId = txId;
		this.fees = fees;
		this.weigth = weigth;
	}

	@Override
	public String getTxId() {
		return txId;
	}

	// TODO: This two methods should not return the same but we don't have enough
	// information
	@Override
	public double getSatvByteIncludingAncestors() {
		return ((double) fees / ((double) (weigth) / 4.0d));
	}

	@Override
	public double getSatvByte() {
		return ((double) fees / ((double) (weigth) / 4.0d));
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

	public Integer getWeigth() {
		return weigth;
	}

	public void setWeigth(Integer weigth) {
		this.weigth = weigth;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NotInMemPoolTx [txId=");
		builder.append(txId);
		builder.append(", fees=");
		builder.append(fees);
		builder.append(", weigth=");
		builder.append(weigth);
		builder.append("]");
		return builder.toString();
	}

}