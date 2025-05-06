package com.artiles_photography_backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.models.Configuration;
import com.artiles_photography_backend.repository.ConfigurationRepository;
import com.artiles_photography_backend.services.ConfigurationService;

/**
 *
 * @author arojas
 */
@RestController
@RequestMapping("/api/config")
public class ConfigurationController {

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private ConfigurationRepository repository;

	@GetMapping
	public ResponseEntity<Configuration> getConfiguration() {
		return repository.findById(1L)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/hero")
	public ResponseEntity<String> getHeroBackgroundImage() {
		return ResponseEntity.ok(configurationService.getHeroBackgroundImage());
	}

	@PostMapping
	public ResponseEntity<Configuration> saveConfiguration(@RequestBody Configuration config) {
		return ResponseEntity.ok(configurationService.saveConfiguration(config));
	}
}
