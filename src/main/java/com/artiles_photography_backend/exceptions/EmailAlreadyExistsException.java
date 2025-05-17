package com.artiles_photography_backend.exceptions;

/**
 *
 * @author arojas
 */
public class EmailAlreadyExistsException extends RuntimeException {
	public EmailAlreadyExistsException(String message) {
		super(message);
	}
}
