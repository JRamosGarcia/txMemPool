package com.mempoolexplorer.txmempool.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mempoolexplorer.txmempool.components.TxMemPool;
import com.mempoolexplorer.txmempool.controllers.errors.ErrorDetails;
import com.mempoolexplorer.txmempool.controllers.exceptions.TransactionNotFoundInMemPoolException;

@RestController
@RequestMapping("/memPool")
public class MemPoolController {

	@Autowired
	private TxMemPool txMemPool;

	@GetMapping("/{txId}")
	public Boolean existTxId(@PathVariable("txId") String txId) throws TransactionNotFoundInMemPoolException {
		return txMemPool.getTxIdSet().contains(txId);
	}

	@ExceptionHandler(TransactionNotFoundInMemPoolException.class)
	public ResponseEntity<?> onTransactionNotFound(TransactionNotFoundInMemPoolException e) {
		ErrorDetails errorDetails = new ErrorDetails();
		errorDetails.setErrorMessage(e.getMessage());
		errorDetails.setErrorCode(HttpStatus.NOT_FOUND.toString());
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

}
