package com.mempoolexplorer.txmempool.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.blockchain.Block;
import com.mempoolexplorer.txmempool.entites.MaxMinFeeTransactions;
import com.mempoolexplorer.txmempool.entites.MaxMinFeeTransactionMap;
import com.mempoolexplorer.txmempool.entites.MisMinedTransactions;
import com.mempoolexplorer.txmempool.entites.NotMinedTransaction;
import com.mempoolexplorer.txmempool.entites.RepudiatedTransaction;
import com.mempoolexplorer.txmempool.entites.RepudiatingBlock;
import com.mempoolexplorer.txmempool.entites.RepudiatingBlockStats;
import com.mempoolexplorer.txmempool.utils.SysProps;

@Component
public class RepudiatedTransactionsPoolImpl implements RepudiatedTransactionPool {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<String, RepudiatedTransaction> repudiatedTransactionMap = new HashMap<>();

	long totalFeesLost = 0;// Just For fun

	@Override
	public Map<String, RepudiatedTransaction> getMap() {
		return repudiatedTransactionMap;
	}

	@Override
	public void refresh(Block block, MisMinedTransactions mmt, Set<String> memPoolSet) {

		RepudiatingBlock repudiatingBlock = calculateRepudiatingBlock(block, mmt, memPoolSet);
		totalFeesLost += repudiatingBlock.getLostReward();
		logger.info(repudiatingBlock.toString());
		Iterator<NotMinedTransaction> it = mmt.getNotMinedButInCandidateBlock().getTxMap().values().iterator();
		while (it.hasNext()) {
			NotMinedTransaction nmTx = it.next();
			boolean newRtx = false;
			RepudiatedTransaction rTx = repudiatedTransactionMap.get(nmTx.getTx().getTxId());
			if (null == rTx) {
				rTx = new RepudiatedTransaction();
				newRtx = true;
				rTx.setTx(nmTx.getTx());
				rTx.setState(RepudiatedTransaction.State.INMEMPOOL);
			}
			rTx.getPositionInBlockHeightMap().put(block.getHeight(), nmTx.getOrdinalpositionInBlock());

			rTx.getRepudiatingBlockList().add(repudiatingBlock);
			if (rTx.getRepudiatingBlockList().size() == 1) {
				rTx.setTimeWhenShouldHaveBeenMined(block.getChangeTime());
			}
			
			rTx.setTotalSatvBytesLost(calculateTotalSatvBytesLost(repudiatingBlock, rTx));
			rTx.setTotalFeesLost(calculateTotalFeesLost(repudiatingBlock, rTx));
			if (newRtx) {
				repudiatedTransactionMap.put(rTx.getTx().getTxId(), rTx);
			}
		}

		clearRepudiatedTransactionMap(block, memPoolSet);// In case of mined or deleted txs
		logIt();
	}

	private double calculateTotalSatvBytesLost(RepudiatingBlock repudiatingBlock, RepudiatedTransaction rTx) {
		double totalSatvBytesLost = rTx.getTotalSatvBytesLost();
		double blockSatvBytesLost = repudiatingBlock.getMaxMinFeesInBlock().getMinFee().orElse(0D);
		double diff = rTx.getTx().getSatvByte() - blockSatvBytesLost;
		return totalSatvBytesLost + diff;
	}

	private long calculateTotalFeesLost(RepudiatingBlock repudiatingBlock, RepudiatedTransaction rTx) {
		return (long) (rTx.getTotalSatvBytesLost()*rTx.getTx().getFees().getBase());
	}

	private void logIt() {
		String nl = SysProps.NL;
		StringBuilder sb = new StringBuilder();
		sb.append("repudiatedTransactionMap (#" + repudiatedTransactionMap.size() + "):");
		sb.append(nl);
		repudiatedTransactionMap.values().stream().forEach(rtx -> {
			sb.append(rtx.toString());
			sb.append(nl);
		});
		logger.info(sb.toString());
		logger.info("TotalFeesLost: {}", totalFeesLost);

	}

