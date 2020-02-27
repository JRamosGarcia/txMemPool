package com.mempoolexplorer.txmempool.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mempoolexplorer.txmempool.entites.IgnoringBlock;

@Repository
public interface IgnoringBlockRepository extends MongoRepository<IgnoringBlock, String> {


}
