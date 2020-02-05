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

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.components.alarms.AlarmLogger;
import com.mempoolexplorer.txmempool.entites.IgnoredTransaction;
import com.mempoolexplorer.txmempool.entites.IgnoredTxState;
import com.mempoolexplorer.txmempool.entites.IgnoringBlock;
import com.mempoolexplorer.txmempool.entites.IgnoringBlockStats;
import com.mempoolexplorer.txmempool.entites.MaxMinFeeTransactionMap;
import com.mempoolexplorer.txmempool.entites.MaxMinFeeTransactions;
import com.mempoolexplorer.txmempool.entites.MisMinedTransactions;
import com.mempoolexplorer.txmempool.entites.NotMinedTransaction;
import com.mempoolexplorer.txmempool.entites.miningqueue.CandidateBlock;
import com.mempoolexplorer.txmempool.utils.AsciiUtils;
import com.mempoolexplorer.txmempool.utils.SysProps;

@Component
public class IgnoredTransactionsPoolImpl implements IgnoredTransactionsPool {

	@Autowired
	private AlarmLogger alarmLogger;

	@Autowired
	private IgnoringBlocksPool ignoringBlocksPool;

	@Autowired
	private RepudiatedTransactionsPool repudiatedTransactionsPool;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// Mutable version
	private Map<String, IgnoredTransaction> ignoredTransactionMap = new HashMap<>(SysProps.EXPECTED_MAX_IGNORED_TXS);

	// Inmmutable version
	private AtomicReference<Map<String, IgnoredTransaction>> ignoredTransactionMapRef = new AtomicReference<>(
			ignoredTransactionMap);

	long totalGlobalFeesLost = 0;// Just For fun

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

		IgnoringBlock ignoringBlock = calculateIgnorigBlock(block, mmt);

		ignoringBlocksPool.add(ignoringBlock);

		long lostReward = ignoringBlock.getLostReward().longValue();

		if (lostReward < 0L) {
			alarmLogger.addAlarm("Lost Reward: " + lostReward + ", in block: " + ignoringBlock.getBlockHeight());
		}

