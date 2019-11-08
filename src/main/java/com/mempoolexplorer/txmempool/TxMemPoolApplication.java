package com.mempoolexplorer.txmempool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@SpringBootApplication
@RefreshScope
public class TxMemPoolApplication {

	public static void main(String[] args) {
		SpringApplication.run(TxMemPoolApplication.class, args);
	}

}
