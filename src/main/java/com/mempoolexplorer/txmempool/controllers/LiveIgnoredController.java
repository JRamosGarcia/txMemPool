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

import com.mempoolexplorer.txmempool.components.containers.PoolFactory;
import com.mempoolexplorer.txmempool.controllers.errors.ErrorDetails;
import com.mempoolexplorer.txmempool.controllers.exceptions.AlgorithmTypeNotFoundException;
import com.mempoolexplorer.txmempool.controllers.exceptions.BlockNotFoundException;
import com.mempoolexplorer.txmempool.controllers.exceptions.TransactionNotFoundException;
import com.mempoolexplorer.txmempool.entites.IgnoredTransaction;
import com.mempoolexplorer.txmempool.entites.IgnoringBlock;
import com.mempoolexplorer.txmempool.entites.ignored.LiveIgnoredTransaction;

@RestController
@RequestMapping("/liveIgnored")
public class LiveIgnoredController {

	@Autowired
	private PoolFactory poolFactory;

	@GetMapping("/{algo}/txs")
	public List<LiveIgnoredTransaction> getLiveIgnoredTransactionList(@PathVariable("algo") String algo)
			throws AlgorithmTypeNotFoundException {

		Map<String, IgnoredTransaction> ignoredTransactionMap = poolFactory.getIgnoredTransactionsPool(algo)
				.atomicGetIgnoredTransactionMap();

		var retList = new ArrayList<LiveIgnoredTransaction>();
		ignoredTransactionMap.forEach((txId, iTx) -> {
			LiveIgnoredTransaction liTx = LiveIgnoredTransaction.from(iTx);
			retList.add(liTx);
		});
		return retList;
	}

	@GetMapping("/{algo}/txsNTimes/{nTimes}")
	public List<LiveIgnoredTransaction> getLiveIgnoredNTimesTransactionList(@PathVariable("algo") String algo,
			@PathVariable("nTimes") Integer nTimes) throws AlgorithmTypeNotFoundException {
		Map<String, IgnoredTransaction> ignoredTransactionMap = poolFactory.getIgnoredTransactionsPool(algo)
				.atomicGetIgnoredTransactionMap();

		var retList = new ArrayList<LiveIgnoredTransaction>();
		ignoredTransactionMap.forEach((txId, iTx) -> {
			if (iTx.getIgnoringBlockList().size() >= nTimes) {
				LiveIgnoredTransaction liTx = LiveIgnoredTransaction.from(iTx);
				retList.add(liTx);
			}
		});
		return retList;
	}

	@GetMapping("/{algo}/txs/{txId}")
	public LiveIgnoredTransaction getLiveIgnoredTransactionById(@PathVariable("algo") String algo,
			@PathVariable("txId") String txId) throws TransactionNotFoundException, AlgorithmTypeNotFoundException {
		Optional<IgnoredTransaction> ignoredTransaction = poolFactory.getIgnoredTransactionsPool(algo)
				.getIgnoredTransaction(txId);
		if (ignoredTransaction.isEmpty()) {
			throw new TransactionNotFoundException("Transaction txId: " + txId + " not found in ignoredTxPool");
		}
		return LiveIgnoredTransaction.from(ignoredTransaction.get());
	}

	// Heavy weight!!
	@GetMapping("/{algo}/fullTxs")
	public List<IgnoredTransaction> getIgnoredTransactionList(@PathVariable("algo") String algo) throws AlgorithmTypeNotFoundException {
		return poolFactory.getIgnoredTransactionsPool(algo).atomicGetIgnoredTransactionMap().values().stream()
				.collect(Collectors.toList());
	}

	@GetMapping("/{algo}/fullTxs/{txId}")
	public IgnoredTransaction getIgnoredTransactionById(@PathVariable("algo") String algo,
			@PathVariable("txId") String txId) throws TransactionNotFoundException, AlgorithmTypeNotFoundException {
		Optional<IgnoredTransaction> ignoredTransaction = poolFactory.getIgnoredTransactionsPool(algo)
				.getIgnoredTransaction(txId);
		if (ignoredTransaction.isEmpty()) {
			throw new TransactionNotFoundException("Transaction txId: " + txId + " not found in ignoredTxPool");
		}
		return ignoredTransaction.get();
	}

	@GetMapping("/{algo}/blocks")
	public Map<Integer, IgnoringBlock> getIgnoringBlockMap(@PathVariable("algo") String algo) throws AlgorithmTypeNotFoundException {
		return poolFactory.getIgnoringBlocksPool(algo).getIgnoringBlocksMap();
	}

	@GetMapping("/{algo}/blocks/{height}")
	public IgnoringBlock getIgnoringBlock(@PathVariable("algo") String algo, @PathVariable("height") Integer height)
			throws BlockNotFoundException, AlgorithmTypeNotFoundException {
		Optional<IgnoringBlock> ignoringBlock = poolFactory.getIgnoringBlocksPool(algo).getIgnoringBlock(height);
		if (ignoringBlock.isEmpty()) {
			throw new BlockNotFoundException("Ignoring block with height: " + height + " not found");
		}
		return ignoringBlock.get();
	}

	@GetMapping("/{algo}/blocks/last")
	public IgnoringBlock getLast(@PathVariable("algo") String algo) throws BlockNotFoundException, AlgorithmTypeNotFoundException {
		Optional<IgnoringBlock> ignoringBlock = poolFactory.getIgnoringBlocksPool(algo).getLast();
		if (ignoringBlock.isEmpty()) {
			throw new BlockNotFoundException("Last ignoring block not found");
		}
		return ignoringBlock.get();
	}

	@ExceptionHandler(AlgorithmTypeNotFoundException.class)
	public ResponseEntity<?> onAlgorithmTypeNotFoundException(AlgorithmTypeNotFoundException e) {
		ErrorDetails errorDetails = new ErrorDetails();
		errorDetails.setErrorMessage(e.getMessage());
		errorDetails.setErrorCode(HttpStatus.NOT_FOUND.toString());
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(TransactionNotFoundException.class)
	public ResponseEntity<?> onTransactionNotFound(TransactionNotFoundException e) {
		ErrorDetails errorDetails = new ErrorDetails();
		errorDetails.setErrorMessage(e.getMessage());
		errorDetails.setErrorCode(HttpStatus.NOT_FOUND.toString());
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(BlockNotFoundException.class)
	public ResponseEntity<?> onIgnoringBlockNotFound(BlockNotFoundException e) {
		ErrorDetails errorDetails = new ErrorDetails();
		errorDetails.setErrorMessage(e.getMessage());
		errorDetails.setErrorCode(HttpStatus.NOT_FOUND.toString());
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

}
