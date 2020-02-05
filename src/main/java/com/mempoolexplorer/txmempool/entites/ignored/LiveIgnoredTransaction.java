package com.mempoolexplorer.txmempool.entites.ignored;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.mempoolexplorer.txmempool.entites.IgnoredTransaction;
import com.mempoolexplorer.txmempool.entites.IgnoredTxState;
import com.mempoolexplorer.txmempool.entites.IgnoringBlock;

public class LiveIgnoredTransaction {

	private String txId;

	private List<SmallIgnoringBlock> smallIgnoringBlockList = new ArrayList<>();

	private Integer numTimesIgnored;
	
	private IgnoredTxState state = IgnoredTxState.INMEMPOOL;

	private Double totalSatvBytesLost = 0D; // Total Satoshis per byte lost due to ignoration (sum of
											// (Tx.satByte-blockMinSatBytes) for each ignoring block)
	private Long totalFeesLost = 0l;// totalSatvBytesLost*tx.vSize

	private Instant timeWhenShouldHaveBeenMined;// Mining time of the fist block in which the tx should have been mined

	private Integer finallyMinedOnBlock = -1;// Block height on which transaction was finally mined, could be 0 (not
												// mined but deleted)

	public LiveIgnoredTransaction() {

	}

	public static LiveIgnoredTransaction from(IgnoredTransaction igTx) {
		var liTx = new LiveIgnoredTransaction();

		liTx.setTxId(igTx.getTx().getTxId());
		var smallIBList = new ArrayList<SmallIgnoringBlock>();
		for (IgnoringBlock ib : igTx.getIgnoringBlockList()) {
			var sib = new SmallIgnoringBlock();
			sib.setHeight(ib.getMinedBlockData().getHeight());
			sib.setPostitionInQueue(igTx.getPositionInBlockHeightMap().get(ib.getMinedBlockData().getHeight()));
			smallIBList.add(sib);
		}
		liTx.setSmallIgnoringBlockList(smallIBList);
		liTx.setNumTimesIgnored(smallIBList.size());
		liTx.setState(igTx.getState());
		liTx.setTotalSatvBytesLost(igTx.getTotalSatvBytesLost());
		liTx.setTotalFeesLost(igTx.getTotalFeesLost());
		liTx.setTimeWhenShouldHaveBeenMined(igTx.getTimeWhenShouldHaveBeenMined());
		liTx.setFinallyMinedOnBlock(igTx.getFinallyMinedOnBlock());

		return liTx;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public List<SmallIgnoringBlock> getSmallIgnoringBlockList() {
		return smallIgnoringBlockList;
	}

	public void setSmallIgnoringBlockList(List<SmallIgnoringBlock> smallIgnoringBlockList) {
		this.smallIgnoringBlockList = smallIgnoringBlockList;
	}

	public Integer getNumTimesIgnored() {
		return numTimesIgnored;
	}




	public void setNumTimesIgnored(Integer numTimesIgnored) {
		this.numTimesIgnored = numTimesIgnored;
	}




	public IgnoredTxState getState() {
		return state;
	}

	public void setState(IgnoredTxState state) {
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

}
