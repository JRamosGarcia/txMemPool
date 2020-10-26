package com.mempoolexplorer.txmempool.controllers.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TxNode {

	private String txId;
	private int weight;// Segwit
	private long baseFee;// In sats
	private long timeInSecs;// Since entered in mempool
	private boolean bip125Replaceable;
	private int containingBlockIndex;
	private double modifiedSatVByte;
	
}
