package com.mempoolexplorer.txmempool.controllers.exceptions;

public class ServiceNotReadyYetException extends Exception{
	
	private static final long serialVersionUID = -8571481345991606123L;

	public ServiceNotReadyYetException() {
		super();
	}

	public ServiceNotReadyYetException(String message) {
		super(message);
	}

	public ServiceNotReadyYetException(String message, Throwable cause) {
		super(message, cause);
	}


}
