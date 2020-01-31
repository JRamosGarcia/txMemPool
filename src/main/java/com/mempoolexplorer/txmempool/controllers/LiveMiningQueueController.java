package com.mempoolexplorer.txmempool.controllers;

import java.util.Optional;

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
import com.mempoolexplorer.txmempool.components.containers.LiveMiningQueueContainer;
import com.mempoolexplorer.txmempool.controllers.entities.LiveMiningQueueGraphData;
import com.mempoolexplorer.txmempool.controllers.entities.TxInQueue;
import com.mempoolexplorer.txmempool.controllers.errors.ErrorDetails;
import com.mempoolexplorer.txmempool.controllers.exceptions.ServiceNotReadyYetException;
import com.mempoolexplorer.txmempool.controllers.exceptions.TransactionNotFoundException;
import com.mempoolexplorer.txmempool.entites.miningqueue.MiningQueue;
import com.mempoolexplorer.txmempool.entites.miningqueue.CandidateBlock;
import com.mempoolexplorer.txmempool.entites.miningqueue.TxToBeMined;

@RestController
@RequestMapping("/liveMiningQueue")
public class LiveMiningQueueController {

	@Autowired
	private LiveMiningQueueContainer liveMiningQueueContainer;

	@Autowired
	private TxMemPool txMemPool;

	@GetMapping("/graphicData")
	public LiveMiningQueueGraphData getGraphicData() throws ServiceNotReadyYetException {
		if (liveMiningQueueContainer.atomicGet() == null) {
			throw new ServiceNotReadyYetException();
		}
		return liveMiningQueueContainer.atomicGet().getLiveMiningQueueGraphData();
	}

	@GetMapping("/{txId}")
	public TxInQueue getTxInQueue(@PathVariable("txId") String txId)
			throws TransactionNotFoundException, ServiceNotReadyYetException {
		if (liveMiningQueueContainer.atomicGet() == null) {
			throw new ServiceNotReadyYetException();
		}
		MiningQueue miningQueue = liveMiningQueueContainer.atomicGet().getMiningQueue();

		Optional<TxToBeMined> txToBeMined = miningQueue.getTxToBeMined(txId);
		if (txToBeMined.isEmpty()) {
			Optional<Transaction> tx = txMemPool.getTx(txId);
			if (tx.isEmpty()) {
				throw new TransactionNotFoundException("txId: " + txId + " not found.");
			} else {
				// Client must use satVByte to guess an aproximate position
				return new TxInQueue(tx.get(), TxInQueue.UNKNOWN_POSITION);
			}
		} else {
			CandidateBlock containingBlock = txToBeMined.get().getContainingBlock();
			int positionInQueue = containingBlock.getPrecedingTxsCount() + txToBeMined.get().getPositionInBlock();
			return new TxInQueue(txToBeMined.get().getTx(), positionInQueue);
		}
	}

	@ExceptionHandler(TransactionNotFoundException.class)
	public ResponseEntity<?> onTransactionNotFound(TransactionNotFoundException e) {
		ErrorDetails errorDetails = new ErrorDetails();
		errorDetails.setErrorMessage(e.getMessage());
		errorDetails.setErrorCode(HttpStatus.NOT_FOUND.toString());
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(ServiceNotReadyYetException.class)
	public ResponseEntity<?> onServiceNotReadyYet(ServiceNotReadyYetException e) {
		ErrorDetails errorDetails = new ErrorDetails();
		errorDetails.setErrorMessage(e.getMessage());
		errorDetails.setErrorCode(HttpStatus.SERVICE_UNAVAILABLE.toString());
		return new ResponseEntity<>(errorDetails, HttpStatus.SERVICE_UNAVAILABLE);
	}

}