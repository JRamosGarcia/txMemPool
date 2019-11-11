package com.mempoolexplorer.txmempool.controllers;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mempoolexplorer.txmempool.bitcoindadapter.entites.AppStateEnum;
import com.mempoolexplorer.txmempool.feingintefaces.BitcoindAdapter;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;


//TODO: Hace falta poner un manejador de excepciones?
@RestController
@RequestMapping("/redirectMemPool")
public class RedirectController {

	@Autowired
	private BitcoindAdapter bitcoinAdapter;
	
	@GetMapping("")
	@HystrixCommand(commandProperties=
		{@HystrixProperty(
				name="execution.isolation.thread.timeoutInMilliseconds",
				value="2000")},fallbackMethod = "getDefaultAppState",
				threadPoolKey = "getMemPoolThreadPool",
				threadPoolProperties =
				{@HystrixProperty(name = "coreSize",value="3"),
				@HystrixProperty(name="maxQueueSize", value="-1")})
	public AppStateEnum getMemPool() {
		randomlyRunLong();
		return bitcoinAdapter.getState();
	}
	
	@HystrixCommand(threadPoolKey = "getMemPoolThreadPool")
	@GetMapping("/noFalla")
	public AppStateEnum getMemPoolNoFail() {
		return bitcoinAdapter.getState();
	}
	
	@SuppressWarnings("unused")//Used by above method.
	private AppStateEnum getDefaultAppState() {
		return AppStateEnum.LOADINGFROMBITCOINCLIENT;
	}
	private void randomlyRunLong(){
		Random rand = new Random();
		int randomNum = rand.nextInt(4);
		if (randomNum==3) sleep();
	}
	
	private void sleep(){
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
