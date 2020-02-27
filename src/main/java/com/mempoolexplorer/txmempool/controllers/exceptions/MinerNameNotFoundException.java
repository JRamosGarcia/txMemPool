package com.mempoolexplorer.txmempool.controllers.exceptions;

public class MinerNameNotFoundException extends Exception {

	private static final long serialVersionUID = 3290927037379587017L;

	public MinerNameNotFoundException() {
		super();
	}

	public MinerNameNotFoundException(String message) {
		super(message);
	}

	public MinerNameNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
