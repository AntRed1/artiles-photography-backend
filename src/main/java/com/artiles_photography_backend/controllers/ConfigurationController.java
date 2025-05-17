package com.artiles_photography_backend.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.artiles_photography_backend.dtos.ConfigurationRequest;
import com.artiles_photography_backend.dtos.ConfigurationResponse;
import com.artiles_photography_backend.dtos.ConfigurationUploadRequest;
import com.artiles_photography_backend.services.ConfigurationService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

/**
 * @author arojas
 *         Controlador REST para manejar peticiones relacionadas con
 *         Configuration.
 */
@RestController
@RequestMapping("/api")
public class ConfigurationController {

	private static final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

	private final ConfigurationService configurationService;

	public ConfigurationController(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	@GetMapping("/config")
	public ResponseEntity<ConfigurationResponse> getConfiguration() {
		try {
			logger.info("Recuperando configuración");
			return ResponseEntity.ok(configurationService.getConfiguration());
		} catch (EntityNotFoundException e) {
			logger.warn("Configuración no encontrada: {}", e.getMessage());
			return ResponseEntity.status(404).body(null);
		}
	}

	@GetMapping("/config/{id}")
	public ResponseEntity<ConfigurationResponse> getConfigurationById(@PathVariable Long id) {
		logger.info("Recuperando configuración con ID: {}", id);
		return ResponseEntity.ok(configurationService.getConfigurationById(id));
	}

	@GetMapping("/config/hero")
	public ResponseEntity<String> getHeroBackgroundImage() {
		try {
			logger.info("Recuperando imagen de fondo hero");
			return ResponseEntity.ok(configurationService.getHeroBackgroundImage());
		} catch (EntityNotFoundException e) {
			logger.warn("Imagen de fondo hero no encontrada: {}", e.getMessage());
			return ResponseEntity.status(404).body(null);
		}
	}

	@GetMapping("/admin/config/all")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<ConfigurationResponse>> getAllConfigurations() {
		logger.info("Recuperando todas las configuraciones");
		return ResponseEntity.ok(configurationService.getAllConfigurations());
	}

	@PostMapping("/admin/config")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ConfigurationResponse> createConfiguration(
			@Valid @RequestBody ConfigurationUploadRequest request) {
		logger.info("Creando nueva configuración");
		return ResponseEntity.status(201).body(configurationService.createConfiguration(request));
	}

	@PutMapping(value = "/admin/config/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ConfigurationResponse> updateConfiguration(
			@PathVariable Long id,
			@RequestPart(name = "logoFile", required = false) MultipartFile logoFile,
			@RequestPart(name = "logoAltText", required = false) String logoAltText,
			@RequestPart(name = "heroBackgroundImage", required = false) String heroBackgroundImage,
			@RequestPart(name = "availabilityMessage", required = false) String availabilityMessage,
			@RequestPart(name = "responseTime", required = false) String responseTime,
			@RequestPart(name = "notificationsEnabled", required = false) String notificationsEnabled) {
		logger.info("Actualizando configuración con ID: {} con posible archivo", id);

		try {
			// Crear ConfigurationUploadRequest
			ConfigurationUploadRequest request = new ConfigurationUploadRequest();
			request.setLogoFile(logoFile);
			request.setLogoAltText(logoAltText);
			request.setHeroBackgroundImage(heroBackgroundImage);
			request.setAvailabilityMessage(availabilityMessage);
			request.setResponseTime(responseTime);
			request.setNotificationsEnabled(notificationsEnabled != null ? Boolean.parseBoolean(notificationsEnabled) : null);

			// Delegar al servicio
			ConfigurationResponse response = configurationService.updateConfiguration(id, request);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			logger.error("Error de validación: {}", e.getMessage());
			return ResponseEntity.badRequest().body(null);
		} catch (Exception e) {
			logger.error("Error al actualizar configuración: {}", e.getMessage(), e);
			throw new RuntimeException("Error al actualizar configuración", e);
		}
	}

	@PutMapping(value = "/admin/config/{id}/no-file", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ConfigurationResponse> updateConfigurationNoFile(
			@PathVariable Long id, @Valid @RequestBody ConfigurationRequest request) {
		logger.info("Actualizando configuración con ID: {} sin archivo", id);
		return ResponseEntity.ok(configurationService.updateConfiguration(id, request));
	}

	@DeleteMapping("/admin/config/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteConfiguration(@PathVariable Long id) {
		logger.info("Eliminando configuración con ID: {}", id);
		configurationService.deleteConfiguration(id);
		return ResponseEntity.noContent().build();
	}
}