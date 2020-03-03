package com.mempoolexplorer.txmempool;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mempoolexplorer.txmempool.components.containers.PoolFactory;
import com.mempoolexplorer.txmempool.controllers.entities.RecalculateAllStatsResult;
import com.mempoolexplorer.txmempool.entites.AlgorithmType;
import com.mempoolexplorer.txmempool.entites.IgnoringBlock;
import com.mempoolexplorer.txmempool.repositories.entities.MinerNameToBlockHeight;
import com.mempoolexplorer.txmempool.repositories.entities.MinerStatistics;
import com.mempoolexplorer.txmempool.repositories.reactive.IgnoringBlockReactiveRepository;
import com.mempoolexplorer.txmempool.repositories.reactive.MinerNameToBlockHeightReactiveRepository;
import com.mempoolexplorer.txmempool.repositories.reactive.MinerStatisticsReactiveRepository;
import com.mempoolexplorer.txmempool.utils.SysProps;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

	@Autowired
	private PoolFactory poolFactory;

	@Autowired
	private IgnoringBlockReactiveRepository ignoringBlockRepository;

	@Autowired
	private MinerStatisticsReactiveRepository minerStatisticsRepository;

	@Autowired
	private MinerNameToBlockHeightReactiveRepository minerNameToBlockHeightRepository;

	@Override
	public RecalculateAllStatsResult recalculateAllStats() {
		RecalculateAllStatsResult res = new RecalculateAllStatsResult();

		ignoringBlockRepository.deleteAll().block();
		res.getExecutionInfoList().add("ignoringBlockRepository.deleteAll() executed.");
		minerStatisticsRepository.deleteAll().block();
		res.getExecutionInfoList().add("minerStatisticsRepository.deleteAll() executed.");
		minerNameToBlockHeightRepository.deleteAll().block();
		res.getExecutionInfoList().add("minerNameToBlockHeightRepository.deleteAll() executed.");

		ignoringBlockRepository.findAll().doOnNext(ib -> saveStatistics(ib, res)).blockLast();

		return res;

	}

	private void saveStatistics(IgnoringBlock ib, RecalculateAllStatsResult res) {
		String minerName = ib.getMinedBlockData().getCoinBaseData().getMinerName();
		int blockHeight = ib.getMinedBlockData().getHeight();
		AlgorithmType algorithmUsed = ib.getAlgorithmUsed();

		// Only igBlock bitcoind instance saves this to avoid repetitive saves.
		if (algorithmUsed == AlgorithmType.BITCOIND) {
			minerNameToBlockHeightRepository.insert(
					new MinerNameToBlockHeight(minerName, blockHeight, ib.getMinedBlockData().getMedianMinedTime()))
					.block();
			saveMinerStatistics(minerName, ib.getLostReward(), 0, 1);
		} else {
			saveMinerStatistics(minerName, 0, ib.getLostReward(), 0);
		}
		res.getExecutionInfoList().add("Saved stats for block: " + blockHeight + ", Algorithm: " + algorithmUsed);
	}

	@Override
	public boolean saveStatisticsToDB(int blockHeight) {
		Optional<IgnoringBlock> opIGBlockBitcoind = poolFactory.getIgnoringBlocksPool(AlgorithmType.BITCOIND)
				.getIgnoringBlock(blockHeight);
		Optional<IgnoringBlock> opIGBlockOurs = poolFactory.getIgnoringBlocksPool(AlgorithmType.OURS)
				.getIgnoringBlock(blockHeight);
		if (opIGBlockBitcoind.isEmpty() || opIGBlockOurs.isEmpty()) {
			return false;
		}

		IgnoringBlock iGBlockBitcoind = opIGBlockBitcoind.get();
		IgnoringBlock iGBlockOurs = opIGBlockOurs.get();

		ignoringBlockRepository.insert(iGBlockBitcoind).block();
		ignoringBlockRepository.insert(iGBlockOurs).block();

		String minerName = iGBlockBitcoind.getMinedBlockData().getCoinBaseData().getMinerName();

		minerNameToBlockHeightRepository.insert(new MinerNameToBlockHeight(minerName, blockHeight,
				iGBlockBitcoind.getMinedBlockData().getMedianMinedTime())).block();

		saveMinerStatistics(minerName, iGBlockBitcoind.getLostReward(), iGBlockOurs.getLostReward(), 1);

		// SaveGlobalStatistics
		saveMinerStatistics(SysProps.GLOBAL_MINER_NAME, iGBlockBitcoind.getLostReward(), iGBlockOurs.getLostReward(),
				1);

		log.info("Statistics persisted.");
		return true;
	}

	/**
	 * 
	 * @param minerName
	 * @param iGBlockBitcoindLostReward if any
	 * @param iGBlockOursLostReward     if any
	 * @param add                       this is the amount to add to
	 *                                  ms.setNumBlocksMined, useful when there is
	 *                                  igblocks from different algoritms in db.
	 */
	private void saveMinerStatistics(String minerName, long iGBlockBitcoindLostReward, long iGBlockOursLostReward,
			int add) {
		MinerStatistics minerStatistics = minerStatisticsRepository.findById(minerName).map(ms -> {
			ms.setNumBlocksMined(ms.getNumBlocksMined() + add);
			ms.setTotalLostRewardBT(ms.getTotalLostRewardBT() + iGBlockBitcoindLostReward);
			ms.setTotalLostRewardCB(ms.getTotalLostRewardCB() + iGBlockOursLostReward);
			return ms;
		}).defaultIfEmpty(new MinerStatistics(minerName, iGBlockBitcoindLostReward, iGBlockOursLostReward, add))
				.block();

		minerStatisticsRepository.save(minerStatistics).block();
	}

}
