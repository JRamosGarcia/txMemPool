package com.mempoolexplorer.txmempool.controllers.api;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mempoolexplorer.txmempool.components.containers.PoolFactory;
import com.mempoolexplorer.txmempool.controllers.entities.IgnoringBlockStats;
import com.mempoolexplorer.txmempool.controllers.entities.IgnoringBlockStatsEx;
import com.mempoolexplorer.txmempool.controllers.exceptions.AlgorithmTypeNotFoundException;
import com.mempoolexplorer.txmempool.controllers.exceptions.ServiceNotReadyYetException;
import com.mempoolexplorer.txmempool.entites.AlgorithmType;
import com.mempoolexplorer.txmempool.entites.IgnoringBlock;
import com.mempoolexplorer.txmempool.entites.pools.IgnoringBlocksPool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ignoringBlocksAPI")
public class IgnoringBlocksAPIController {

    @Autowired
    private PoolFactory poolFactory;

    @GetMapping("/ignoringBlocks")
    public List<IgnoringBlockStats> getIgnoringBlocks()
            throws AlgorithmTypeNotFoundException, ServiceNotReadyYetException {
        IgnoringBlocksPool igBlocksPool = poolFactory.getIgnoringBlocksPool(AlgorithmType.OURS.name());
        if (igBlocksPool == null)
            throw new ServiceNotReadyYetException();

        Map<Integer, IgnoringBlock> ignoringBlocksMap = igBlocksPool.getIgnoringBlocksMap();

        return ignoringBlocksMap.values().stream().map(IgnoringBlockStats::new)
                .sorted(Comparator.comparingInt(IgnoringBlockStats::getHeight)).collect(Collectors.toList());
    }

    @GetMapping("/ignoringBlock/{height}")
    public IgnoringBlockStatsEx getIgnoringBlockStatsEx(@PathVariable("height") Integer height)
            throws AlgorithmTypeNotFoundException, ServiceNotReadyYetException {
        IgnoringBlocksPool igBlocksPool = poolFactory.getIgnoringBlocksPool(AlgorithmType.OURS.name());
        if (igBlocksPool == null)
            throw new ServiceNotReadyYetException();

        IgnoringBlock igBlock = igBlocksPool.getIgnoringBlocksMap().get(height);

        return new IgnoringBlockStatsEx(igBlock);

    }

}
