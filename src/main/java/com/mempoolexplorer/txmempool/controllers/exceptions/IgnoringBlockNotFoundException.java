package com.mempoolexplorer.txmempool.controllers.exceptions;

public class IgnoringBlockNotFoundException extends Exception {

	private static final long serialVersionUID = 9081789344029880305L;

	public IgnoringBlockNotFoundException() {
		super();
	}

	public IgnoringBlockNotFoundException(String message) {
		super(message);
	}

	public IgnoringBlockNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
