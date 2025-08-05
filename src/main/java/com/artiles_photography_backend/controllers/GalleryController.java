package com.artiles_photography_backend.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.dtos.GalleryImageUpdateRequest;
import com.artiles_photography_backend.dtos.GalleryResponse;
import com.artiles_photography_backend.dtos.GallerySelectRequest;
import com.artiles_photography_backend.dtos.GalleryUploadRequest;
import com.artiles_photography_backend.services.GalleryService;

import jakarta.validation.Valid;

/**
 * @author arojas
 *         Controlador REST para manejar peticiones relacionadas con imágenes de
 *         la galería, incluyendo subida y eliminación en Cloudinary.
 */
@RestController
@RequestMapping("/api/gallery")
public class GalleryController {

	private static final Logger logger = LoggerFactory.getLogger(GalleryController.class);
	private final GalleryService galleryService;

	public GalleryController(GalleryService galleryService) {
		this.galleryService = galleryService;
	}

	@GetMapping
	public ResponseEntity<List<GalleryResponse>> getAllGalleryImages() {
		return ResponseEntity.ok(galleryService.getAllGalleryImages());
	}

	@GetMapping("/{id}")
	public ResponseEntity<GalleryResponse> getGalleryImageById(@PathVariable Long id) {
		return ResponseEntity.ok(galleryService.getGalleryImageById(id));
	}

	@PostMapping("/cloudinary")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<GalleryResponse> createGalleryImageWithCloudinary(
			@RequestBody @Valid GallerySelectRequest request) {
		logger.info("Creating gallery image with Cloudinary publicId: {}", request.getPublicId());
		return ResponseEntity.status(201).body(galleryService.createGalleryImageWithCloudinary(request));
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<GalleryResponse> uploadGalleryImage(
			@Valid @ModelAttribute GalleryUploadRequest request) {
		logger.info("Uploading gallery image - File: {}",
				request.getFile() != null ? request.getFile().getOriginalFilename() : "none");
		return ResponseEntity.status(201).body(galleryService.createGalleryImage(request));
	}

	// Método para actualizar con multipart/form-data (con archivos)
	@PutMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<GalleryResponse> updateGalleryImageWithFile(
			@PathVariable Long id, @Valid @ModelAttribute GalleryImageUpdateRequest request) {
		logger.info("Update gallery image with file - ID: {}, File: {}, PublicId: {}",
				id, request.getFile() != null ? request.getFile().getOriginalFilename() : "none",
				request.getPublicId());
		return ResponseEntity.ok(galleryService.updateGalleryImage(id, request));
	}

	// Método para actualizar solo metadatos con JSON (sin archivos)
	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<GalleryResponse> updateGalleryImageMetadata(
			@PathVariable Long id, @RequestBody @Valid GalleryImageUpdateRequest request) {
		logger.info("Update gallery image metadata - ID: {}, PublicId: {}",
				id, request.getPublicId());
		return ResponseEntity.ok(galleryService.updateGalleryImage(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteGalleryImage(@PathVariable Long id) {
		galleryService.deleteGalleryImage(id);
		return ResponseEntity.noContent().build();
	}
}