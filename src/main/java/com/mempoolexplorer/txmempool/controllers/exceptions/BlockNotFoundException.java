package com.mempoolexplorer.txmempool.controllers.exceptions;

public class BlockNotFoundException extends Exception {

	private static final long serialVersionUID = 9081789344029880305L;

	public BlockNotFoundException() {
		super();
	}

	public BlockNotFoundException(String message) {
		super(message);
	}

	public BlockNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
