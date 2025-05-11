package com.artiles_photography_backend.controllers;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.dtos.PhotographyPackageResponse;
import com.artiles_photography_backend.dtos.PhotographyPackageUploadRequest;
import com.artiles_photography_backend.services.PhotographyPackageService;

import jakarta.validation.Valid;

/**
 * @author arojas
 *         Controlador REST para manejar peticiones relacionadas con paquetes
 *         fotográficos, incluyendo subida y eliminación en Cloudinary.
 */
@RestController
@RequestMapping("/api/packages")
public class PhotographyPackageController {

	private final PhotographyPackageService service;

	public PhotographyPackageController(PhotographyPackageService service) {
		this.service = service;
	}

	@GetMapping
	public ResponseEntity<List<PhotographyPackageResponse>> getAllPhotographyPackages() {
		return ResponseEntity.ok(service.getAllPhotographyPackages());
	}

	@GetMapping("/{id}")
	public ResponseEntity<PhotographyPackageResponse> getPhotographyPackageById(@PathVariable Long id) {
		return ResponseEntity.ok(service.getById(id));
	}

	@GetMapping("/active")
	public ResponseEntity<List<PhotographyPackageResponse>> getActivePhotographyPackages() {
		return ResponseEntity.ok(service.getActivePhotographyPackages());
	}

	@PostMapping(value = "/admin/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<PhotographyPackageResponse> createPhotographyPackage(
			@Valid @ModelAttribute PhotographyPackageUploadRequest request) {
		return ResponseEntity.status(201).body(service.createPhotographyPackage(request));
	}

	@PutMapping(value = "/admin/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<PhotographyPackageResponse> updatePhotographyPackage(
			@PathVariable Long id, @Valid @ModelAttribute PhotographyPackageUploadRequest request) {
		return ResponseEntity.ok(service.updatePhotographyPackage(id, request));
	}

	@DeleteMapping("/admin/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deletePhotographyPackage(@PathVariable Long id) {
		service.deletePhotographyPackage(id);
		return ResponseEntity.noContent().build();
	}
}