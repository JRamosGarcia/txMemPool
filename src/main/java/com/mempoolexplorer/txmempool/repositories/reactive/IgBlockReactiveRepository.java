package com.mempoolexplorer.txmempool.repositories.reactive;

import com.mempoolexplorer.txmempool.entites.IgnoringBlock;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IgBlockReactiveRepository extends ReactiveMongoRepository<IgnoringBlock, String> {

}