	private void clearRepudiatedTransactionMap(Block block, Set<String> memPoolSet) {
		// TODO: mirar sincronia de memPoolSet
		List<String> txIdsToRemoveList = new ArrayList<>();// txIds to remove.

		// Delete mined transactions
		Iterator<String> it = block.getTxIds().iterator();
		while (it.hasNext()) {
			String txId = it.next();
			RepudiatedTransaction rt = repudiatedTransactionMap.get(txId);
			if (null != rt) {
				txIdsToRemoveList.add(txId);
				rt.setFinallyMinedOnBlock(block.getHeight());
				rt.setState(RepudiatedTransaction.State.MINED);
			}
		}

		// Delete deleted transactions
		// TODO: It would be nice if we track new txs replacing old ones using Replace
		// by Fee
		Iterator<RepudiatedTransaction> rtIt = repudiatedTransactionMap.values().iterator();
		while (rtIt.hasNext()) {
			RepudiatedTransaction rt = rtIt.next();
			String txId = rt.getTx().getTxId();
			if (!memPoolSet.contains(txId)) {
				txIdsToRemoveList.add(txId);
				rt.setState(RepudiatedTransaction.State.DELETED);
				rt.setFinallyMinedOnBlock(-1);
			}
		}

		logClearedRepudiatedTransactionMap(txIdsToRemoveList);
		// TODO: save in DB before delete?
		txIdsToRemoveList.stream().forEach(txId -> repudiatedTransactionMap.remove(txId));
	}

	private void logClearedRepudiatedTransactionMap(List<String> txIds) {
		String nl = SysProps.NL;
		StringBuilder sb = new StringBuilder();
		sb.append("DeletedRepudiatedTransactions (#" + txIds.size() + "):");
		Iterator<String> it = txIds.iterator();
		while (it.hasNext()) {
			String txId = it.next();
			RepudiatedTransaction rTx = repudiatedTransactionMap.get(txId);
			sb.append(nl);
			sb.append(rTx.toString());
		}
		logger.info(sb.toString());
	}

	private RepudiatingBlock calculateRepudiatingBlock(Block block, MisMinedTransactions mmt, Set<String> memPoolSet) {
		RepudiatingBlock repBlock = new RepudiatingBlock();
		repBlock.setBlockChangeTime(block.getChangeTime());
		repBlock.setBlockHeight(block.getHeight());
		repBlock.setCoinBaseTx(block.getCoinBaseTx());
		repBlock.setLostReward(calculateLostReward(block, mmt));
		repBlock.setMaxMinFeesInBlock(calculateMaxMinFeesInBlock(block, mmt));
		repBlock.setMinedAndInMemPoolStats(calculateStats(mmt.getMinedAndInMemPool()));
		repBlock.setMinedButNotInMemPoolTxNum(mmt.getMinedButNotInMemPool().size());
		repBlock.setMinedInMempoolButNotInCandidateBlockStats(
				calculateStats(mmt.getMinedInMempoolButNotInCandidateBlock()));
		repBlock.setMinerName(RepudiatingBlock.UNKNOWN);
		repBlock.setNotMinedButInCandidateBlockStats(
				calculateNotMinedTransactionStats(mmt.getNotMinedButInCandidateBlock()));
		repBlock.setNumTxInMinedBlock(block.getTxIds().size());
		repBlock.setWeight(block.getWeight());
		return repBlock;
	}

	private RepudiatingBlockStats calculateStats(MaxMinFeeTransactionMap<Transaction> mmftm) {
		RepudiatingBlockStats minedAndInMemPoolStats = new RepudiatingBlockStats(mmftm.getMaxMinFee(),
				mmftm.getTxMap().size(),
				mmftm.getTxMap().values().stream().mapToLong(tx -> tx.getFees().getBase()).sum(),
				mmftm.getTxMap().values().stream().mapToInt(tx -> tx.getvSize()).sum());
		return minedAndInMemPoolStats;
	}

	private RepudiatingBlockStats calculateNotMinedTransactionStats(
			MaxMinFeeTransactionMap<NotMinedTransaction> mmfnmtm) {
		RepudiatingBlockStats minedAndInMemPoolStats = new RepudiatingBlockStats(mmfnmtm.getMaxMinFee(),
				mmfnmtm.getTxMap().size(),
				mmfnmtm.getTxMap().values().stream().mapToLong(tx -> tx.getTx().getFees().getBase()).sum(),
				mmfnmtm.getTxMap().values().stream().mapToInt(tx -> tx.getTx().getvSize()).sum());
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
		maxMinFeesInBlock.checkFees(new MaxMinFeeTransactions(block.getNotInMemPoolTransactions().values().stream()));
		return maxMinFeesInBlock;
	}
}