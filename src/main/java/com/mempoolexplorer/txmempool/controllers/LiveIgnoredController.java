package com.mempoolexplorer.txmempool.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mempoolexplorer.txmempool.components.IgnoredTransactionsPool;
import com.mempoolexplorer.txmempool.components.IgnoringBlocksPool;
import com.mempoolexplorer.txmempool.controllers.errors.ErrorDetails;
import com.mempoolexplorer.txmempool.controllers.exceptions.IgnoringBlockNotFoundException;
import com.mempoolexplorer.txmempool.controllers.exceptions.TransactionNotFoundException;
import com.mempoolexplorer.txmempool.entites.IgnoredTransaction;
import com.mempoolexplorer.txmempool.entites.IgnoringBlock;
import com.mempoolexplorer.txmempool.entites.ignored.LiveIgnoredTransaction;

@RestController
@RequestMapping("/liveIgnored")
public class LiveIgnoredController {

	@Autowired
	private IgnoredTransactionsPool ignoredTransactionPool;

	@Autowired
	private IgnoringBlocksPool ignoringBlocksPool;

	@GetMapping("/txs")
	public List<LiveIgnoredTransaction> getLiveIgnoredTransactionList() {
		Map<String, IgnoredTransaction> ignoredTransactionMap = ignoredTransactionPool.atomicGetIgnoredTransactionMap();

		var retList = new ArrayList<LiveIgnoredTransaction>();
		ignoredTransactionMap.forEach((txId, iTx) -> {
			LiveIgnoredTransaction liTx = LiveIgnoredTransaction.from(iTx);
			retList.add(liTx);
		});
		return retList;
	}

	@GetMapping("/txsNTimes/{nTimes}")
	public List<LiveIgnoredTransaction> getLiveIgnoredNTimesTransactionList(@PathVariable("nTimes") Integer nTimes) {
		Map<String, IgnoredTransaction> ignoredTransactionMap = ignoredTransactionPool.atomicGetIgnoredTransactionMap();

		var retList = new ArrayList<LiveIgnoredTransaction>();
		ignoredTransactionMap.forEach((txId, iTx) -> {
			if (iTx.getIgnoringBlockList().size() >= nTimes) {
				LiveIgnoredTransaction liTx = LiveIgnoredTransaction.from(iTx);
				retList.add(liTx);
			}
		});
		return retList;
	}

	@GetMapping("/txs/{txId}")
	public LiveIgnoredTransaction getLiveIgnoredTransactionById(@PathVariable("txId") String txId)
			throws TransactionNotFoundException {
		Optional<IgnoredTransaction> ignoredTransaction = ignoredTransactionPool.getIgnoredTransaction(txId);
		if (ignoredTransaction.isEmpty()) {
			throw new TransactionNotFoundException("Transaction txId: " + txId + " not found in ignoredTxPool");
		}
		return LiveIgnoredTransaction.from(ignoredTransaction.get());
	}

	// Heavy weight!!
	@GetMapping("/fullTxs")
	public List<IgnoredTransaction> getIgnoredTransactionList() {
		return ignoredTransactionPool.atomicGetIgnoredTransactionMap().values().stream().collect(Collectors.toList());
	}

	@GetMapping("/fullTxs/{txId}")
	public IgnoredTransaction getIgnoredTransactionById(@PathVariable("txId") String txId)
			throws TransactionNotFoundException {
		Optional<IgnoredTransaction> ignoredTransaction = ignoredTransactionPool.getIgnoredTransaction(txId);
		if (ignoredTransaction.isEmpty()) {
			throw new TransactionNotFoundException("Transaction txId: " + txId + " not found in ignoredTxPool");
		}
		return ignoredTransaction.get();
	}

	@GetMapping("/blocks/{height}")
	public IgnoringBlock getIgnoringBlock(@PathVariable("height") Integer height)
			throws IgnoringBlockNotFoundException {
		Optional<IgnoringBlock> ignoringBlock = ignoringBlocksPool.getIgnoringBlock(height);
		if (ignoringBlock.isEmpty()) {
			throw new IgnoringBlockNotFoundException("Ignoring block with height: " + height + " not found");
		}
		return ignoringBlock.get();
	}

	@GetMapping("/blocks/last")
	public IgnoringBlock getLast() throws IgnoringBlockNotFoundException {
		Optional<IgnoringBlock> ignoringBlock = ignoringBlocksPool.getLast();
		if (ignoringBlock.isEmpty()) {
			throw new IgnoringBlockNotFoundException("Last ignoring block not found");
		}
		return ignoringBlock.get();
	}

	@ExceptionHandler(TransactionNotFoundException.class)
	public ResponseEntity<?> onTransactionNotFound(TransactionNotFoundException e) {
		ErrorDetails errorDetails = new ErrorDetails();
		errorDetails.setErrorMessage(e.getMessage());
		errorDetails.setErrorCode(HttpStatus.NOT_FOUND.toString());
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

}
