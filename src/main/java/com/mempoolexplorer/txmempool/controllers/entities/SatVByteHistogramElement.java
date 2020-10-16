package com.mempoolexplorer.txmempool.controllers.entities;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SatVByteHistogramElement {

	private int modSatVByte;
	private int numTxs;
	private int weight;
	private List<TxIdAndWeight> txIdAndWeightList;

}
