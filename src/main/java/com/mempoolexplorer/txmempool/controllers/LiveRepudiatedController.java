package com.mempoolexplorer.txmempool.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mempoolexplorer.txmempool.components.RepudiatedTransactionsPool;
import com.mempoolexplorer.txmempool.controllers.errors.ErrorDetails;
import com.mempoolexplorer.txmempool.controllers.exceptions.TransactionNotFoundException;
import com.mempoolexplorer.txmempool.entites.IgnoredTransaction;

@RestController
@RequestMapping("/liveRepudiated")
public class LiveRepudiatedController {

	@Autowired
	private RepudiatedTransactionsPool repudiatedTransactionsPool;

	@GetMapping("/txs")
	public List<IgnoredTransaction> getLiveRepudiatedTransactionList() {
		return repudiatedTransactionsPool.getRepudiatedTransactionList();
	}

	@GetMapping("/txs/{txId}")
	public IgnoredTransaction getLiveRepudiatedTransactionById(@PathVariable("txId") String txId)
			throws TransactionNotFoundException {
		Optional<IgnoredTransaction> rTx = repudiatedTransactionsPool.getRepudiatedTransaction(txId);
		if (rTx.isEmpty()) {
			throw new TransactionNotFoundException("Repudiated Transaction id:" + txId + " not found");
		}
		return rTx.get();
	}

	@ExceptionHandler(TransactionNotFoundException.class)
	public ResponseEntity<?> onTransactionNotFound(TransactionNotFoundException e) {
		ErrorDetails errorDetails = new ErrorDetails();
		errorDetails.setErrorMessage(e.getMessage());
		errorDetails.setErrorCode(HttpStatus.NOT_FOUND.toString());
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

}
