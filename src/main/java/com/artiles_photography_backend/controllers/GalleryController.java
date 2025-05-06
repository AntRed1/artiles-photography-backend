package com.artiles_photography_backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.models.Gallery;
import com.artiles_photography_backend.services.GalleryService;

/**
 *
 * @author arojas
 *         * Controlador REST para manejar peticiones relacionadas con Gallery.
 * 
 */
@RestController
@RequestMapping("/api/gallery")
public class GalleryController {

	private final GalleryService galleryService;

	@Autowired
	public GalleryController(GalleryService galleryService) {
		this.galleryService = galleryService;
	}

	/**
	 * Obtiene todas las imágenes de la galería.
	 */
	@GetMapping
	public ResponseEntity<List<Gallery>> getAllGalleryImages() {
		return ResponseEntity.ok(galleryService.getAllGalleryImages());
	}

	/**
	 * Guarda una nueva imagen en la galería.
	 */
	@PostMapping
	public ResponseEntity<Gallery> saveGallery(@RequestBody Gallery gallery) {
		return ResponseEntity.ok(galleryService.saveGallery(gallery));
	}
}
