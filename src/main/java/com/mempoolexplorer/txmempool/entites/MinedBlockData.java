package com.mempoolexplorer.txmempool.entites;

import java.time.Instant;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.CoinBaseTx;
import com.mempoolexplorer.txmempool.utils.AsciiUtils;

public class MinedBlockData {

	private Instant changeTime;// This time is set by us
	private String hash;
	private Integer height;
	private Integer weight;// up to 4 millions (sum of vSize*4)
	private Instant minedTime;// This time is set by miners. Can be in the future!
	private Instant medianMinedTime;// This time always increases with respect height
	private CoinBaseTx coinBaseTx;// also in txIds but not in notInMemPoolTransactions
	private String coinBaseAsciiField;

	private FeeableData feeableData = new FeeableData();

	public MinedBlockData(Block block, FeeableData feeableData) {
		this.changeTime = block.getChangeTime();
		this.hash = block.getHash();
		this.height = block.getHeight();
		this.weight = block.getWeight();
		this.minedTime = block.getMinedTime();
		this.medianMinedTime = block.getMedianMinedTime();
		this.coinBaseTx = block.getCoinBaseTx();
		this.coinBaseAsciiField = AsciiUtils.hexToAscii(block.getCoinBaseTx().getvInField());
		this.feeableData = feeableData;
	}

	public Instant getChangeTime() {
		return changeTime;
	}

	public String getHash() {
		return hash;
	}

	public Integer getHeight() {
		return height;
	}

	public Integer getWeight() {
		return weight;
	}

	public Instant getMinedTime() {
		return minedTime;
	}

	public Instant getMedianMinedTime() {
		return medianMinedTime;
	}

	public CoinBaseTx getCoinBaseTx() {
		return coinBaseTx;
	}

	public FeeableData getFeeableData() {
		return feeableData;
	}

	public String getCoinBaseAsciiField() {
		return coinBaseAsciiField;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MinedBlockData [changeTime=");
		builder.append(changeTime);
		builder.append(", hash=");
		builder.append(hash);
		builder.append(", height=");
		builder.append(height);
		builder.append(", weight=");
		builder.append(weight);
		builder.append(", minedTime=");
		builder.append(minedTime);
		builder.append(", medianMinedTime=");
		builder.append(medianMinedTime);
		builder.append(", coinBaseTx=");
		builder.append(coinBaseTx);
		builder.append(", coinBaseAsciiField=");
		builder.append(coinBaseAsciiField);
		builder.append(", feeableData=");
		builder.append(feeableData);
		builder.append("]");
		return builder.toString();
	}

}
