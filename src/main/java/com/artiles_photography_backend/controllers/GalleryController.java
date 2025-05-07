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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.dtos.GalleryRequest;
import com.artiles_photography_backend.dtos.GalleryResponse;
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

	@PostMapping(value = "/admin/gallery/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<GalleryResponse> uploadGalleryImage(
			@Valid @ModelAttribute GalleryUploadRequest request) {
		return ResponseEntity.status(201).body(galleryService.createGalleryImage(request));
	}

	@PutMapping("/admin/gallery/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<GalleryResponse> updateGalleryImage(
			@PathVariable Long id, @Valid @RequestBody GalleryRequest request) {
		return ResponseEntity.ok(galleryService.updateGalleryImage(id, request));
	}

	@DeleteMapping("/admin/gallery/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteGalleryImage(@PathVariable Long id) {
		galleryService.deleteGalleryImage(id);
		return ResponseEntity.noContent().build();
	}
}