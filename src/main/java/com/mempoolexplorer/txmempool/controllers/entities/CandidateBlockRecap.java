package com.mempoolexplorer.txmempool.controllers.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
//@NoArgsConstructor
@ToString
public class CandidateBlockRecap {
	private int weight = 0;
	private long totalFees = 0;
	private int numTxs = 0;
}
