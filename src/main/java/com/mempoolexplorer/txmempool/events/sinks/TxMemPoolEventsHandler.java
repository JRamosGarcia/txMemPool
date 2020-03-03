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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.ApplicationListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.event.ListenerContainerIdleEvent;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import com.mempoolexplorer.txmempool.StatisticsService;
import com.mempoolexplorer.txmempool.TxMemPoolApplication;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blocktemplate.BlockTemplateChanges;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.mempool.TxPoolChanges;
import com.mempoolexplorer.txmempool.components.MinerNameResolver;
import com.mempoolexplorer.txmempool.components.MisMinedTransactionsChecker;
import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.components.alarms.AlarmLogger;
import com.mempoolexplorer.txmempool.components.containers.AlgorithmDiffContainer;
import com.mempoolexplorer.txmempool.components.containers.BlockTemplateContainer;
import com.mempoolexplorer.txmempool.components.containers.LiveAlgorithmDiffContainer;
import com.mempoolexplorer.txmempool.components.containers.LiveMiningQueueContainer;
import com.mempoolexplorer.txmempool.components.containers.MinerNamesUnresolvedContainer;
import com.mempoolexplorer.txmempool.components.containers.PoolFactory;
import com.mempoolexplorer.txmempool.entites.AlgorithmDiff;
import com.mempoolexplorer.txmempool.entites.CoinBaseData;
import com.mempoolexplorer.txmempool.entites.MisMinedTransactions;
import com.mempoolexplorer.txmempool.entites.blocktemplate.BlockTemplate;
import com.mempoolexplorer.txmempool.entites.miningqueue.CandidateBlock;
import com.mempoolexplorer.txmempool.entites.miningqueue.MiningQueue;
import com.mempoolexplorer.txmempool.events.CustomChannels;
import com.mempoolexplorer.txmempool.events.MempoolEvent;
import com.mempoolexplorer.txmempool.feinginterfaces.BitcoindAdapter;
import com.mempoolexplorer.txmempool.properties.TxMempoolProperties;
import com.mempoolexplorer.txmempool.utils.SysProps;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableBinding(CustomChannels.class)
public class TxMemPoolEventsHandler implements Runnable, ApplicationListener<ListenerContainerIdleEvent> {

	@Autowired
	private TxMemPool txMemPool;

	@Autowired
	private TaskExecutor taskExecutor;

	@Autowired
	private BitcoindAdapter bitcoindAdapter;

	@Autowired
	private LiveMiningQueueContainer liveMiningQueueContainer;

	@Autowired
	private TxMempoolProperties txMempoolProperties;

	@Autowired
	private AlarmLogger alarmLogger;

	@Autowired
	private MisMinedTransactionsChecker misMinedTransactionsChecker;

	@Autowired
	private BlockTemplateContainer blockTemplateContainer;

	@Autowired
	private AlgorithmDiffContainer algoDiffContainer;

	@Autowired
	private PoolFactory poolFactory;

	@Autowired
	private LiveAlgorithmDiffContainer liveAlgorithmDiffContainer;

	@Autowired
	private MinerNameResolver minerNameResolver;

	@Autowired
	private MinerNamesUnresolvedContainer minerNamesUnresolvedContainer;

