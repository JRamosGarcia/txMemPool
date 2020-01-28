package com.mempoolexplorer.txmempool.entites;

import java.util.HashMap;

public class IgnoredTransactionMap extends HashMap<String, IgnoredTransaction> {

	private static final long serialVersionUID = -2763236361360478075L;

	public IgnoredTransactionMap(int initalCapacity) {
		super(initalCapacity);
	}

}
