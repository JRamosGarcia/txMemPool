package com.mempoolexplorer.txmempool.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mempoolexplorer.txmempool.StatisticsService;
import com.mempoolexplorer.txmempool.controllers.entities.RecalculateAllStatsResult;
import com.mempoolexplorer.txmempool.properties.TxMempoolProperties;

@RestController
@RequestMapping("/operations")
public class OperationsController {

	@Autowired
	private TxMempoolProperties properties;

	@Autowired
	private StatisticsService statisticsService;

	@GetMapping("/recalculateAllStats")
	private RecalculateAllStatsResult recalculateAllStats() {
		if (!properties.getPersistState()) {
			return statisticsService.recalculateAllStats();
		}
		var rasr = new RecalculateAllStatsResult();
		rasr.getExecutionInfoList().add("Not persisted allowed.");
		return rasr;
	}

}
