package com.mempoolexplorer.txmempool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import com.mongodb.ConnectionString;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

@Configuration
@EnableReactiveMongoRepositories
class MyReactiveMongoConf extends AbstractReactiveMongoConfiguration {

	@Value("${spring.data.mongodb.uri}")
	private String connString;

	@Override
	public MongoClient reactiveMongoClient() {
		return MongoClients.create(connString);
	}

	@Override
	protected String getDatabaseName() {
		ConnectionString cs = new ConnectionString(connString);
		return cs.getDatabase();
	}
}