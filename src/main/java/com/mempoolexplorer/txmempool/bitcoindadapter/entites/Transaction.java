package com.mempoolexplorer.txmempool.bitcoindadapter.entites;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mempoolexplorer.txmempool.entites.Feeable;

public class Transaction implements Feeable {
	private String txId;
	private List<TxInput> txInputs = new ArrayList<>();
	private List<TxOutput> txOutputs = new ArrayList<>();
	private Integer size;// In bytes
	private Integer vSize;// In bytes
	// BE CAREFUL: THIS FIELD MUST KEPT UPDATED, COULD CHANGE ONCE RECEIVED!!!!
	private Fees fees;
	private Long timeInSecs;// Epoch time in seconds since the transaction entered.
	// BE CAREFUL: THIS FIELD MUST KEPT UPDATED, COULD CHANGE ONCE RECEIVED!!!!
	private TxAncestry txAncestry;
	private Boolean bip125Replaceable;
	private String hex;// Raw transaction in hexadecimal
	// RPC

	/**
	 * Returns all addresses involved in this transaction, address in inputs,
	 * outputs and duplicated.
	 * 
	 */
	public List<String> listAddresses() {
		List<String> txInputsAddresses = txInputs.stream().map(txInput -> txInput.getAddressIds())
				.flatMap(addresses -> addresses.stream()).collect(Collectors.toList());
		return txOutputs.stream().map(txOutput -> txOutput.getAddressIds()).flatMap(addresses -> addresses.stream())
				.collect(Collectors.toCollection(() -> txInputsAddresses));
	}

	@Override
	public String getTxId() {
		return txId;
	}

	@Override
	public double getSatvByteIncludingAncestors() {
		if (txAncestry.getAncestorSize() == 0)
			return 0;
		return ((double) fees.getAncestor()) / ((double) txAncestry.getAncestorSize());// getAncestorSize returns vSize.
																						// No worries.
	}

	@Override
	public double getSatvByte() {
		if (vSize == 0)
			return 0;
		return ((double) fees.getBase()) / ((double) vSize);
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public List<TxInput> getTxInputs() {
		return txInputs;
	}

	public void setTxInputs(List<TxInput> txInputs) {
		this.txInputs = txInputs;
	}

	public List<TxOutput> getTxOutputs() {
		return txOutputs;
	}

	public void setTxOutputs(List<TxOutput> txOutputs) {
		this.txOutputs = txOutputs;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Integer getvSize() {
		return vSize;
	}

	public void setvSize(Integer vSize) {
		this.vSize = vSize;
	}

	public Fees getFees() {
		return fees;
	}

	public void setFees(Fees fees) {
		this.fees = fees;
	}

	public Long getTimeInSecs() {
		return timeInSecs;
	}

	public void setTimeInSecs(Long timeInSecs) {
		this.timeInSecs = timeInSecs;
	}

	public TxAncestry getTxAncestry() {
		return txAncestry;
	}

	public void setTxAncestry(TxAncestry txAncestry) {
		this.txAncestry = txAncestry;
	}

	public Boolean getBip125Replaceable() {
		return bip125Replaceable;
	}

	public void setBip125Replaceable(Boolean bip125Replaceable) {
		this.bip125Replaceable = bip125Replaceable;
	}

	public String getHex() {
		return hex;
	}

	public void setHex(String hex) {
		this.hex = hex;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Transaction [txId=");
		builder.append(txId);
		builder.append(", txInputs=");
		builder.append(txInputs);
		builder.append(", txOutputs=");
		builder.append(txOutputs);
		builder.append(", size=");
		builder.append(size);
		builder.append(", vSize=");
		builder.append(vSize);
		builder.append(", fees=");
		builder.append(fees);
		builder.append(", timeInSecs=");
		builder.append(timeInSecs);
		builder.append(", txAncestry=");
		builder.append(txAncestry);
		builder.append(", bip125Replaceable=");
		builder.append(bip125Replaceable);
		builder.append(", hex=");
		builder.append(hex);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return txId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		if (txId == null) {
			if (other.txId != null)
				return false;
		} else if (!txId.equals(other.txId))
			return false;
		return true;
	}
}
