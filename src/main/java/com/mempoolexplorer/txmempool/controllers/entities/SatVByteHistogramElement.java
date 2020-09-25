package com.mempoolexplorer.txmempool.controllers.entities;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SatVByteHistogramElement {

	private Double modSatVByte;
	private Integer satVByte;
	private Integer numTxs;
	private Integer sumWeight;
	private List<String> txIdList;

}
