package com.davecoss.android.genericserver;

public class FileError extends HTTPError {
	/**
	 * UID Generated by Eclipse
	 */
	private static final long serialVersionUID = -4146380696835160355L;

	public FileError() {
		super();
	}

	public FileError(String message) {
		super(message);
	}

	public FileError(String message, Throwable throwable) {
		super(message, throwable);
	}
}
