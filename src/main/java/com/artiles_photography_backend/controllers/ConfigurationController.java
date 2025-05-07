package com.artiles_photography_backend.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.dtos.ConfigurationRequest;
import com.artiles_photography_backend.dtos.ConfigurationResponse;
import com.artiles_photography_backend.services.ConfigurationService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

/**
 * @author arojas
 *         Controlador REST para manejar peticiones relacionadas con
 *         Configuration.
 */
@RestController
@RequestMapping("/api/config")
public class ConfigurationController {

	private final ConfigurationService configurationService;

	public ConfigurationController(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	@GetMapping
	public ResponseEntity<ConfigurationResponse> getConfiguration() {
		try {
			return ResponseEntity.ok(configurationService.getConfiguration());
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(404).body(null);
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<ConfigurationResponse> getConfigurationById(@PathVariable Long id) {
		return ResponseEntity.ok(configurationService.getConfigurationById(id));
	}

	@GetMapping("/hero")
	public ResponseEntity<String> getHeroBackgroundImage() {
		try {
			return ResponseEntity.ok(configurationService.getHeroBackgroundImage());
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(404).body(null);
		}
	}

	@GetMapping("/all")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<ConfigurationResponse>> getAllConfigurations() {
		return ResponseEntity.ok(configurationService.getAllConfigurations());
	}

	@PostMapping("/admin/config")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ConfigurationResponse> createConfiguration(@Valid @RequestBody ConfigurationRequest request) {
		return ResponseEntity.status(201).body(configurationService.createConfiguration(request));
	}

	@PutMapping("/admin/config/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ConfigurationResponse> updateConfiguration(
			@PathVariable Long id, @Valid @RequestBody ConfigurationRequest request) {
		return ResponseEntity.ok(configurationService.updateConfiguration(id, request));
	}

	@DeleteMapping("/admin/config/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteConfiguration(@PathVariable Long id) {
		configurationService.deleteConfiguration(id);
		return ResponseEntity.noContent().build();
	}
}