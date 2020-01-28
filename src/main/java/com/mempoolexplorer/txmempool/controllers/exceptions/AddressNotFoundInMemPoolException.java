package com.mempoolexplorer.txmempool.controllers.exceptions;

public class AddressNotFoundInMemPoolException extends Exception {

	private static final long serialVersionUID = -2678721372795522019L;

	public AddressNotFoundInMemPoolException() {
		super();
	}

	public AddressNotFoundInMemPoolException(String message) {
		super(message);
	}

	public AddressNotFoundInMemPoolException(String message, Throwable cause) {
		super(message, cause);
	}

}
