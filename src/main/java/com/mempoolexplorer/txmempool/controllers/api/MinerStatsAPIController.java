package com.mempoolexplorer.txmempool.controllers.api;

import java.util.Comparator;

import com.mempoolexplorer.txmempool.controllers.entities.IgnoringBlockStats;
import com.mempoolexplorer.txmempool.controllers.entities.MinerStats;
import com.mempoolexplorer.txmempool.entites.AlgorithmType;
import com.mempoolexplorer.txmempool.repositories.reactive.IgBlockReactiveRepository;
import com.mempoolexplorer.txmempool.repositories.reactive.MinerStatisticsReactiveRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@CrossOrigin
@RestController
@RequestMapping("/minersStatsAPI")
public class MinerStatsAPIController {

    @Autowired
    private MinerStatisticsReactiveRepository minerStatisticsRepository;

    @Autowired
    private IgBlockReactiveRepository igBlockReactiveRepository;

    @GetMapping("/historicStats")
    public Flux<MinerStats> getMinersStats() {
        return minerStatisticsRepository.findAll().map(MinerStats::new);
    }

    @GetMapping("/ignoringBlocks/{minerName}")
    public Flux<IgnoringBlockStats> getIgnoringBlocks(@PathVariable("minerName") String minerName) {

        return igBlockReactiveRepository.findAll().filter(igBlock -> igBlock.getAlgorithmUsed() == AlgorithmType.OURS)
                .map(IgnoringBlockStats::new).filter(ibs -> ibs.getMinerName().compareToIgnoreCase(minerName) == 0)
                .sort(Comparator.comparingInt(IgnoringBlockStats::getHeight));
    }
}