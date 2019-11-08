package com.mempoolexplorer.txmempool.feingintefaces;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.AppStateEnum;

@FeignClient("bitcoindAdapter")
public interface BitcoindAdapter {

	@GetMapping(value = "/memPool/state", consumes = "application/json")
	AppStateEnum getState();
}
