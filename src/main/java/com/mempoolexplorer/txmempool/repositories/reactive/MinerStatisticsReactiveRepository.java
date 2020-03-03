package com.mempoolexplorer.txmempool.repositories.reactive;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.mempoolexplorer.txmempool.repositories.entities.MinerStatistics;

@Repository
public interface MinerStatisticsReactiveRepository extends ReactiveMongoRepository<MinerStatistics, String> {

}
