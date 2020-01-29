package com.mempoolexplorer.txmempool.entites.miningqueue;

public class SatVByte_NumTXs {

	private Integer satVByte;
	private Integer numTxs;

	public SatVByte_NumTXs(Integer satVByte, Integer numTxs) {
		super();
		this.satVByte = satVByte;
		this.numTxs = numTxs;
	}

	public Integer getSatVByte() {
		return satVByte;
	}

	public void setSatVByte(Integer satVByte) {
		this.satVByte = satVByte;
	}

	public Integer getNumTxs() {
		return numTxs;
	}

	public void setNumTxs(Integer numTxs) {
		this.numTxs = numTxs;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SatVByteNumTXs [satVByte=");
		builder.append(satVByte);
		builder.append(", numTxs=");
		builder.append(numTxs);
		builder.append("]");
		return builder.toString();
	}

}
