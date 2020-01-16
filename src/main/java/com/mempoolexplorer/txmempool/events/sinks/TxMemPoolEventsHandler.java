package com.mempoolexplorer.txmempool.events.sinks;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.ApplicationListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.event.ListenerContainerIdleEvent;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.NotInMemPoolTx;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.mempool.TxPoolChanges;
import com.mempoolexplorer.txmempool.components.RepudiatedTransactionPool;
import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.entites.MisMinedTransactions;
import com.mempoolexplorer.txmempool.events.CustomChannels;
import com.mempoolexplorer.txmempool.events.MempoolEvent;
import com.mempoolexplorer.txmempool.feingintefaces.BitcoindAdapter;
import com.mempoolexplorer.txmempool.utils.AsciiUtils;
import com.mempoolexplorer.txmempool.utils.SysProps;

@EnableBinding(CustomChannels.class)
public class TxMemPoolEventsHandler implements Runnable, ApplicationListener<ListenerContainerIdleEvent> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TxMemPool txMemPool;

	@Autowired
	private TaskExecutor taskExecutor;

	@Autowired
	private BitcoindAdapter bitcoindAdapter;
	
	@Autowired
	private RepudiatedTransactionPool repudiatedTransactionPool;

	@Value("${spring.cloud.stream.bindings.txMemPoolEvents.destination}")
	private String topic;

	private AtomicBoolean doResume = new AtomicBoolean(false);

	private int numConsecutiveBlocks = 0;// Number of consecutive Blocks before a refresh is made.

	private AtomicBoolean initializing = new AtomicBoolean(true);

	private AtomicBoolean loadingFullMempool = new AtomicBoolean(false);

	@StreamListener("txMemPoolEvents")
	public void blockSink(MempoolEvent mempoolEvent, @Header(KafkaHeaders.CONSUMER) Consumer<?, ?> consumer) {
		if ((mempoolEvent.getEventType() == MempoolEvent.EventType.NEW_BLOCK) && (!initializing.get())) {
			Block block = mempoolEvent.tryConstructBlock().get();
			logger.info("New Block with {} transactions", block.getTxIds().size());
			OnNewBlock(block, numConsecutiveBlocks++);
		} else if (mempoolEvent.getEventType() == MempoolEvent.EventType.REFRESH_POOL) {
			// OnRefreshPool
			TxPoolChanges txpc = mempoolEvent.tryConstructTxPoolChanges().get();
			// When initializing but bitcoindAdapter is not intitializing
			if ((initializing.get()) && (txpc.getChangeCounter() != 0) && (!loadingFullMempool.get())) {
				// We pause incoming messages, but several messages has been taken from kafka at
				// once so this method will be called several times. Refresh the mempool only if
				// not initializing
				logger.info("txMemPool is starting but bitcoindAdapter started long ago... "
						+ "pausing receiving kafka messages and loading full mempool from REST interface");
				consumer.pause(Collections.singleton(new TopicPartition(topic, 0)));
				loadingFullMempool.set(true);
				// Load full mempool asyncronous via REST service, then resume kafka msgs
				doFullLoadAsync();// Method must return ASAP, this is a kafka queue.
			} else if (!loadingFullMempool.get()) {// This is because consumer.pause does not pause inmediately
				// Refresh the mempool
				txMemPool.refresh(txpc);
				initializing.set(false);
			}
			numConsecutiveBlocks = 0;
		}
	}

	private void OnNewBlock(Block block, int numConsecutiveBlocks) {
		MisMinedTransactions misMinedTransactions = txMemPool.calculateMisMinedTransactions(block,
				numConsecutiveBlocks);
		logger.info(misMinedTransactions.toString());
		//TODO: Buen sitio para poner una alarma!
		logger.info("Equals: {}", checkNotInMemPoolTxs(block, misMinedTransactions));
		logger.info(strLogBlockNotInMemPoolData(block));
		repudiatedTransactionPool.refresh(block, misMinedTransactions, txMemPool.getTxIdSet());
	}

	private String strLogBlockNotInMemPoolData(Block block) {
		String nl = SysProps.NL;
		StringBuilder sb = new StringBuilder();
		sb.append("CoinbaseTxId: " + block.getCoinBaseTx().getTxId());
		sb.append(nl);
		sb.append("CoinbaseField: " + block.getCoinBaseTx().getvInField());
		sb.append(nl);
		sb.append("CoinbasevSize: " + block.getCoinBaseTx().getSizeInvBytes());
		sb.append(nl);
		
		sb.append("Ascci: " + AsciiUtils.hexToAscii(block.getCoinBaseTx().getvInField()));
		sb.append(nl);
		sb.append("block.notInmempool: [");

		Iterator<NotInMemPoolTx> it = block.getNotInMemPoolTransactions().values().iterator();
		while (it.hasNext()) {
			NotInMemPoolTx tx = it.next();
			sb.append(nl);
			sb.append(tx.toString());
		}
		sb.append(nl);
		sb.append("]");
		return sb.toString();
	}

	private boolean checkNotInMemPoolTxs(Block block, MisMinedTransactions misMinedTransactions) {
		Set<String> blockSet = new HashSet<String>();
		blockSet.addAll(block.getNotInMemPoolTransactions().keySet());
		blockSet.add(block.getCoinBaseTx().getTxId());
		Set<String> mmSet = misMinedTransactions.getMinedButNotInMemPool();
		if (blockSet.size() != mmSet.size()) {
			return false;
		}
		if (!blockSet.stream().filter(txId -> !mmSet.contains(txId)).collect(Collectors.toList()).isEmpty()
				|| !mmSet.stream().filter(txId -> !blockSet.contains(txId)).collect(Collectors.toList()).isEmpty()) {
			return false;
		}
		return true;
	}

	private void doFullLoadAsync() {
		taskExecutor.execute(this);
	}

	// Kafka consumer is not thread-safe so we must call pause and resume in the
	// calling thread.
	@Override
	public void onApplicationEvent(ListenerContainerIdleEvent event) {
		// logger.info(event.toString());
		if (doResume.get()) {
			if (event.getConsumer().paused().size() > 0) {
				event.getConsumer().resume(event.getConsumer().paused());
			}
			doResume.set(false);
		}
	}

	@Override
	public void run() {
		try {
			Map<String, Transaction> fullMemPoolMap = bitcoindAdapter.getFullMemPool();
			TxPoolChanges txpc = new TxPoolChanges();
			txpc.setChangeCounter(0);// Force reset
			txpc.setChangeTime(Instant.now());
			txpc.setNewTxs(new ArrayList<>(fullMemPoolMap.values()));
			txMemPool.refresh(txpc);
		} catch (Exception e) {
			logger.error(e.toString());
			// TODO: This is a good place for a system-wide alarm.
		} finally {
			// If loading via REST has failed, we continue and wait for mempool convergence
			// in the long run
			initializing.set(false);
			loadingFullMempool.set(false);
			doResume.set(true);
		}
	}

}
