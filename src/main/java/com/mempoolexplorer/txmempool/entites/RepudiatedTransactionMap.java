package com.mempoolexplorer.txmempool.entites;

import java.util.HashMap;

public class RepudiatedTransactionMap extends HashMap<String, RepudiatedTransaction> {

	private static final long serialVersionUID = -2763236361360478075L;

	public RepudiatedTransactionMap(int initalCapacity) {
		super(initalCapacity);
	}

}
