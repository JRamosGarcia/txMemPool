package com.mempoolexplorer.txmempool;

import com.mempoolexplorer.txmempool.repositories.entities.MinerNameToBlockHeight;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@SpringBootApplication
@RefreshScope
@EnableFeignClients
@EnableCircuitBreaker
public class TxMemPoolApplication {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private MongoMappingContext mongoMappingContext;

	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		context = SpringApplication.run(TxMemPoolApplication.class, args);
	}

	public static void exit() {
		SpringApplication.exit(context, () -> 1);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void initIndicesAfterStartup() {
		IndexResolver resolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);

		IndexOperations indexOps = mongoTemplate.indexOps(MinerNameToBlockHeight.class);
		resolver.resolveIndexFor(MinerNameToBlockHeight.class).forEach(indexOps::ensureIndex);
	}

}
