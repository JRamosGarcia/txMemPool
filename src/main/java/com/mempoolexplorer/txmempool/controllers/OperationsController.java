package com.mempoolexplorer.txmempool.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mempoolexplorer.txmempool.properties.TxMempoolProperties;

@RestController
@RequestMapping("/operations")
public class OperationsController {

	@Autowired
	private TxMempoolProperties properties;

	@GetMapping("/recalculateAllStats")
	private Boolean recalculateAllStats() {
		if (!properties.getPersistState()) {
			return false;
		}
		
		
		
		return true;
	}

}
