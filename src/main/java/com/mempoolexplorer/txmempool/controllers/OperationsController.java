package com.mempoolexplorer.txmempool.controllers;

import com.mempoolexplorer.txmempool.controllers.entities.RecalculateAllStatsResult;
import com.mempoolexplorer.txmempool.services.StatisticsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/operations")
public class OperationsController {

	@Autowired
	private StatisticsService statisticsService;

	@GetMapping("/recalculateAllStats")
	public RecalculateAllStatsResult recalculateAllStats() {
		return statisticsService.recalculateAllStats();
	}

}
