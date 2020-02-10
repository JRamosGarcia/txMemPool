package com.mempoolexplorer.txmempool.controllers.exceptions;

public class AlgorithmTypeNotFoundException extends Exception {
	
	private static final long serialVersionUID = 3029388442844477246L;

	public AlgorithmTypeNotFoundException() {
		super();
	}

	public AlgorithmTypeNotFoundException(String message) {
		super(message);
	}

	public AlgorithmTypeNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
