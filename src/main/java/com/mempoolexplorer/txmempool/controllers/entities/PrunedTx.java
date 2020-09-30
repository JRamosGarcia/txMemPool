package com.mempoolexplorer.txmempool.controllers.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PrunedTx {
	private String id;
	private int w;
}
