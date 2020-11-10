package com.mempoolexplorer.txmempool.controllers.api;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mempoolexplorer.txmempool.components.containers.PoolFactory;
import com.mempoolexplorer.txmempool.controllers.entities.TxIdTimesIgnored;
import com.mempoolexplorer.txmempool.controllers.exceptions.AlgorithmTypeNotFoundException;
import com.mempoolexplorer.txmempool.controllers.exceptions.ServiceNotReadyYetException;
import com.mempoolexplorer.txmempool.entites.AlgorithmType;
import com.mempoolexplorer.txmempool.entites.IgnoredTransaction;
import com.mempoolexplorer.txmempool.entites.pools.IgnoredTransactionsPool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ignoredTxAPI")
public class IgnoredTxAPIController {

    @Autowired
    private PoolFactory poolFactory;

    @GetMapping("/ignoredTxs")
    public List<TxIdTimesIgnored> getIgnoredTxs() throws ServiceNotReadyYetException, AlgorithmTypeNotFoundException {

        IgnoredTransactionsPool igTxPool = poolFactory.getIgnoredTransactionsPool(AlgorithmType.OURS.name());
        if (igTxPool == null)
            throw new ServiceNotReadyYetException();

        Map<String, IgnoredTransaction> igTxMap = igTxPool.atomicGetIgnoredTransactionMap();
        return igTxMap.values().stream()
                .map(igTx -> new TxIdTimesIgnored(igTx.getTx().getTxId(),
                        Integer.valueOf(igTx.getIgnoringBlockList().size())))
                .sorted(Comparator.comparingInt(TxIdTimesIgnored::getNIgnored).reversed()
                        .thenComparing(TxIdTimesIgnored::getTxId))
                .collect(Collectors.toList());
    }

}