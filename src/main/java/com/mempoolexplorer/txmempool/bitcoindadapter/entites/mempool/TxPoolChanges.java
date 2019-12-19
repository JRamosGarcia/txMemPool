package com.mempoolexplorer.txmempool.bitcoindadapter.entites.mempool;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;

public class TxPoolChanges {
	private Instant changeTime;
	private Integer changeCounter;
	private List<Transaction> newTxs = new ArrayList<>();
	private List<String> removedTxsId = new ArrayList<>();

	public Instant getChangeTime() {
		return changeTime;
	}

	public void setChangeTime(Instant changeTime) {
		this.changeTime = changeTime;
	}

	public Integer getChangeCounter() {
		return changeCounter;
	}

	public void setChangeCounter(Integer changeCounter) {
		this.changeCounter = changeCounter;
	}

	public List<Transaction> getNewTxs() {
		return newTxs;
	}

	public void setNewTxs(List<Transaction> newTxs) {
		this.newTxs = newTxs;
	}

	public List<String> getRemovedTxsId() {
		return removedTxsId;
	}

	public void setRemovedTxsId(List<String> removedTxsId) {
		this.removedTxsId = removedTxsId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TxPoolChanges [changeTime=");
		builder.append(changeTime);
		builder.append(", changeCounter=");
		builder.append(changeCounter);
		builder.append(", newTxs=");
		builder.append(newTxs);
		builder.append(", removedTxsId=");
		builder.append(removedTxsId);
		builder.append("]");
		return builder.toString();
	}

}
