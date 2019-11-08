package com.mempoolexplorer.txmempool.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.AppStateEnum;
import com.mempoolexplorer.txmempool.feingintefaces.BitcoindAdapter;


//TODO: Hace falta poner un manejador de excepciones?
@RestController
@RequestMapping("/redirectMemPool")
public class RedirectController {

	@Autowired
	private BitcoindAdapter bitcoinAdapter;
	
	@GetMapping("")
	public AppStateEnum getMemPool() {
		return bitcoinAdapter.getState();
	}

}
