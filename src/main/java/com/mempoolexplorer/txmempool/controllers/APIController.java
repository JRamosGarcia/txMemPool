package com.mempoolexplorer.txmempool.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mempoolexplorer.txmempool.components.containers.LiveMiningQueueContainer;

@RestController
@RequestMapping("/api")
public class APIController {

	@Autowired
	private LiveMiningQueueContainer liveMiningQueueContainer;

}
