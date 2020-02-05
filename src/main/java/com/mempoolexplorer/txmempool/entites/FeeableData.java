package com.mempoolexplorer.txmempool.entites;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Class that keeps track of max and min satVByte and txIds using
 * getSatvByteIncludingAncestors() Also total fees in satoshis
 */
public class FeeableData {

	private double maxSatVByteIncAnc = Double.MIN_VALUE;
	private double minSatVByteIncAnc = Double.MAX_VALUE;

	private long totalBaseFee = 0;
	private long totalAncestorsFee = 0;

	private Integer numTxs = 0;
	private Integer totalWeight = 0;

	private String maxSatVByteIncAncTxId;
	private String minSatVByteIncAncTxId;

	public FeeableData(Stream<? extends Feeable> fStream) {
		super();
		checkFees(fStream);
	}

	public FeeableData() {
		super();
	}

	public boolean isValid() {
		if ((maxSatVByteIncAnc == Double.MIN_VALUE) || (minSatVByteIncAnc == Double.MAX_VALUE))
			return false;
		return true;
	}

	public void checkFeeable(Feeable feeable) {
		totalBaseFee += feeable.getBaseFees();
		totalAncestorsFee += feeable.getAncestorFees();
		totalWeight += feeable.getWeight();
		numTxs++;

		if ((feeable.getSatvByteIncludingAncestors() == Double.MIN_VALUE)
				|| (feeable.getSatvByteIncludingAncestors() == Double.MAX_VALUE))
			return;
		if (feeable.getSatvByteIncludingAncestors() > maxSatVByteIncAnc) {
			maxSatVByteIncAnc = feeable.getSatvByteIncludingAncestors();
			maxSatVByteIncAncTxId = feeable.getTxId();
		}
		if (feeable.getSatvByteIncludingAncestors() < minSatVByteIncAnc) {
			minSatVByteIncAnc = feeable.getSatvByteIncludingAncestors();
			minSatVByteIncAncTxId = feeable.getTxId();
		}
	}

	public void checkOther(FeeableData other) {
		if ((other.maxSatVByteIncAnc == Double.MIN_VALUE) || (other.minSatVByteIncAnc == Double.MAX_VALUE))
			return;
		checkFees(other.maxSatVByteIncAnc, other.maxSatVByteIncAncTxId);
		checkFees(other.minSatVByteIncAnc, other.minSatVByteIncAncTxId);
		totalBaseFee += other.totalBaseFee;
		totalAncestorsFee += other.totalAncestorsFee;
		totalWeight += other.totalWeight;
		numTxs += other.numTxs;
	}

	// fee and txId must be checked correct before calling this method.
	private void checkFees(double fee, String txId) {

		if (fee > maxSatVByteIncAnc) {
			maxSatVByteIncAnc = fee;
			maxSatVByteIncAncTxId = txId;
		}
		if (fee < minSatVByteIncAnc) {
			minSatVByteIncAnc = fee;
			minSatVByteIncAncTxId = txId;
		}
	}

	public void checkFees(Stream<? extends Feeable> fStream) {
		fStream.forEach(f -> checkFeeable(f));
	}

	public Optional<String> getMaxSatVByteIncAncTxId() {
		if (!isValid()) {
			return Optional.empty();
		}
		return Optional.of(maxSatVByteIncAncTxId);
	}

	public Optional<String> getMinSatVByteIncAncTxId() {
		if (!isValid()) {
			return Optional.empty();
		}
		return Optional.of(minSatVByteIncAncTxId);
	}

	public Optional<Double> getMaxSatVByteIncAnc() {
		if (!isValid()) {
			return Optional.empty();
		}
		return Optional.of(maxSatVByteIncAnc);
	}

	public Optional<Double> getMinSatVByteIncAnc() {
		if (!isValid()) {
			return Optional.empty();
		}
		return Optional.of(minSatVByteIncAnc);
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
		builder.append("FeeableData [");
		if (isValid()) {
			builder.append("totalBaseFee=");
			builder.append(totalBaseFee);
			builder.append(", totalAncestorsFee=");
			builder.append(totalAncestorsFee);
			builder.append(", maxSatVByteIncAnc=");
			builder.append(maxSatVByteIncAnc);
			builder.append(", minSatVByteIncAnc=");
			builder.append(minSatVByteIncAnc);
			builder.append(", maxSatVByteIncAncTxId=");
			builder.append(maxSatVByteIncAncTxId);
			builder.append(", minSatVByteIncAncTxId=");
			builder.append(minSatVByteIncAncTxId);
		} else {
			builder.append("Not A Value");
		}
		builder.append("]");
		return builder.toString();
	}

}
