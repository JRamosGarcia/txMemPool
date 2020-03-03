package com.mempoolexplorer.txmempool.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "txmempool")
@Getter
@Setter
public class TxMempoolProperties {

	private Integer refreshCountToCreateNewMiningQueue = 5;
	private Integer miningQueueNumTxs = 100000;
	private Integer miningQueueMaxNumBlocks = 30;
	private Integer liveMiningQueueMaxTxs = 100000;
	private Integer liveMiningQueueGraphSize = 500;
	private Integer maxLiveDataBufferSize = 100;
	private Integer numTimesTxIgnoredToRaiseAlarm = 3;
	private Integer numTxMinedButNotInMemPoolToRaiseAlarm = 10;
	private Boolean liveAlgorithmDiffsEnabled = false;
	private Boolean persistState = false;

}
