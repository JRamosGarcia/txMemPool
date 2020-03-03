package com.mempoolexplorer.txmempool.repositories.reactive;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.mempoolexplorer.txmempool.entites.IgnoringBlock;

@Repository
public interface IgnoringBlockReactiveRepository extends ReactiveMongoRepository<IgnoringBlock, String> {

}
