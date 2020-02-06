package com.mempoolexplorer.txmempool.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.components.alarms.AlarmLogger;
import com.mempoolexplorer.txmempool.entites.IgnoredTransaction;
import com.mempoolexplorer.txmempool.entites.IgnoredTxState;
import com.mempoolexplorer.txmempool.entites.IgnoringBlock;
import com.mempoolexplorer.txmempool.entites.MisMinedTransactions;
import com.mempoolexplorer.txmempool.entites.NotMinedTransaction;
import com.mempoolexplorer.txmempool.properties.TxMempoolProperties;
import com.mempoolexplorer.txmempool.utils.SysProps;

@Component
public class IgnoredTransactionsPoolImpl implements IgnoredTransactionsPool {

	@Autowired
	private AlarmLogger alarmLogger;

	@Autowired
	private IgnoringBlocksPool ignoringBlocksPool;

	@Autowired
	private RepudiatedTransactionsPool repudiatedTransactionsPool;

	@Autowired
	private TxMempoolProperties txMempoolProperties;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// Mutable version
	private Map<String, IgnoredTransaction> ignoredTransactionMap = new HashMap<>(SysProps.EXPECTED_MAX_IGNORED_TXS);

	// Inmmutable version
	private AtomicReference<Map<String, IgnoredTransaction>> ignoredTransactionMapRef = new AtomicReference<>(
			ignoredTransactionMap);

	@Override
	public Map<String, IgnoredTransaction> atomicGetIgnoredTransactionMap() {
		return ignoredTransactionMapRef.get();
	}

	@Override
	public Optional<IgnoredTransaction> getIgnoredTransaction(String txId) {
		return Optional.ofNullable(ignoredTransactionMapRef.get().get(txId));
	}

	@Override
	public void refresh(Block block, MisMinedTransactions mmt, TxMemPool txMemPool) {

		clearIgnoredTransactionMap(block, txMemPool);// In case of mined or deleted txs

		IgnoringBlock ignoringBlock = new IgnoringBlock(mmt);

		ignoringBlocksPool.add(ignoringBlock);

		if (logger.isDebugEnabled()) {
			logger.debug(ignoringBlock.toString());
		}

		Iterator<NotMinedTransaction> it = mmt.getNotMinedButInCandidateBlockMapWD().getFeeableMap().values()
				.iterator();
		while (it.hasNext()) {
			NotMinedTransaction nmTx = it.next();
			IgnoredTransaction igTx = ignoredTransactionMap.get(nmTx.getTx().getTxId());
			if (null == igTx) {
				igTx = new IgnoredTransaction();
				igTx.setTx(nmTx.getTx());
				igTx.setState(IgnoredTxState.INMEMPOOL);
			}
			igTx.getPositionInBlockHeightMap().put(block.getHeight(), nmTx.getOrdinalpositionInBlock());

			igTx.getIgnoringBlockList().add(ignoringBlock);
			if (igTx.getIgnoringBlockList().size() == 1) {
				igTx.setTimeWhenShouldHaveBeenMined(block.getChangeTime());
			}

			igTx.setTotalSatvBytesLost(calculateTotalSatvBytesLost(ignoringBlock, igTx));
			igTx.setTotalFeesLost(calculateTotalFeesLost(igTx));
			ignoredTransactionMap.put(igTx.getTx().getTxId(), igTx);
			
			//Consider repudiated if needed
			if (igTx.getIgnoringBlockList().size() >= txMempoolProperties.getNumTimesTxIgnoredToRaiseAlarm()) {
				repudiatedTransactionsPool.put(igTx);
				alarmLogger.addAlarm("Repudiated transaction txId:" + igTx + ". Has been ignored "
						+ igTx.getIgnoringBlockList().size() + " times.");
			}
		}

		logIt();
		copyMapToAtomicReference();
	}

	private void copyMapToAtomicReference() {
		Map<String, IgnoredTransaction> ignoredTransactionCopyMap = new HashMap<>(
				(int) (ignoredTransactionMap.size() * SysProps.HM_INITIAL_CAPACITY_MULTIPLIER));
		ignoredTransactionCopyMap.putAll(ignoredTransactionMap);
		ignoredTransactionMapRef.set(ignoredTransactionCopyMap);
	}

	private double calculateTotalSatvBytesLost(IgnoringBlock ignoringBlock, IgnoredTransaction igTx) {
		double totalSatvBytesLost = igTx.getTotalSatvBytesLost();
		double blockSatvBytesLost = ignoringBlock.getMinedBlockData().getFeeableData().getMinSatVByteIncAnc()
				.orElse(0D);
		double diff = igTx.getTx().getSatvByte() - blockSatvBytesLost;
		return totalSatvBytesLost + diff;
	}

	private long calculateTotalFeesLost(IgnoredTransaction igTx) {
		return (long) (igTx.getTotalSatvBytesLost() * igTx.getTx().getFees().getBase());
	}

	private void logIt() {
		if (logger.isDebugEnabled()) {

			String nl = SysProps.NL;
			StringBuilder sb = new StringBuilder();
			sb.append("ignoredTransactionMap (#" + ignoredTransactionMap.size() + "):");
			sb.append(nl);
			ignoredTransactionMap.values().stream().forEach(rtx -> {
				sb.append(rtx.toString());
				sb.append(nl);
			});
			logger.debug(sb.toString());
		}
	}

	private void clearIgnoredTransactionMap(Block block, TxMemPool txMemPool) {
		logger.info("MemPool size on clearIgnoredTransactionMap: {}", txMemPool.getTxNumber());
		Set<String> txIdsToRemoveSet = new HashSet<>(SysProps.EXPECTED_MAX_IGNORED_TXS);// txIds to remove.

		// Delete mined transactions
		for (String bTxId : block.getTxIds()) {
			IgnoredTransaction rt = ignoredTransactionMap.get(bTxId);
			if (null != rt) {
				txIdsToRemoveSet.add(bTxId);
				rt.setFinallyMinedOnBlock(block.getHeight());
				rt.setState(IgnoredTxState.MINED);
			}
		}
		// Delete deleted transactions
		// TODO: It would be nice if we track new txs replacing old ones using Replace
		// by Fee
		for (IgnoredTransaction igTx : ignoredTransactionMap.values()) {
			String rTxId = igTx.getTx().getTxId();
			if (!txMemPool.containsTxId(rTxId)) {
				txIdsToRemoveSet.add(rTxId);
				igTx.setState(IgnoredTxState.DELETED);
				igTx.setFinallyMinedOnBlock(-1);
			}
		}

		logClearedIgnoredTransactionMap(txIdsToRemoveSet);
		// TODO: save in DB before delete?
		txIdsToRemoveSet.stream().forEach(txId -> ignoredTransactionMap.remove(txId));
	}

	private void logClearedIgnoredTransactionMap(Set<String> txIds) {
		if (logger.isDebugEnabled()) {

			String nl = SysProps.NL;
			StringBuilder sb = new StringBuilder();
			sb.append("DeletedIgnoredTransactions (#" + txIds.size() + "):");
			Iterator<String> it = txIds.iterator();
			while (it.hasNext()) {
				String txId = it.next();
				IgnoredTransaction igTx = ignoredTransactionMap.get(txId);
				sb.append(nl);
				sb.append(igTx.toString());
			}
			logger.debug(sb.toString());
		}
	}

}
