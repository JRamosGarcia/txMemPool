package com.mempoolexplorer.txmempool.events.sinks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.mempool.TxPoolChanges;
import com.mempoolexplorer.txmempool.events.CustomChannels;

@EnableBinding(CustomChannels.class)
public class TxMemPoolChangesHandler {

	private static final Logger logger = LoggerFactory.getLogger(TxMemPoolChangesHandler.class);

	@StreamListener("txMemPoolChanges")
	public void loggerSink(TxPoolChanges txPoolChanges) {
		logger.info(txPoolChanges.toString());
	}
}
