package com.mempoolexplorer.txmempool.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mempoolexplorer.txmempool.controllers.errors.ErrorDetails;
import com.mempoolexplorer.txmempool.controllers.exceptions.MinerNameNotFoundException;
import com.mempoolexplorer.txmempool.repositories.entities.MinerStatistics;
import com.mempoolexplorer.txmempool.repositories.reactive.MinerNameToBlockHeightReactiveRepository;
import com.mempoolexplorer.txmempool.repositories.reactive.MinerStatisticsReactiveRepository;
import com.mempoolexplorer.txmempool.utils.SysProps;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/minerStatistics")
public class MinerStatisticsController {

	@Autowired
	private MinerNameToBlockHeightReactiveRepository minerNameToBlockHeightRepository;

	@Autowired
	private MinerStatisticsReactiveRepository minerStatisticsRepository;

	@GetMapping("/minerNames")
	public Mono<List<String>> getMinerNames() {
		// TODO: do this but in BD
		return minerNameToBlockHeightRepository.findAll().map(mTb -> mTb.getMinerToBlock().getMinerName()).distinct().collectList();
	}

	@GetMapping("/{minerName}")
	public Mono<MinerStatistics> getMinerStatistics(@PathVariable("minerName") String minerName)
			throws MinerNameNotFoundException {
		return minerStatisticsRepository.findById(minerName.toLowerCase())
				.switchIfEmpty(Mono.error(new MinerNameNotFoundException("Miner name:" + minerName + " not found.")));
	}

	@GetMapping("/global")
	public Mono<MinerStatistics> getGlobalMinerStatistics() throws MinerNameNotFoundException {
		return minerStatisticsRepository.findById(SysProps.GLOBAL_MINER_NAME)
				.switchIfEmpty(Mono.error(new MinerNameNotFoundException("Global miner statistics not found.")));

	}

	@ExceptionHandler(MinerNameNotFoundException.class)
	public ResponseEntity<?> onMinerNameNotFound(MinerNameNotFoundException e) {
		ErrorDetails errorDetails = new ErrorDetails();
		errorDetails.setErrorMessage(e.getMessage());
		errorDetails.setErrorCode(HttpStatus.NOT_FOUND.toString());
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

}
