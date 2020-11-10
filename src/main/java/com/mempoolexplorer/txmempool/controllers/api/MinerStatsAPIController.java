package com.mempoolexplorer.txmempool.controllers.api;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mempoolexplorer.txmempool.components.containers.PoolFactory;
import com.mempoolexplorer.txmempool.controllers.entities.IgnoringBlockStats;
import com.mempoolexplorer.txmempool.controllers.entities.MinerStats;
import com.mempoolexplorer.txmempool.controllers.exceptions.AlgorithmTypeNotFoundException;
import com.mempoolexplorer.txmempool.controllers.exceptions.ServiceNotReadyYetException;
import com.mempoolexplorer.txmempool.entites.AlgorithmType;
import com.mempoolexplorer.txmempool.entites.IgnoringBlock;
import com.mempoolexplorer.txmempool.entites.pools.IgnoringBlocksPool;
import com.mempoolexplorer.txmempool.repositories.reactive.MinerStatisticsReactiveRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/minersStatsAPI")
public class MinerStatsAPIController {

    @Autowired
    private MinerStatisticsReactiveRepository minerStatisticsRepository;

    @Autowired
    private PoolFactory poolFactory;

    @GetMapping("/historicStats")
    public Flux<MinerStats> getMinersStats() {
        return minerStatisticsRepository.findAll().map(MinerStats::new);
    }

    @GetMapping("/ignoringBlocks/{minerName}")
    public List<IgnoringBlockStats> getIgnoringBlocks(@PathVariable("minerName") String minerName)
            throws AlgorithmTypeNotFoundException, ServiceNotReadyYetException {
        IgnoringBlocksPool igBlocksPool = poolFactory.getIgnoringBlocksPool(AlgorithmType.OURS.name());
        if (igBlocksPool == null)
            throw new ServiceNotReadyYetException();

        Map<Integer, IgnoringBlock> ignoringBlocksMap = igBlocksPool.getIgnoringBlocksMap();

        return ignoringBlocksMap.values().stream().map(IgnoringBlockStats::new)
                .filter(ibs -> ibs.getMinerName().compareToIgnoreCase(minerName) == 0)
                .sorted(Comparator.comparingInt(IgnoringBlockStats::getHeight)).collect(Collectors.toList());
    }
}