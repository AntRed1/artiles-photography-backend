package com.artiles_photography_backend.exceptions;

/**
 *
 * @author arojas
 */
public class JwtValidationException extends RuntimeException {
	public JwtValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}
