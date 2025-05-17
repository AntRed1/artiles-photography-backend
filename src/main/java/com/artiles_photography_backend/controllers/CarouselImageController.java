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

import com.artiles_photography_backend.dtos.CarouselImageResponse;
import com.artiles_photography_backend.dtos.CarouselImageUpdateRequest;
import com.artiles_photography_backend.dtos.CarouselImageUploadRequest;
import com.artiles_photography_backend.services.CarouselImageService;

import jakarta.validation.Valid;

/**
 * @author arojas
 *         Controlador REST para manejar peticiones relacionadas con imágenes
 *         del carrusel, incluyendo subida y eliminación en Cloudinary.
 */
@RestController
@RequestMapping("/api/carousel")
public class CarouselImageController {

	private final CarouselImageService carouselImageService;

	public CarouselImageController(CarouselImageService carouselImageService) {
		this.carouselImageService = carouselImageService;
	}

	@GetMapping
	public ResponseEntity<List<CarouselImageResponse>> getAllCarouselImages() {
		return ResponseEntity.ok(carouselImageService.getAllCarouselImages());
	}

	@GetMapping("/{id}")
	public ResponseEntity<CarouselImageResponse> getCarouselImageById(@PathVariable Long id) {
		return ResponseEntity.ok(carouselImageService.getCarouselImageById(id));
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<CarouselImageResponse> uploadCarouselImage(
			@Valid @ModelAttribute CarouselImageUploadRequest request) {
		return ResponseEntity.status(201).body(carouselImageService.createCarouselImage(request));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<CarouselImageResponse> updateCarouselImage(
			@PathVariable Long id, @Valid @ModelAttribute CarouselImageUpdateRequest request) {
		return ResponseEntity.ok(carouselImageService.updateCarouselImage(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteCarouselImage(@PathVariable Long id) {
		carouselImageService.deleteCarouselImage(id);
		return ResponseEntity.noContent().build();
	}
}