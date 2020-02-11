package com.mempoolexplorer.txmempool.entites;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.entites.blocktemplate.BlockTemplate;
import com.mempoolexplorer.txmempool.entites.blocktemplate.BlockTemplateTx;
import com.mempoolexplorer.txmempool.entites.miningqueue.CandidateBlock;

public class AlgorithmDiff {

	private int blockHeight;
	private Boolean candidateBlockCorrect = Boolean.FALSE;
	private String firstOffendingTx;
	private FeeableData oursData = new FeeableData();
	private FeeableData bitcoindData = new FeeableData();
	private AlgorithmDiffSets algoDiffs;
	private List<Transaction> txOrderedListOurs = new ArrayList<>();
	private List<BlockTemplateTx> txOrderedListBitcoind = new ArrayList<>();

	public AlgorithmDiff(TxMemPool txMemPool, CandidateBlock oursCB, BlockTemplate blockTemplate, int blockHeight) {
		this.blockHeight = blockHeight;
		candidateBlockCorrect = oursCB.checkIsCorrect();
		this.txOrderedListOurs = oursCB.getOrderedStream().map(txTBM -> txTBM.getTx())
				.sorted(Comparator.comparingDouble(Transaction::getSatvByte).reversed()).collect(Collectors.toList());
		order(blockTemplate);

		oursData.checkFees(txOrderedListOurs.stream());
		bitcoindData.checkFees(txOrderedListBitcoind.stream());

		this.algoDiffs = new AlgorithmDiffSets(txMemPool, blockTemplate, oursCB);
		calculateFirstOffending();
	}

	private void calculateFirstOffending() {
		Iterator<Transaction> oursIt = txOrderedListOurs.iterator();
		Iterator<BlockTemplateTx> bitcoindIt = txOrderedListBitcoind.iterator();
		while (oursIt.hasNext() && bitcoindIt.hasNext()) {
			Transaction oursTx = oursIt.next();
			BlockTemplateTx bitcoindTx = bitcoindIt.next();
			String oursTxId = oursTx.getTxId();
			String bitcoindTxId = bitcoindTx.getTxId();
			if (!oursTxId.equals(bitcoindTxId)) {
				if (oursTx.getSatvByte() != bitcoindTx.getSatvByte()) {
					firstOffendingTx = bitcoindTxId;
					break;
				}
			}
		}

	}

	private void order(BlockTemplate blockTemplate) {
		this.txOrderedListBitcoind = blockTemplate.getBlockTemplateTxMap().values().stream()
				.sorted(Comparator.comparingDouble(BlockTemplateTx::getSatvByte).reversed())
				.collect(Collectors.toList());
	}

	public int getBlockHeight() {
		return blockHeight;
	}

	public Boolean getCandidateBlockCorrect() {
		return candidateBlockCorrect;
	}

	public String getFirstOffendingTx() {
		return firstOffendingTx;
	}

	public FeeableData getOursData() {
		return oursData;
	}

	public FeeableData getBitcoindData() {
		return bitcoindData;
	}

	public AlgorithmDiffSets getAlgoDiffs() {
		return algoDiffs;
	}

	public List<Transaction> getTxOrderedListOurs() {
		return txOrderedListOurs;
	}

	public List<BlockTemplateTx> getTxOrderedListBitcoind() {
		return txOrderedListBitcoind;
	}

}
