package com.mempoolexplorer.txmempool.entites;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Class that keeps track of max and min satVByte and txIds using
 * getSatvByteIncludingAncestors() Also total fees in satoshis
 */
public class MaxMinFeeTransactions {

	private double maxSatVByte = Double.MIN_VALUE;
	private double minSatVByte = Double.MAX_VALUE;

	private long totalBaseFee = 0;
	private long totalAncestorsFee = 0;

	private String maxSatVByteTxId;
	private String minSatVByteTxId;

	public MaxMinFeeTransactions(Stream<? extends Feeable> fStream) {
		super();
		checkFees(fStream);
	}

	public MaxMinFeeTransactions() {
		super();
	}

	public boolean isValid() {
		if ((maxSatVByte == Double.MIN_VALUE) || (minSatVByte == Double.MAX_VALUE))
			return false;
		return true;
	}

	public void checkFees(Feeable feeable) {
		totalBaseFee += feeable.getBaseFees();
		totalAncestorsFee += feeable.getAncestorFees();

		if ((feeable.getSatvByteIncludingAncestors() == Double.MIN_VALUE)
				|| (feeable.getSatvByteIncludingAncestors() == Double.MAX_VALUE))
			return;
		if (feeable.getSatvByteIncludingAncestors() > maxSatVByte) {
			maxSatVByte = feeable.getSatvByteIncludingAncestors();
			maxSatVByteTxId = feeable.getTxId();
		}
		if (feeable.getSatvByteIncludingAncestors() < minSatVByte) {
			minSatVByte = feeable.getSatvByteIncludingAncestors();
			minSatVByteTxId = feeable.getTxId();
		}
	}

	public void checkFees(MaxMinFeeTransactions other) {
		if ((other.maxSatVByte == Double.MIN_VALUE) || (other.minSatVByte == Double.MAX_VALUE))
			return;
		checkFees(other.maxSatVByte, other.maxSatVByteTxId);
		checkFees(other.minSatVByte, other.minSatVByteTxId);
		totalBaseFee += other.totalBaseFee;
		totalAncestorsFee += other.totalAncestorsFee;

	}

	// fee and txId must be checked correct before calling this method.
	private void checkFees(double fee, String txId) {

		if (fee > maxSatVByte) {
			maxSatVByte = fee;
			maxSatVByteTxId = txId;
		}
		if (fee < minSatVByte) {
			minSatVByte = fee;
			minSatVByteTxId = txId;
		}
	}

	public void checkFees(Stream<? extends Feeable> fStream) {
		fStream.forEach(f -> checkFees(f));
	}

	public Optional<String> getMaxSatVByteTxId() {
		if (!isValid()) {
			return Optional.empty();
		}
		return Optional.of(maxSatVByteTxId);
	}

	public Optional<String> getMinSatVByteTxId() {
		if (!isValid()) {
			return Optional.empty();
		}
		return Optional.of(minSatVByteTxId);
	}

	public Optional<Double> getMaxSatVByte() {
		if (!isValid()) {
			return Optional.empty();
		}
		return Optional.of(maxSatVByte);
	}

	public Optional<Double> getMinSatVByte() {
		if (!isValid()) {
			return Optional.empty();
		}
		return Optional.of(minSatVByte);
	}

	public Optional<Long> getTotalBaseFee() {
		if (!isValid()) {
			return Optional.empty();
		}
		return Optional.of(totalBaseFee);
	}

	public Optional<Long> getTotalAncestorsFee() {
		if (!isValid()) {
			return Optional.empty();
		}
		return Optional.of(totalAncestorsFee);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MaxMinFee [");
		if (isValid()) {
			builder.append("totalBaseFee=");
			builder.append(totalBaseFee);
			builder.append(", totalAncestorsFee=");
			builder.append(totalAncestorsFee);
			builder.append(", maxSatVByte=");
			builder.append(maxSatVByte);
			builder.append(", minSatVByte=");
			builder.append(minSatVByte);
			builder.append(", maxSatVByteTxId=");
			builder.append(maxSatVByteTxId);
			builder.append(", minSatVByteTxId=");
			builder.append(minSatVByteTxId);
		} else {
			builder.append("Not A Value");
		}
		builder.append("]");
		return builder.toString();
	}

}
