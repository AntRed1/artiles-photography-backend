package com.artiles_photography_backend.exceptions;

/**
 *
 * @author arojas
 */
public class JwtGenerationException extends RuntimeException {
	public JwtGenerationException(String message, Throwable cause) {
		super(message, cause);
	}
}
