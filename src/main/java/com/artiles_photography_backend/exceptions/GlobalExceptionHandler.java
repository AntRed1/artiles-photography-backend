package com.artiles_photography_backend.exceptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

/**
 *
 * @author arojas
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(AuthenticationFailedException.class)
	public ResponseEntity<Map<String, String>> handleAuthenticationFailedException(AuthenticationFailedException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("error", ex.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("error", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<Map<String, String>> handleIOException(IOException ex) {
		logger.error("Error de E/S: {}", ex.getMessage(), ex);
		Map<String, String> error = new HashMap<>();
		error.put("error", "Error al interactuar con Google Calendar");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
		logger.error("Error inesperado: {}", ex.getMessage(), ex);
		Map<String, String> error = new HashMap<>();
		error.put("error", "Error interno del servidor");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}

	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("error", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("error", ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("error", ex.getConstraintViolations().iterator().next().getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(JwtGenerationException.class)
	public ResponseEntity<Map<String, String>> handleJwtGenerationException(JwtGenerationException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("error", ex.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}

	@ExceptionHandler(JwtValidationException.class)
	public ResponseEntity<Map<String, String>> handleJwtValidationException(JwtValidationException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("error", ex.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(CloudinaryUploadException.class)
	public ResponseEntity<Map<String, String>> handleCloudinaryUploadException(CloudinaryUploadException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("error", ex.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("error", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<Map<String, String>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("error", "El archivo excede el tamaño máximo permitido");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}
}