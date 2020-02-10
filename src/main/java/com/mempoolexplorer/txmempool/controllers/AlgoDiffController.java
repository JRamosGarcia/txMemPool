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

import com.mempoolexplorer.txmempool.components.containers.AlgorithmDiffContainer;
import com.mempoolexplorer.txmempool.controllers.errors.ErrorDetails;
import com.mempoolexplorer.txmempool.controllers.exceptions.BlockNotFoundException;
import com.mempoolexplorer.txmempool.entites.AlgorithmDifferences;

@RestController
@RequestMapping("/algo")
public class AlgoDiffController {

	@Autowired
	private AlgorithmDiffContainer algoDiffContainer;

	@GetMapping("/diffs/{height}")
	public AlgorithmDifferences getAlgorithmDifferences(@PathVariable("height") Integer height)
			throws BlockNotFoundException {
		AlgorithmDifferences algorithmDifferences = algoDiffContainer.getHeightToAlgoDiffMap().get(height);
		if (algorithmDifferences == null) {
			throw new BlockNotFoundException();
		}
		return algorithmDifferences;
	}

	@GetMapping("/diffs/last")
	public AlgorithmDifferences getLastAlgorithmDifferences() throws BlockNotFoundException {
		Optional<AlgorithmDifferences> opAlgorithmDifferences = algoDiffContainer.getLast();
		if (opAlgorithmDifferences.isEmpty()) {
			throw new BlockNotFoundException();
		}
		return opAlgorithmDifferences.get();
	}

	@ExceptionHandler(BlockNotFoundException.class)
	public ResponseEntity<?> onIgnoringBlockNotFound(BlockNotFoundException e) {
		ErrorDetails errorDetails = new ErrorDetails();
		errorDetails.setErrorMessage(e.getMessage());
		errorDetails.setErrorCode(HttpStatus.NOT_FOUND.toString());
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

}
