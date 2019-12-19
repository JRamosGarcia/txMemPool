package com.mempoolexplorer.txmempool.events;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.mempool.TxPoolChanges;

/**
 * This class is an union of Block and TxPoolChanges since is meant to be used
 * as a kafka message for same topic conserving message order. The topic is a
 * "mempool event"
 */
public class MempoolEvent {
	//
	public enum EventType {
		NEW_BLOCK, REFRESH_POOL
	};

	private EventType eventType;

	private Instant changeTime;// Used by TxPoolChanges and Block, set by us

	private Integer changeCounter;// Used by TxPoolChanges
	private List<Transaction> newTxs; // Used by TxPoolChanges
	private List<String> removedTxsId;// Used by TxPoolChanges and Block

	// Used by Block class:
	private String hash;
	private Integer height;
	private Instant minedTime;// This time is set by miners. Can be in the future!
	private Instant medianMinedTime;// This time always increases with respect height

	private MempoolEvent() {
	}

	public static MempoolEvent createFrom(TxPoolChanges txPoolChanges) {
		MempoolEvent mpe = new MempoolEvent();
		mpe.eventType = EventType.REFRESH_POOL;
		mpe.changeTime = txPoolChanges.getChangeTime();
		mpe.changeCounter = txPoolChanges.getChangeCounter();
		mpe.newTxs = txPoolChanges.getNewTxs();
		mpe.removedTxsId = txPoolChanges.getRemovedTxsId();
		return mpe;
	}

	public static MempoolEvent createFrom(Block block) {
		MempoolEvent mpe = new MempoolEvent();
		mpe.eventType = EventType.NEW_BLOCK;
		mpe.changeTime = block.getChangeTime();
		mpe.hash = block.getHash();
		mpe.height = block.getHeight();
		mpe.minedTime = block.getMinedTime();
		mpe.medianMinedTime = block.getMedianMinedTime();
		mpe.removedTxsId = block.getTxs();
		return mpe;
	}

	public Optional<Block> tryConstructBlock() {
		if (this.eventType != null && this.eventType == EventType.NEW_BLOCK) {
			Block block = new Block();
			block.setChangeTime(changeTime);
			block.setHash(hash);
			block.setHeight(height);
			block.setMinedTime(minedTime);
			block.setMedianMinedTime(medianMinedTime);
			block.setTxs(removedTxsId);
			return Optional.of(block);
		} else
			return Optional.empty();
	}

	public Optional<TxPoolChanges> tryConstructTxPoolChanges() {
		if (this.eventType != null && this.eventType == EventType.REFRESH_POOL) {
			TxPoolChanges txpc = new TxPoolChanges();
			txpc.setChangeCounter(changeCounter);
			txpc.setChangeTime(changeTime);
			txpc.setNewTxs(newTxs);
			txpc.setRemovedTxsId(removedTxsId);
			return Optional.of(txpc);
		} else
			return Optional.empty();
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

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

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Instant getMinedTime() {
		return minedTime;
	}

	public void setMinedTime(Instant minedTime) {
		this.minedTime = minedTime;
	}

	public Instant getMedianMinedTime() {
		return medianMinedTime;
	}

	public void setMedianMinedTime(Instant medianMinedTime) {
		this.medianMinedTime = medianMinedTime;
	}

}
