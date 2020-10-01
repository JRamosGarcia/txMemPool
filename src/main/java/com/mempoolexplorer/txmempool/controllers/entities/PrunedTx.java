package com.mempoolexplorer.txmempool.controllers.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PrunedTx {
	@JsonProperty("i")
	private String txId;
	@JsonProperty("w")
	private int weight;
}
