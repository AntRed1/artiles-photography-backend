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

import com.artiles_photography_backend.dtos.PhotographyServiceRequest;
import com.artiles_photography_backend.dtos.PhotographyServiceResponse;
import com.artiles_photography_backend.services.PhotographyServiceService;

import jakarta.validation.Valid;

/**
 *
 * @author arojas
 *         * Controlador REST para manejar peticiones relacionadas con
 *         PhotographyService.
 *
 */
@RestController
@RequestMapping("/api/services")
public class PhotographyServiceController {

	private final PhotographyServiceService photographyServiceService;

	public PhotographyServiceController(PhotographyServiceService photographyServiceService) {
		this.photographyServiceService = photographyServiceService;
	}

	@GetMapping
	public ResponseEntity<List<PhotographyServiceResponse>> getAllPhotographyServices() {
		return ResponseEntity.ok(photographyServiceService.getAllPhotographyServices());
	}

	@GetMapping("/{id}")
	public ResponseEntity<PhotographyServiceResponse> getPhotographyServiceById(@PathVariable Long id) {
		return ResponseEntity.ok(photographyServiceService.getPhotographyServiceById(id));
	}

	@PostMapping("/admin/services")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<PhotographyServiceResponse> createPhotographyService(
			@Valid @RequestBody PhotographyServiceRequest request) {
		return ResponseEntity.status(201).body(photographyServiceService.createPhotographyService(request));
	}

	@PutMapping("/admin/services/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<PhotographyServiceResponse> updatePhotographyService(
			@PathVariable Long id, @Valid @RequestBody PhotographyServiceRequest request) {
		return ResponseEntity.ok(photographyServiceService.updatePhotographyService(id, request));
	}

	@DeleteMapping("/admin/services/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deletePhotographyService(@PathVariable Long id) {
		photographyServiceService.deletePhotographyService(id);
		return ResponseEntity.noContent().build();
	}
}