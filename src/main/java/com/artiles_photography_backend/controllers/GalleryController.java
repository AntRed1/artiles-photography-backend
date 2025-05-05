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
 */
@RestController
@RequestMapping("/api/config")
public class GalleryController {
	@Autowired
	private GalleryService galleryService;

	@GetMapping("/gallery")
	public ResponseEntity<List<Gallery>> getGalleryImages() {
		return ResponseEntity.ok(galleryService.getGalleryImages());
	}

	@GetMapping("/carousel")
	public ResponseEntity<List<Gallery>> getCarouselImages() {
		return ResponseEntity.ok(galleryService.getCarouselImages());
	}

	@PostMapping("/gallery")
	public ResponseEntity<Gallery> saveGalleryImage(@RequestBody Gallery gallery) {
		return ResponseEntity.ok(galleryService.saveGalleryImage(gallery));
	}
}
