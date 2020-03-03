package com.mempoolexplorer.txmempool;

import com.mempoolexplorer.txmempool.controllers.entities.RecalculateAllStatsResult;

public interface StatisticsService {

	RecalculateAllStatsResult recalculateAllStats();

	boolean saveStatisticsToDB(int blockHeight);

}