		totalGlobalFeesLost += lostReward;
		logger.info(ignoringBlock.toString());
		Iterator<NotMinedTransaction> it = mmt.getNotMinedButInCandidateBlock().getTxMap().values().iterator();
		while (it.hasNext()) {
			NotMinedTransaction nmTx = it.next();
			boolean newIgTx = false;
			IgnoredTransaction igTx = ignoredTransactionMap.get(nmTx.getTx().getTxId());
			if (null == igTx) {
				igTx = new IgnoredTransaction();
				newIgTx = true;
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
			if (!newIgTx) {// ignored twice or more. it's repudiated
				repudiatedTransactionsPool.put(igTx);
				alarmLogger.addAlarm("Repudiated transaction txId:" + igTx + ". Has been repudiated "
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
		double blockSatvBytesLost = ignoringBlock.getMaxMinFeesInBlock().getMinSatVByte().orElse(0D);
		double diff = igTx.getTx().getSatvByte() - blockSatvBytesLost;
		return totalSatvBytesLost + diff;
	}

	private long calculateTotalFeesLost(IgnoredTransaction igTx) {
		return (long) (igTx.getTotalSatvBytesLost() * igTx.getTx().getFees().getBase());
	}

	private void logIt() {
		String nl = SysProps.NL;
		StringBuilder sb = new StringBuilder();
		sb.append("ignoredTransactionMap (#" + ignoredTransactionMap.size() + "):");
		sb.append(nl);
		ignoredTransactionMap.values().stream().forEach(rtx -> {
			sb.append(rtx.toString());
			sb.append(nl);
		});
		logger.info(sb.toString());
		logger.info("TotalGlobalFeesLost: {}", totalGlobalFeesLost);

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
		logger.info(sb.toString());
	}

	private IgnoringBlock calculateIgnorigBlock(Block block, MisMinedTransactions mmt) {
		IgnoringBlock igBlock = new IgnoringBlock();
		igBlock.setBlockChangeTime(block.getChangeTime());
		igBlock.setBlockHeight(block.getHeight());
		igBlock.setCoinBaseTx(block.getCoinBaseTx());
		igBlock.setLostReward(calculateLostReward(block, mmt));
		igBlock.setMaxMinFeesInBlock(calculateMaxMinFeesInBlock(block, mmt));

		igBlock.setCandidateBlockStats(calculateStats(mmt.getCandidateBlock()));

		igBlock.setMinedAndInMemPoolStats(calculateStats(mmt.getMinedAndInMemPool()));
		igBlock.setMinedButNotInMemPoolTxNum(mmt.getMinedButNotInMemPoolSet().size());
		igBlock.setMinedInMempoolButNotInCandidateBlockStats(
				calculateStats(mmt.getMinedInMempoolButNotInCandidateBlock()));
		igBlock.setMinerName(IgnoringBlock.UNKNOWN);
		igBlock.setAscciCoinBaseField(AsciiUtils.hexToAscii(block.getCoinBaseTx().getvInField()));
		igBlock.setNotMinedButInCandidateBlockStats(
				calculateNotMinedTransactionStats(mmt.getNotMinedButInCandidateBlock()));
		igBlock.setNumTxInMinedBlock(block.getTxIds().size());
		igBlock.setWeight(block.getWeight());
		return igBlock;
	}

	private IgnoringBlockStats calculateStats(CandidateBlock candidateBlock) {
		MaxMinFeeTransactions mmft = new MaxMinFeeTransactions();

		candidateBlock.getEntriesStream().map(entry -> entry.getValue()).forEach(txtbm -> {
			mmft.checkFees(txtbm.getTx());
		});
		IgnoringBlockStats candidateBlockStats = new IgnoringBlockStats(mmft, candidateBlock.numTxs(),
				candidateBlock.getTotalFees(), candidateBlock.getWeight());
		return candidateBlockStats;
	}

	private IgnoringBlockStats calculateStats(MaxMinFeeTransactionMap<Transaction> mmftm) {
		IgnoringBlockStats minedAndInMemPoolStats = new IgnoringBlockStats(mmftm.getMaxMinFee(),
				mmftm.getTxMap().size(),
				mmftm.getTxMap().values().stream().mapToLong(tx -> tx.getFees().getBase()).sum(),
				mmftm.getTxMap().values().stream().mapToInt(tx -> tx.getWeight()).sum());
		return minedAndInMemPoolStats;
	}

	private IgnoringBlockStats calculateNotMinedTransactionStats(MaxMinFeeTransactionMap<NotMinedTransaction> mmfnmtm) {
		IgnoringBlockStats minedAndInMemPoolStats = new IgnoringBlockStats(mmfnmtm.getMaxMinFee(),
				mmfnmtm.getTxMap().size(),
				mmfnmtm.getTxMap().values().stream().mapToLong(tx -> tx.getTx().getFees().getBase()).sum(),
				mmfnmtm.getTxMap().values().stream().mapToInt(tx -> tx.getTx().getWeight()).sum());
		return minedAndInMemPoolStats;
	}

	private Long calculateLostReward(Block block, MisMinedTransactions mmt) {
		long feesMined = mmt.getMinedInMempoolButNotInCandidateBlock().getTxMap().values().stream()
				.mapToLong(tx -> tx.getFees().getBase()).sum();
		feesMined += block.getNotInMemPoolTransactions().values().stream().mapToLong(tx -> tx.getFees()).sum();

		long feesNotMined = mmt.getNotMinedButInCandidateBlock().getTxMap().values().stream()
				.mapToLong(tx -> tx.getTx().getFees().getBase()).sum();

		return feesNotMined - feesMined;
	}

	private MaxMinFeeTransactions calculateMaxMinFeesInBlock(Block block, MisMinedTransactions mmt) {
		MaxMinFeeTransactions maxMinFeesInBlock = new MaxMinFeeTransactions();
		maxMinFeesInBlock.checkFees(mmt.getMinedAndInMemPool().getMaxMinFee());
		// block.getNotInMemPoolTransactions() does not contain coinBaseTx
		maxMinFeesInBlock.checkFees(new MaxMinFeeTransactions(block.getNotInMemPoolTransactions().values().stream()));
		return maxMinFeesInBlock;
	}

}
