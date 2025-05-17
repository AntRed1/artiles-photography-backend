package com.artiles_photography_backend.exceptions;

/**
 *
 * @author arojas
 */
public class CloudinaryUploadException extends RuntimeException {
	public CloudinaryUploadException(String message, Throwable cause) {
		super(message, cause);
	}
}
