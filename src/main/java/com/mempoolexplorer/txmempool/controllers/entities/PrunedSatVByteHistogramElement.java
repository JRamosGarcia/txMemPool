package com.mempoolexplorer.txmempool.controllers.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PrunedSatVByteHistogramElement {
	private int modSatVByte;
	private int numTxs;
	private int weight;
}
