package com.mempoolexplorer.txmempool.controllers.exceptions;

public class TransactionNotFoundInMemPoolException extends Exception {

	private static final long serialVersionUID = 3372623966394648556L;

	public TransactionNotFoundInMemPoolException() {
		super();
	}

	public TransactionNotFoundInMemPoolException(String message) {
		super(message);
	}

	public TransactionNotFoundInMemPoolException(String message, Throwable cause) {
		super(message, cause);
	}

}
