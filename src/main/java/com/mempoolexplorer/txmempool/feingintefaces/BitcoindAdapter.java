package com.mempoolexplorer.txmempool.feingintefaces;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.AppStateEnum;
import com.mempoolexplorer.txmempool.bitcoindadapter.entites.Transaction;

@FeignClient("bitcoindAdapter")
public interface BitcoindAdapter {

	@GetMapping(value = "/memPool/state", consumes = "application/json")
	AppStateEnum getState();
	
	@GetMapping(value = "/memPool/full", consumes = "application/json")
	Map<String,Transaction> getFullMemPool();
}
