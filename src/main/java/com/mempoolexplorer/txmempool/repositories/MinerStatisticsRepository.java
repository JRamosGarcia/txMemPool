package com.mempoolexplorer.txmempool.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mempoolexplorer.txmempool.repositories.entities.MinerStatistics;

@Repository
public interface MinerStatisticsRepository extends MongoRepository<MinerStatistics, String> {


}
