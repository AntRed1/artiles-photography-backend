package com.artiles_photography_backend.exceptions;

/**
 *
 * @author arojas
 */
public class AuthenticationFailedException extends RuntimeException {
	public AuthenticationFailedException(String message) {
		super(message);
	}
}