	@Autowired
	private StatisticsService statisticsService;

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
				Block block = mempoolEvent.tryGetBlock().get();
				log.info("New block(height: " + block.getHeight() + ", hash:" + block.getHash() + "txNum: "
						+ block.getTxIds().size() + ") ---------------------------");
				OnNewBlock(block);
				// alarmLogger.prettyPrint();
				numConsecutiveBlocks++;
			} else if (mempoolEvent.getEventType() == MempoolEvent.EventType.REFRESH_POOL) {
				numConsecutiveBlocks = 0;
				coinBaseTxWeightList.clear();// If we have new txPoolChanges, we reset coinBaseVSizeList
				// OnRefreshPool
				TxPoolChanges txpc = mempoolEvent.tryGetTxPoolChanges().get();
				Optional<BlockTemplateChanges> opBTC = mempoolEvent.tryGetBlockTemplateChanges();
				validate(txpc);
				// When initializing but bitcoindAdapter is not initializing
				if ((initializing.get()) && (txpc.getChangeCounter() != 0) && (!loadingFullMempool.get())) {
					// We pause incoming messages, but several messages has been taken from kafka at
					// once so this method will be called several times. Refresh the mempool only if
					// not initializing
					log.info("txMemPool is starting but bitcoindAdapter started long ago... "
							+ "pausing receiving kafka messages and loading full mempool from REST interface");
					consumer.pause(Collections.singleton(new TopicPartition(topic, 0)));
					loadingFullMempool.set(true);
					// Load full mempool asyncronous via REST service, then resume kafka msgs
					doFullLoadAsync();// Method must return ASAP, this is a kafka queue.
				} else if (!loadingFullMempool.get()) {// This is because consumer.pause does not pause inmediately
					refreshContainers(txpc, opBTC);
					initializing.set(false);
				}
			}
		} catch (Exception e) {
			log.error("Exception: ", e);
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

	// Refresh mempool, liveMiningQueue, blockTemplateContainer and
	// liveAlgorithmDiffContainer
	private void refreshContainers(TxPoolChanges txpc, Optional<BlockTemplateChanges> opBTC) {
		Optional<MiningQueue> opMQ = Optional.empty();
		// Order of this operations matters.
		refreshMempool(txpc);
		if (txpc.getChangeCounter() != 0) {
			opMQ = liveMiningQueueContainer.refreshIfNeeded();
		}
		if (forceMiningQueueRefresh) {
			log.info("LiveMiningQueue refresh forced.");
			opMQ = Optional.of(liveMiningQueueContainer.forceRefresh());
			forceMiningQueueRefresh = false;
		}

		opBTC.ifPresent(blockTemplateContainer::refresh);

		if (txMempoolProperties.getLiveAlgorithmDiffsEnabled()) {
			if (opMQ.isPresent()) {// Mining Queue could not be refreshed.
				AlgorithmDiff liveAlgorithmDiff = new AlgorithmDiff(txMemPool,
						opMQ.get().getCandidateBlock(0).orElseGet(CandidateBlock::empty),
						blockTemplateContainer.getBlockTemplate(), 0);
				liveAlgorithmDiffContainer.setLiveAlgorithmDiff(liveAlgorithmDiff);
			}
		}
	}

	public void refreshMempool(TxPoolChanges txPoolChanges) {
		if (txPoolChanges.getChangeCounter() == 0) {
			if (updateFullTxMemPool) {
				log.info("Receiving full txMemPool due to bitcoindAdapter/txMemPool (re)start. "
						+ "Dropping last txMemPool and BlockTemplate (if any) It can take a while...");
				txMemPool.drop();
				blockTemplateContainer.drop();
				updateFullTxMemPool = false;
			}
			txMemPool.refresh(txPoolChanges);
		} else {
			if (!updateFullTxMemPool) {
				log.info("Full txMemPool received!");
				forceMiningQueueRefresh = true;
			}
			updateFullTxMemPool = true;// Needed if bitcoindAdapter restarts
			txMemPool.refresh(txPoolChanges);
			log.info("{} transactions in txMemPool.", txMemPool.getTxNumber());
		}
	}

	private void OnNewBlock(Block block) {
		if (coinBaseTxWeightList.size() != numConsecutiveBlocks) {
			alarmLogger.addAlarm("THIS SHOULD NOT BE HAPPENING: coinBaseTxVSizeList.size() != numConsecutiveBlocks");
			log.warn("THIS SHOULD NOT BE HAPPENING: coinBaseTxVSizeList.size() != numConsecutiveBlocks");
			return;
		}
		coinBaseTxWeightList.add(block.getCoinBaseTx().getWeight());

		MiningQueue miningQueue = buildMiningQueue();
		// CandidateBlock can be empty
		CandidateBlock candidateBlock = getCandidateBlock(block.getHeight(), miningQueue);
		Optional<Boolean> isCorrect = checkCandidateBlockIsCorrect(block.getHeight(), candidateBlock);

		// When two blocks arrive without refreshing mempool this is ALWAYS empty
		BlockTemplate blockTemplate = getBlockTemplate(block.getHeight());
		CoinBaseData coinBaseData = resolveMinerName(block);

		MisMinedTransactions mmtBlockTemplate = new MisMinedTransactions(txMemPool, blockTemplate, block, coinBaseData);
		MisMinedTransactions mmtCandidateBlock = new MisMinedTransactions(txMemPool, candidateBlock, block,
				coinBaseData);

		buildAndStoreAlgorithmDifferences(block, candidateBlock, blockTemplate, isCorrect);

		// Check for alarms or inconsistencies
		misMinedTransactionsChecker.check(mmtBlockTemplate);
		misMinedTransactionsChecker.check(mmtCandidateBlock);

		poolFactory.getIgnoredTransactionsPool(mmtBlockTemplate.getAlgorithmUsed()).refresh(block, mmtBlockTemplate,
				txMemPool);
		poolFactory.getIgnoredTransactionsPool(mmtCandidateBlock.getAlgorithmUsed()).refresh(block, mmtCandidateBlock,
				txMemPool);

		if (txMempoolProperties.getPersistState()) {
			// Returns a boolean if blockHeight does not exist in igTxPool.
			// we ignore it since it's saved above
			statisticsService.saveStatisticsToDB(block.getHeight());
		}
	}

	private void buildAndStoreAlgorithmDifferences(Block block, CandidateBlock candidateBlock,
			BlockTemplate blockTemplate, Optional<Boolean> isCorrect) {
		AlgorithmDiff ad = new AlgorithmDiff(txMemPool, candidateBlock, blockTemplate, block.getHeight(), isCorrect);
		algoDiffContainer.put(ad);

		if (ad.getBitcoindData().getTotalBaseFee().isPresent() && ad.getOursData().getTotalBaseFee().isPresent()
				&& ad.getBitcoindData().getTotalBaseFee().get().longValue() > ad.getOursData().getTotalBaseFee().get()
						.longValue()) {
			alarmLogger.addAlarm("Bitcoind algorithm better than us in block: " + block.getHeight());
		}
	}

	private CoinBaseData resolveMinerName(Block block) {
		CoinBaseData coinBaseData = minerNameResolver.resolveFrom(block.getCoinBaseTx().getvInField());

		if (coinBaseData.getMinerName().compareTo(SysProps.MINER_NAME_UNKNOWN) == 0) {
			minerNamesUnresolvedContainer.addCoinBaseField(coinBaseData.getAscciOfField(), block.getHeight());
		}
		return coinBaseData;
	}

	private BlockTemplate getBlockTemplate(int blockHeight) {
		BlockTemplate blockTemplate = blockTemplateContainer.getBlockTemplate();
		if (numConsecutiveBlocks != 0) {
			blockTemplate = BlockTemplate.empty();
			alarmLogger
					.addAlarm("OnNewBlock height: " + blockHeight + ", numConsecutiveBlocks=" + numConsecutiveBlocks);
		}
		return blockTemplate;
	}

	private CandidateBlock getCandidateBlock(int blockHeight, MiningQueue miningQueue) {
		CandidateBlock candidateBlock = miningQueue.getCandidateBlock(coinBaseTxWeightList.size() - 1)
				.orElse(CandidateBlock.empty());

		return candidateBlock;
	}

	private Optional<Boolean> checkCandidateBlockIsCorrect(int blockHeight, CandidateBlock candidateBlock) {
		Optional<Boolean> opIsCorrect = candidateBlock.checkIsCorrect();
		if (opIsCorrect.isEmpty()) {
			alarmLogger.addAlarm("We can't determinate if CandidateBlock is incorrect in block:" + blockHeight);
		} else {
			if (!opIsCorrect.get()) {
				alarmLogger.addAlarm("CandidateBlock is incorrect in block:" + blockHeight);
			}
		}
		return opIsCorrect;
	}

	private MiningQueue buildMiningQueue() {
		MiningQueue miningQueue = MiningQueue.buildFrom(coinBaseTxWeightList, txMemPool,
				txMempoolProperties.getMiningQueueNumTxs(), coinBaseTxWeightList.size());

		if (miningQueue.isHadErrors()) {
			alarmLogger.addAlarm("Mining Queue had errors, in OnNewBlock");
		}
		return miningQueue;
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
			BlockTemplate blockTemplate = bitcoindAdapter.getBlockTemplate();
			Map<String, Transaction> fullMemPoolMap = bitcoindAdapter.getFullMemPool();

			TxPoolChanges txpc = new TxPoolChanges();
			txpc.setChangeCounter(0);// Force reset
			txpc.setChangeTime(Instant.now());
			txpc.setNewTxs(new ArrayList<>(fullMemPoolMap.values()));
			txMemPool.refresh(txpc);
			log.info("LiveMiningQueue refresh forced.");
			liveMiningQueueContainer.forceRefresh();
			blockTemplateContainer.setBlockTemplate(blockTemplate);

			initializing.set(false);
			loadingFullMempool.set(false);
			doResume.set(true);
		} catch (Exception e) {
			// When loading if there are no clients, shutdown.
			log.error(e.toString());
			alarmLogger.addAlarm("Eror en MemPoolEventsHandler.run, stopping txMemPool service: " + e.toString());
			TxMemPoolApplication.exit();
		}
	}

}
