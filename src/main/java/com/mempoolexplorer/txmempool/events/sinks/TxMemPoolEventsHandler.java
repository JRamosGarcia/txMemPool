package com.mempoolexplorer.txmempool.events.sinks;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.Validate;
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

import com.mempoolexplorer.txmempool.TxMemPoolApplication;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.mempool.TxPoolChanges;
import com.mempoolexplorer.txmempool.components.IgnoredTransactionsPool;
import com.mempoolexplorer.txmempool.components.MisMinedTransactionsChecker;
import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.components.alarms.AlarmLogger;
import com.mempoolexplorer.txmempool.components.containers.LiveMiningQueueContainer;
import com.mempoolexplorer.txmempool.entites.MisMinedTransactions;
import com.mempoolexplorer.txmempool.entites.miningqueue.CandidateBlock;
import com.mempoolexplorer.txmempool.entites.miningqueue.MiningQueue;
import com.mempoolexplorer.txmempool.events.CustomChannels;
import com.mempoolexplorer.txmempool.events.MempoolEvent;
import com.mempoolexplorer.txmempool.feingintefaces.BitcoindAdapter;
import com.mempoolexplorer.txmempool.properties.TxMempoolProperties;

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
	private IgnoredTransactionsPool ignoredTransactionPool;

	@Autowired
	private LiveMiningQueueContainer liveMiningQueueContainer;

	@Autowired
	private TxMempoolProperties txMempoolProperties;

	@Autowired
	private AlarmLogger alarmLogger;

	@Autowired
	private MisMinedTransactionsChecker misMinedTransactionsChecker;

	@Value("${spring.cloud.stream.bindings.txMemPoolEvents.destination}")
	private String topic;

	private AtomicBoolean doResume = new AtomicBoolean(false);

	private int numConsecutiveBlocks = 0;// Number of consecutive Blocks before a refresh is made.

	private AtomicBoolean initializing = new AtomicBoolean(true);

	private AtomicBoolean loadingFullMempool = new AtomicBoolean(false);

	private boolean updateFullTxMemPool = true;

	private boolean forceMiningQueueRefresh = false;

	private List<Integer> coinBaseTxWeightList = new ArrayList<>();

	@StreamListener("txMemPoolEvents")
	public void blockSink(MempoolEvent mempoolEvent, @Header(KafkaHeaders.CONSUMER) Consumer<?, ?> consumer) {
		try {
			if ((mempoolEvent.getEventType() == MempoolEvent.EventType.NEW_BLOCK) && (!initializing.get())) {
				forceMiningQueueRefresh = true;
				Block block = mempoolEvent.tryConstructBlock().get();
				logger.info("New Block with {} transactions", block.getTxIds().size());
				OnNewBlock(block, numConsecutiveBlocks++);
				alarmLogger.prettyPrint();
			} else if (mempoolEvent.getEventType() == MempoolEvent.EventType.REFRESH_POOL) {
				// OnRefreshPool
				TxPoolChanges txpc = mempoolEvent.tryConstructTxPoolChanges().get();
				validate(txpc);
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
					refreshMemPoolAndLiveMiningQueue(txpc);
					initializing.set(false);
				}
				numConsecutiveBlocks = 0;
			}
		} catch (Exception e) {
			logger.error("Exception: ", e);
			alarmLogger.addAlarm("Exception in @StreamListener of txMemPoolEvents" + e.toString());
		}
	}

	private void validate(TxPoolChanges txpc) {
		txpc.getNewTxs().stream().forEach(tx -> validateTx(tx));
	}

	private void validateTx(Transaction tx) {
		Validate.notNull(tx.getTxId(), "txId can't be null");
		Validate.notNull(tx.getTxInputs(), "txInputs can't be null");
		Validate.notNull(tx.getTxOutputs(), "txOutputs can't be null");
		Validate.notNull(tx.getWeight(), "weight can't be null");
		Validate.notNull(tx.getFees(), "Fees object can't be null");
		Validate.notNull(tx.getFees().getBase(), "Fees.base can't be null");
		Validate.notNull(tx.getFees().getModified(), "Fees.modified can't be null");
		Validate.notNull(tx.getFees().getAncestor(), "Fees.ancestor can't be null");
		Validate.notNull(tx.getFees().getDescendant(), "Fees.descendant can't be null");
		Validate.notNull(tx.getTimeInSecs(), "timeInSecs can't be null");
		Validate.notNull(tx.getTxAncestry(), "txAncestry can't be null");
		Validate.notNull(tx.getTxAncestry().getDescendantCount(), "descendantCount can't be null");
		Validate.notNull(tx.getTxAncestry().getDescendantSize(), "descendantSize can't be null");
		Validate.notNull(tx.getTxAncestry().getAncestorCount(), "ancestorCount can't be null");
		Validate.notNull(tx.getTxAncestry().getAncestorSize(), "ancestorSize can't be null");
		Validate.notNull(tx.getTxAncestry().getDepends(), "depends can't be null");
		Validate.notNull(tx.getBip125Replaceable(), "bip125Replaceable can't be null");
		Validate.notEmpty(tx.getHex(), "Hex can't be empty");

		tx.getTxInputs().forEach(input -> {
			if (input.getCoinbase() == null) {
				Validate.notNull(input.getTxId(), "input.txId can't be null");
				Validate.notNull(input.getvOutIndex(), "input.voutIndex can't be null");
				Validate.notNull(input.getAmount(), "input.amount can't be null");
				// Input address could be null in case of unrecognized input scripts
				// Validate.notNull(input.getAddressIds());
			}
		});

		tx.getTxOutputs().forEach(output -> {
			// addressIds can be null if script is not recognized.
			Validate.notNull(output.getAmount(), "amount can't be null in a TxOutput");
			Validate.notNull(output.getIndex(), "index can't be null in a TxOutput");
		});

	}

	private void refreshMemPoolAndLiveMiningQueue(TxPoolChanges txpc) {
		// Order of this operations matters.
		refreshMempool(txpc);
		if (txpc.getChangeCounter() != 0) {
			liveMiningQueueContainer.refreshIfNeeded();
		}
		if (forceMiningQueueRefresh) {
			logger.info("LiveMiningQueue refresh forced.");
			liveMiningQueueContainer.forceRefresh();
			forceMiningQueueRefresh = false;
		}
		coinBaseTxWeightList.clear();// If we have new txPoolChanges, we reset coinBaseVSizeList
	}

	public void refreshMempool(TxPoolChanges txPoolChanges) {
		if (txPoolChanges.getChangeCounter() == 0) {
			if (updateFullTxMemPool) {
				logger.info("Receiving full txMemPool due to bitcoindAdapter/txMemPool (re)start. "
						+ "Dropping last txMemPool (if any) It can take a while...");
				txMemPool.drop();
				updateFullTxMemPool = false;
			}
			txMemPool.refresh(txPoolChanges);
		} else {
			if (!updateFullTxMemPool) {
				logger.info("Full txMemPool received!");
				forceMiningQueueRefresh = true;
			}
			updateFullTxMemPool = true;// Needed if bitcoindAdapter restarts
			txMemPool.refresh(txPoolChanges);
			logger.info("{} transactions in txMemPool.", txMemPool.getTxNumber());
		}
	}

	private void OnNewBlock(Block block, int numConsecutiveBlocks) {
		if (coinBaseTxWeightList.size() != numConsecutiveBlocks) {
			alarmLogger.addAlarm("THIS SHOULD NOT BE HAPPENING: coinBaseTxVSizeList.size() != numConsecutiveBlocks");
			logger.warn("THIS SHOULD NOT BE HAPPENING: coinBaseTxVSizeList.size() != numConsecutiveBlocks");
			return;
		}
		coinBaseTxWeightList.add(block.getCoinBaseTx().getWeight());

		MiningQueue miningQueue = MiningQueue.buildFrom(coinBaseTxWeightList, txMemPool,
				txMempoolProperties.getMiningQueueNumTxs(), coinBaseTxWeightList.size());

		if (miningQueue.isHadErrors()) {
			alarmLogger.addAlarm("Mining Queue had errors, in OnNewBlock");
		}

		Optional<CandidateBlock> optCB = miningQueue.getCandidateBlock(coinBaseTxWeightList.size() - 1);
		if (optCB.isEmpty()) {
			alarmLogger.addAlarm("THIS SHOULD NOT BE HAPPENING: optCB.isEmpty()");
			logger.warn("THIS SHOULD NOT BE HAPPENING: optCB.isEmpty()");
			return;
		}
		MisMinedTransactions misMinedTransactions = new MisMinedTransactions(txMemPool, optCB.get(), block);

		//Check for alarms or inconsistencies
		misMinedTransactionsChecker.check(misMinedTransactions);

		logger.info(misMinedTransactions.toString());
		ignoredTransactionPool.refresh(block, misMinedTransactions, txMemPool);
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
			logger.info("LiveMiningQueue refresh forced.");
			liveMiningQueueContainer.forceRefresh();

			initializing.set(false);
			loadingFullMempool.set(false);
			doResume.set(true);
		} catch (Exception e) {
			// When loading if there are no clients, shutdown.
			logger.error(e.toString());
			alarmLogger.addAlarm("Eror en MemPoolEventsHandler.run, stopping txMemPool service: " + e.toString());
			TxMemPoolApplication.exit();
		}
	}

}
