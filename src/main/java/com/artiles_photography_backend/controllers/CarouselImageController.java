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

import com.artiles_photography_backend.dtos.CarouselImageResponse;
import com.artiles_photography_backend.dtos.CarouselImageSelectRequest;
import com.artiles_photography_backend.dtos.CarouselImageUpdateRequest;
import com.artiles_photography_backend.dtos.CarouselImageUploadRequest;
import com.artiles_photography_backend.services.CarouselImageService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * @author arojas
 *         Controlador REST para manejar peticiones relacionadas con imágenes
 *         del carrusel, incluyendo subida y eliminación en Cloudinary.
 */
@RestController
@RequestMapping("/api/carousel")
public class CarouselImageController {

	private static final Logger logger = LoggerFactory.getLogger(CarouselImageController.class);
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

	@PostMapping("/select")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<CarouselImageResponse> selectCarouselImage(
			@RequestBody @Valid CarouselImageSelectRequest request) {
		return ResponseEntity.status(201).body(carouselImageService.selectCarouselImage(request));
	}

	// Método para actualizar con multipart/form-data (con archivos)
	@PutMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<CarouselImageResponse> updateCarouselImageWithFile(
			@PathVariable Long id, @Valid @ModelAttribute CarouselImageUpdateRequest request) {
		logger.info("Update carousel image with file - ID: {}, File: {}",
				id, request.getFile() != null ? request.getFile().getOriginalFilename() : "none");
		return ResponseEntity.ok(carouselImageService.updateCarouselImage(id, request));
	}

	// Método para actualizar solo metadatos con JSON (sin archivos)
	// Alternativa: Un solo endpoint que detecta el Content-Type
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<CarouselImageResponse> updateCarouselImage(
			@PathVariable Long id,
			HttpServletRequest request) {

		String contentType = request.getContentType();

		if (contentType != null && contentType.startsWith("multipart/form-data")) {
			// Procesar como multipart
			try {
				CarouselImageUpdateRequest updateRequest = new CarouselImageUpdateRequest();
				// Extraer manualmente los parámetros del request multipart
				// ... lógica para extraer file, title, description, etc.
				return ResponseEntity.ok(carouselImageService.updateCarouselImage(id, updateRequest));
			} catch (Exception e) {
				throw new RuntimeException("Error processing multipart request", e);
			}
		} else if (contentType != null && contentType.startsWith("application/json")) {
			// Procesar como JSON
			try {
				ObjectMapper mapper = new ObjectMapper();
				CarouselImageUpdateRequest updateRequest = mapper.readValue(
						request.getInputStream(), CarouselImageUpdateRequest.class);
				return ResponseEntity.ok(carouselImageService.updateCarouselImage(id, updateRequest));
			} catch (Exception e) {
				throw new RuntimeException("Error processing JSON request", e);
			}
		} else {
			throw new IllegalArgumentException("Unsupported content type: " + contentType);
		}
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteCarouselImage(@PathVariable Long id) {
		carouselImageService.deleteCarouselImage(id);
		return ResponseEntity.noContent().build();
	}
}