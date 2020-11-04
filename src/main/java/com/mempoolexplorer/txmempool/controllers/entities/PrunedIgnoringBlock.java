package com.mempoolexplorer.txmempool.controllers.entities;

import com.mempoolexplorer.txmempool.entites.CoinBaseData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrunedIgnoringBlock {
	private int height;
	private int txsInMinedBlock;
	private int txsInCandidateBlock;
	private int posInCandidateBlock;
	private long time;
	CoinBaseData coinBaseData;
}
