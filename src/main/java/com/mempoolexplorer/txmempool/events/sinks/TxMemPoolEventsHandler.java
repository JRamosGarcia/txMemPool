package com.mempoolexplorer.txmempool.events.sinks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.mempool.TxPoolChanges;
import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.entites.MisMinedTransactions;
import com.mempoolexplorer.txmempool.events.CustomChannels;
import com.mempoolexplorer.txmempool.events.MempoolEvent;

@EnableBinding(CustomChannels.class)
public class TxMemPoolEventsHandler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TxMemPool txMemPool;

	private int numConsecutiveBlocks = 0;// Number of consecutive Blocks before a refresh is made.

	@StreamListener("txMemPoolEvents")
	public void blockSink(MempoolEvent mempoolEvent) {
		if (mempoolEvent.getEventType() == MempoolEvent.EventType.NEW_BLOCK) {
			Block block = mempoolEvent.tryConstructBlock().get();
			logger.info("New Block with {} transactions", block.getTxs().size());
			OnNewBlock(block, numConsecutiveBlocks);

			numConsecutiveBlocks++;

		} else if (mempoolEvent.getEventType() == MempoolEvent.EventType.REFRESH_POOL) {
			TxPoolChanges txpc = mempoolEvent.tryConstructTxPoolChanges().get();
			OnRefresh(txpc);

			numConsecutiveBlocks = 0;
		}
	}

	private void OnRefresh(TxPoolChanges txPoolChanges) {
		// Refresh the mempool
		txMemPool.refresh(txPoolChanges);
	}

	private void OnNewBlock(Block block, int numConsecutiveBlocks) {
		MisMinedTransactions misMinedTransactions = txMemPool.calculateMisMinedTransactions(block,
				numConsecutiveBlocks);
		logger.info(misMinedTransactions.toString());
	}
}
