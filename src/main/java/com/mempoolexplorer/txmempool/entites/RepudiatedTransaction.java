package com.mempoolexplorer.txmempool.entites;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.utils.SysProps;

public class RepudiatedTransaction {

	public enum State {
		INMEMPOOL, MINED, DELETED
	};

	private Transaction tx;

	private List<RepudiatingBlock> repudiatingBlockList = new ArrayList<>();

	private Map<Integer, Integer> positionInBlockHeightMap = new HashMap<>();

	private State state = State.INMEMPOOL;

	private Double totalSatvBytesLost = 0D; // Total Satoshis per byte lost due to repudiation (sum of
											// (Tx.satByte-blockMinSatBytes) for each repudiating block)
	private Long totalFeesLost = 0l;// totalSatvBytesLost*tx.vSize

	private Instant timeWhenShouldHaveBeenMined;// Mining time of the fist block in which the tx should have been mined

	private Integer finallyMinedOnBlock = -1;// Block height on which transaction was finally mined, could be 0 (not
												// mined but deleted)

	public Transaction getTx() {
		return tx;
	}

	public void setTx(Transaction tx) {
		this.tx = tx;
	}

	public List<RepudiatingBlock> getRepudiatingBlockList() {
		return repudiatingBlockList;
	}

	public void setRepudiatingBlockList(List<RepudiatingBlock> repudiatingBlockList) {
		this.repudiatingBlockList = repudiatingBlockList;
	}

	public Map<Integer, Integer> getPositionInBlockHeightMap() {
		return positionInBlockHeightMap;
	}

	public void setPositionInBlockHeightMap(Map<Integer, Integer> positionInBlockHeight) {
		this.positionInBlockHeightMap = positionInBlockHeight;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Double getTotalSatvBytesLost() {
		return totalSatvBytesLost;
	}

	public void setTotalSatvBytesLost(Double totalSatvBytesLost) {
		this.totalSatvBytesLost = totalSatvBytesLost;
	}

	public Long getTotalFeesLost() {
		return totalFeesLost;
	}

	public void setTotalFeesLost(Long totalFeesLost) {
		this.totalFeesLost = totalFeesLost;
	}

	public Instant getTimeWhenShouldHaveBeenMined() {
		return timeWhenShouldHaveBeenMined;
	}

	public void setTimeWhenShouldHaveBeenMined(Instant timeWhenShouldHaveBeenMined) {
		this.timeWhenShouldHaveBeenMined = timeWhenShouldHaveBeenMined;
	}

	public Integer getFinallyMinedOnBlock() {
		return finallyMinedOnBlock;
	}

	public void setFinallyMinedOnBlock(Integer finallyMinedOnBlock) {
		this.finallyMinedOnBlock = finallyMinedOnBlock;
	}

	@Override
	public String toString() {
		String nl = SysProps.NL;
		StringBuilder builder = new StringBuilder();
		builder.append("RepudiatedTransaction [txId=");
		builder.append(tx.getTxId());
		builder.append(nl);
		builder.append("ancestor/descendantCount=(");
		builder.append(tx.getTxAncestry().getAncestorCount());
		builder.append(",");
		builder.append(tx.getTxAncestry().getDescendantCount());
		builder.append(")");
		builder.append(nl);
		builder.append(", repudiatingBlockList=[");

		Iterator<RepudiatingBlock> it = repudiatingBlockList.iterator();
		while (it.hasNext()) {
			RepudiatingBlock rBlock = it.next();
			builder.append("(bh: " + rBlock.getBlockHeight() + ",pos: "
					+ positionInBlockHeightMap.get(rBlock.getBlockHeight()) + ")");
			if (it.hasNext()) {
				builder.append(", ");
			}
		}

		builder.append("]");
		builder.append(nl);
		builder.append(", state=");
		builder.append(state);
		builder.append(", totalSatvBytesLost=");
		builder.append(totalSatvBytesLost);
		builder.append(", totalFeesLost=");
		builder.append(totalFeesLost);
		builder.append(", timeWhenShouldHaveBeenMined=");
		builder.append(timeWhenShouldHaveBeenMined);
		builder.append(", finallyMinedOnBlock=");
		builder.append(finallyMinedOnBlock);
		builder.append("]");
		return builder.toString();
	}

}
