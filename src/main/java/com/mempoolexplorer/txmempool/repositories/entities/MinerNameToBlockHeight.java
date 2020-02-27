package com.mempoolexplorer.txmempool.repositories.entities;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "minerToHeight")
public class MinerNameToBlockHeight {

	@Id
	private MinerToBlock minerToBlock;

	@Indexed
	private Instant getMedianMinedTime;

	public MinerNameToBlockHeight() {
	}

	public MinerNameToBlockHeight(String minerName, Integer blockHeight, Instant getMedianMinedTime) {
		this(new MinerToBlock(minerName, blockHeight), getMedianMinedTime);
	}

	public MinerNameToBlockHeight(MinerToBlock minerToBlock, Instant getMedianMinedTime) {
		super();
		this.minerToBlock = minerToBlock;
		this.getMedianMinedTime = getMedianMinedTime;
	}

	public MinerToBlock getMinerToBlock() {
		return minerToBlock;
	}

	public void setMinerToBlock(MinerToBlock minerToBlock) {
		this.minerToBlock = minerToBlock;
	}

	public Instant getGetMedianMinedTime() {
		return getMedianMinedTime;
	}

	public void setGetMedianMinedTime(Instant getMedianMinedTime) {
		this.getMedianMinedTime = getMedianMinedTime;
	}

}
