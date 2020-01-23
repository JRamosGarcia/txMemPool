package com.mempoolexplorer.txmempool.entites;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Class that keeps track of max and min fee and txIds using getSatvByteIncludingAncestors()
 */
public class MaxMinFeeTransactions {

	private double maxFee = Double.MIN_VALUE;
	private double minFee = Double.MAX_VALUE;

	private String maxFeeTxId;
	private String minFeeTxId;

	public MaxMinFeeTransactions(Stream<? extends Feeable> fStream) {
		super();
		checkFees(fStream);
	}

	public MaxMinFeeTransactions() {
		super();
	}

	public boolean isValid() {
		if ((maxFee == Double.MIN_VALUE) || (minFee == Double.MAX_VALUE))
			return false;
		return true;
	}

	public void checkFees(Feeable feeable) {
		if ((feeable.getSatvByteIncludingAncestors() == Double.MIN_VALUE) || (feeable.getSatvByteIncludingAncestors() == Double.MAX_VALUE))
			return;
		if (feeable.getSatvByteIncludingAncestors() > maxFee) {
			maxFee = feeable.getSatvByteIncludingAncestors();
			maxFeeTxId = feeable.getTxId();
		}
		if (feeable.getSatvByteIncludingAncestors() < minFee) {
			minFee = feeable.getSatvByteIncludingAncestors();
			minFeeTxId = feeable.getTxId();
		}
	}

	public Optional<Feeable> getMaxFeeable() {
		if (!isValid()) {
			return Optional.empty();
		}
		return Optional.of(new Feeable() {
			@Override
			public String getTxId() {
				return maxFeeTxId;
			}

			@Override
			public double getSatvByteIncludingAncestors() {
				return maxFee;
			}
			
			@Override
			public double getSatvByte() {
				return maxFee;
			}
		});
	}

	public Optional<Feeable> getMinFeeable() {
		if (!isValid()) {
			return Optional.empty();
		}
		return Optional.of(new Feeable() {

			@Override
			public String getTxId() {
				return minFeeTxId;
			}

			@Override
			public double getSatvByteIncludingAncestors() {
				return minFee;
			}
			@Override
			public double getSatvByte() {
				return minFee;
			}
		});
	}

	public void checkFees(MaxMinFeeTransactions other) {
		if ((other.maxFee == Double.MIN_VALUE) || (other.minFee == Double.MAX_VALUE))
			return;
		checkFees(other.maxFee, other.maxFeeTxId);
		checkFees(other.minFee, other.minFeeTxId);
	}

	// fee and txId must be checked correct before calling this method.
	private void checkFees(double fee, String txId) {
		if (fee > maxFee) {
			maxFee = fee;
			maxFeeTxId = txId;
		}
		if (fee < minFee) {
			minFee = fee;
			minFeeTxId = txId;
		}
	}

	public void checkFees(Stream<? extends Feeable> fStream) {
		fStream.forEach(f -> checkFees(f));
	}

	public Optional<String> getMaxFeeTxId() {
		if (!isValid()) {
			return Optional.empty();
		}
		return Optional.of(maxFeeTxId);
	}

	public Optional<String> getMinFeeTxId() {
		if (!isValid()) {
			return Optional.empty();
		}
		return Optional.of(minFeeTxId);
	}

	public Optional<Double> getMaxFee() {
		if (!isValid()) {
			return Optional.empty();
		}
		return Optional.of(maxFee);
	}

	public Optional<Double> getMinFee() {
		if (!isValid()) {
			return Optional.empty();
		}
		return Optional.of(minFee);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MaxMinFee [");
		if (isValid()) {
			builder.append("maxFee=");
			builder.append(maxFee);
			builder.append(", minFee=");
			builder.append(minFee);
			builder.append(", maxFeeTxId=");
			builder.append(maxFeeTxId);
			builder.append(", minFeeTxId=");
			builder.append(minFeeTxId);
		} else {
			builder.append("Not A Value");
		}
		builder.append("]");
		return builder.toString();
	}

}
