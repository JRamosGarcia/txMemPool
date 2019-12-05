package com.mempoolexplorer.txmempool;

import org.apache.tomcat.util.net.WriteBuffer.Sink;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;

@SpringBootApplication
@RefreshScope
@EnableFeignClients
@EnableCircuitBreaker
@EnableBinding(Sink.class)
public class TxMemPoolApplication {

	public static void main(String[] args) {
		SpringApplication.run(TxMemPoolApplication.class, args);
	}

}
