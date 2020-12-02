package com.mempoolexplorer.txmempool.events;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.mempool.TxPoolChanges;
import com.mempoolexplorer.txmempool.entites.blocktemplate.BlockTemplate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class is an union of Block, TxPoolChanges and BlockTemplate since is
 * meant to be used as a kafka message for same topic conserving message order.
 * The topic is a "mempool event"
 * 
 * Two kind of Events can be used: NEW_BLOCK and REFRESH_POOL. when NEW_BLOCK is
 * sent, Block, TxPoolChanges and blockTemplate are sent. when REFRESH_POOL is
 * sent, only txPoolChanges is sent.
 */

@Getter
@Setter
@NoArgsConstructor
public class MempoolEvent {
	//
	public enum EventType {
		NEW_BLOCK, REFRESH_POOL
	}

	private int seqNumber;
	private EventType eventType;
	private TxPoolChanges txPoolChanges;
	private Optional<Block> block;
	private Optional<BlockTemplate> blockTemplate;

	/**
	 * Returns blocks transactions in connected or disconnected blocks
	 */
	public Optional<List<String>> tryGetBlockTxIds() {
		if (eventType != null && eventType == EventType.NEW_BLOCK && block.isPresent()) {
			if (Boolean.TRUE.equals(block.get().getConnected())) {
				return Optional.of(txPoolChanges.getRemovedTxsId());
			} else {
				return Optional
						.of(txPoolChanges.getNewTxs().stream().map(Transaction::getTxId).collect(Collectors.toList()));
			}
		}
		return Optional.empty();
	}

}
