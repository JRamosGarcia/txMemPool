package com.mempoolexplorer.txmempool.components.containers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.txmempool.components.alarms.AlarmLogger;
import com.mempoolexplorer.txmempool.controllers.exceptions.AlgorithmTypeNotFoundException;
import com.mempoolexplorer.txmempool.entites.AlgorithmType;
import com.mempoolexplorer.txmempool.entites.pools.IgnoredTransactionsPool;
import com.mempoolexplorer.txmempool.entites.pools.IgnoredTransactionsPoolImpl;
import com.mempoolexplorer.txmempool.entites.pools.IgnoringBlocksPool;
import com.mempoolexplorer.txmempool.entites.pools.IgnoringBlocksPoolImpl;
import com.mempoolexplorer.txmempool.entites.pools.RepudiatedTransactionsPool;
import com.mempoolexplorer.txmempool.entites.pools.RepudiatedTransactionsPoolImpl;
import com.mempoolexplorer.txmempool.properties.TxMempoolProperties;

@Component
public class PoolFactoryImpl implements PoolFactory {

	@Autowired
	private AlarmLogger alarmLogger;

	@Autowired
	private TxMempoolProperties txMempoolProperties;

	private List<RepudiatedTransactionsPool> repTxPoolList = new ArrayList<>(2);

	private List<IgnoringBlocksPool> igBlocksPoolList = new ArrayList<>(2);

	private List<IgnoredTransactionsPool> igTxPoolList = new ArrayList<>(2);

	@PostConstruct
	public void init() {
		igBlocksPoolList.add(new IgnoringBlocksPoolImpl(txMempoolProperties));
		igBlocksPoolList.add(new IgnoringBlocksPoolImpl(txMempoolProperties));

		repTxPoolList.add(new RepudiatedTransactionsPoolImpl());
		repTxPoolList.add(new RepudiatedTransactionsPoolImpl());

		igTxPoolList.add(new IgnoredTransactionsPoolImpl(alarmLogger, igBlocksPoolList.get(0), repTxPoolList.get(0),
				txMempoolProperties));
		igTxPoolList.add(new IgnoredTransactionsPoolImpl(alarmLogger, igBlocksPoolList.get(1), repTxPoolList.get(1),
				txMempoolProperties));
	}

	@Override
	public RepudiatedTransactionsPool getRepudiatedTransactionsPool(AlgorithmType at) {
		return repTxPoolList.get(at.getIndex());
	}

	@Override
	public IgnoringBlocksPool getIgnoringBlocksPool(AlgorithmType at) {
		return igBlocksPoolList.get(at.getIndex());
	}

	@Override
	public IgnoredTransactionsPool getIgnoredTransactionsPool(AlgorithmType at) {
		return igTxPoolList.get(at.getIndex());
	}

	@Override
	public RepudiatedTransactionsPool getRepudiatedTransactionsPool(String at) throws AlgorithmTypeNotFoundException {
		return repTxPoolList.get(from(at).getIndex());
	}

	@Override
	public IgnoringBlocksPool getIgnoringBlocksPool(String at) throws AlgorithmTypeNotFoundException {
		return igBlocksPoolList.get(from(at).getIndex());
	}

	@Override
	public IgnoredTransactionsPool getIgnoredTransactionsPool(String at) throws AlgorithmTypeNotFoundException {
		return igTxPoolList.get(from(at).getIndex());
	}

	private AlgorithmType from(String at) throws AlgorithmTypeNotFoundException {
		if (at.compareToIgnoreCase(AlgorithmType.BITCOIND.getName()) == 0) {
			return AlgorithmType.BITCOIND;
		}
		if (at.compareToIgnoreCase(AlgorithmType.OURS.getName()) == 0) {
			return AlgorithmType.OURS;
		}
		throw new AlgorithmTypeNotFoundException();
	}

	@Override
	public void drop() {
		repTxPoolList.stream().forEach(RepudiatedTransactionsPool::drop);
		igBlocksPoolList.stream().forEach(IgnoringBlocksPool::drop);
		igTxPoolList.forEach(IgnoredTransactionsPool::drop);
	}

}
