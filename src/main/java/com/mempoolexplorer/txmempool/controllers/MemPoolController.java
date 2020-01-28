package com.mempoolexplorer.txmempool.controllers;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;
import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.controllers.errors.ErrorDetails;
import com.mempoolexplorer.txmempool.controllers.exceptions.TransactionNotFoundInMemPoolException;

@RestController
@RequestMapping("/memPool")
public class MemPoolController {

	@Autowired
	private TxMemPool txMemPool;

	@GetMapping("/size")
	public Integer getSize() {
		return txMemPool.getTxNumber();
	}

	@GetMapping("exist/{txId}")
	public Boolean existTxId(@PathVariable("txId") String txId) throws TransactionNotFoundInMemPoolException {
		return txMemPool.containsKey(txId);
	}

	@GetMapping("/{txId}")
	public Transaction getTxId(@PathVariable("txId") String txId) throws TransactionNotFoundInMemPoolException {
		Optional<Transaction> tx = txMemPool.getTx(txId);
		if (tx.isPresent()) {
			return tx.get();
		}
		throw new TransactionNotFoundInMemPoolException();
	}

	@GetMapping("/fullRaw")
	public List<Transaction> getRawTxList() {
		return txMemPool.getDescendingTxStream().collect(Collectors.toList());
	}

	@GetMapping("/fullTxIds")
	public List<String> getTxIdsList() {
		return txMemPool.getDescendingTxStream().map(tx -> tx.getTxId()).collect(Collectors.toList());
	}

	@GetMapping("/parentsof/{txId}")
	public Set<String> getParentsOfTxId(@PathVariable("txId") String txId)
			throws TransactionNotFoundInMemPoolException {
		Optional<Transaction> tx = txMemPool.getTx(txId);
		if (tx.isPresent()) {
			return txMemPool.getAllParentsOf(tx.get());
		}
		throw new TransactionNotFoundInMemPoolException();
	}

	@ExceptionHandler(TransactionNotFoundInMemPoolException.class)
	public ResponseEntity<?> onTransactionNotFound(TransactionNotFoundInMemPoolException e) {
		ErrorDetails errorDetails = new ErrorDetails();
		errorDetails.setErrorMessage(e.getMessage());
		errorDetails.setErrorCode(HttpStatus.NOT_FOUND.toString());
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

}
